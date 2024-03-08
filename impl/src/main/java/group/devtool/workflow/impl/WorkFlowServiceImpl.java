package group.devtool.workflow.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import group.devtool.workflow.core.AbstractWorkFlowService;
import group.devtool.workflow.core.ChildWorkFlowInstance;
import group.devtool.workflow.core.ParentWorkFlowInstance;
import group.devtool.workflow.core.WorkFlowContext;
import group.devtool.workflow.core.WorkFlowDefinition;
import group.devtool.workflow.core.WorkFlowInstance;
import group.devtool.workflow.core.WorkFlowNode;
import group.devtool.workflow.core.WorkFlowNodeDefinition;
import group.devtool.workflow.core.WorkFlowTask;
import group.devtool.workflow.core.WorkFlowVariable;
import group.devtool.workflow.core.WorkFlowTask.WorkFlowTaskState;
import group.devtool.workflow.core.WorkFlowInstance.WorkFlowInstanceState;
import group.devtool.workflow.core.WorkFlowNode.WorkFlowNodeState;
import group.devtool.workflow.core.WorkFlowTransactionRetry.WorkFlowTransactionOperation;
import group.devtool.workflow.core.exception.*;

/**
 * {@link AbstractWorkFlowService} 默认实现
 */
public class WorkFlowServiceImpl extends AbstractWorkFlowService {

  private final WorkFlowConfigurationImpl config;

  public WorkFlowServiceImpl() {
    this.config = WorkFlowConfigurationImpl.CONFIG;
  }

  @Override
  public WorkFlowContext getContext(String instanceId, WorkFlowVariable... variables) throws WorkFlowException {
    List<WorkFlowVariableEntity> ves = loadVariableInTransaction(instanceId);
    WorkFlowContext context = new WorkFlowContext(instanceId);
    for (WorkFlowVariableEntity entity : ves) {
      context.add(WorkFlowVariable.bound(entity.getName(), deserialize(entity.getValue()), entity.getTaskId(),
          entity.getNode()));
    }
    context.add(variables);
    return context;
  }

  private List<WorkFlowVariableEntity> loadVariableInTransaction(String instanceId) throws WorkFlowException {
    return config.dbTransaction().doInTransaction(() -> {
      return config.repository().loadVariable(instanceId);
    });
  }

  private Serializable deserialize(byte[] value) throws WorkFlowException {
    try (ByteArrayInputStream is = new ByteArrayInputStream(value);
        ObjectInputStream ois = new ObjectInputStream(is)) {
      Object oo = ois.readObject();
      if (!(oo instanceof Serializable)) {
        throw new CastWorkFlowClassException("流程变量反序列化仅支持Serializable及其子类型");
      }
      return (Serializable) oo;
    } catch (IOException | ClassNotFoundException e) {
      throw new SerializeException(String.format("流程变量反序列化异常，%s", e.getMessage()));
    }
  }

  @Override
  protected WorkFlowTransactionOperation doSave(WorkFlowContext context) throws WorkFlowException {
    List<WorkFlowVariableEntity> addEntities = new ArrayList<>();

    for (WorkFlowVariable variable : context.variables()) {
      addEntities.add(toEntity(context, variable));
    }
    return config.dbTransaction().doInTransaction(() -> {
      config.repository().bulkSaveVariable(addEntities);
      return MybatisWorkFlowTransaction.addVariableOperation(addEntities, config.repository());
    });
  }

  private WorkFlowVariableEntity toEntity(WorkFlowContext context, WorkFlowVariable variable)
      throws WorkFlowException {
    return new WorkFlowVariableEntity(
        variable.getName(),
        serialize(variable.getValue()),
        variable.getNode(),
        variable.getTaskId(),
        context.rootInstanceId());
  }

  private byte[] serialize(Object value) throws WorkFlowException {
    if (!(value instanceof Serializable)) {
      throw new NotSupportWorkFlowVariableClass("流程变量序列化仅支持Serializable子类");
    }
    try (ByteArrayOutputStream us = new ByteArrayOutputStream();
        ObjectOutputStream ous = new ObjectOutputStream(us)) {
      ous.writeObject(value);
      return us.toByteArray();
    } catch (IOException e) {
      throw new SerializeException(String.format("流程变量序列化异常，%s", e.getMessage()));
    }
  }

  @Override
  public WorkFlowInstance getInstance(String code) throws WorkFlowException {
    WorkFlowDefinition definition = config.definitionService().load(code, null, false);
    return config.factory().factory(definition);
  }

  @Override
  public WorkFlowInstance getChildInstance(String definitionCode, String taskId, String rootInstanceId)
      throws WorkFlowException {
    WorkFlowTaskEntity task = loadTaskInTransaction(taskId, rootInstanceId);
    WorkFlowInstanceEntity parent = loadInstanceInTransaction(rootInstanceId, task.getInstanceId());
    WorkFlowDefinition child = config.definitionService().load(definitionCode, parent.getDefinitionVersion(), false);
    return config.factory().childFactory(child, taskId, rootInstanceId);
  }

  private WorkFlowInstanceEntity loadInstanceInTransaction(String rootInstanceId, String instanceId)
      throws WorkFlowException {
    return config.dbTransaction().doInTransaction(() -> {
      return config.repository().getInstance(instanceId, rootInstanceId);
    });
  }

  private WorkFlowTaskEntity loadTaskInTransaction(String taskId, String rootInstanceId)
      throws WorkFlowException {
    return config.dbTransaction().doInTransaction(() -> {
      return config.repository().getTaskById(taskId, rootInstanceId);
    });
  }

  @Override
  public WorkFlowInstance getInstance(String instanceId, String rootInstanceId)
      throws WorkFlowException {
    WorkFlowInstanceEntity entity = loadInstanceInTransaction(rootInstanceId, instanceId);
    if (null == entity) {
      throw new NotFoundWorkFlowInstance(instanceId, rootInstanceId);
    }
    WorkFlowDefinition definition = config.definitionService().load(entity.getDefinitionCode(),
        entity.getDefinitionVersion(), false);
    return config.factory().factory(entity, definition);
  }

  @Override
  public WorkFlowNode getNode(WorkFlowNodeDefinition ndf, String instanceId, String rootInstanceId,
      WorkFlowContext context) throws WorkFlowException {
    return config.factory().factory(ndf, instanceId, rootInstanceId, context);
  }

  @Override
  public WorkFlowNode getNode(String nodeCode, String rootInstanceId) throws WorkFlowException {
    List<WorkFlowTaskEntity> entities = loadTaskByNodeInTransaction(nodeCode, rootInstanceId);
    if (entities.size() < 1) {
      throw new NotFoundWorkFlowNode(nodeCode, rootInstanceId);
    }
    String nodeClass = entities.get(0).getNode().getNodeClass();
    String instanceId = entities.get(0).getInstanceId();
    byte[] nodeConfig = entities.get(0).getConfig();
    List<WorkFlowTask> tasks = new ArrayList<>(entities.size());
    for (WorkFlowTaskEntity entity : entities) {
      tasks.add(config.factory().factory(entity));
    }
    return config.factory().factory(nodeCode, nodeClass, nodeConfig, instanceId, rootInstanceId, tasks);
  }

  private List<WorkFlowTaskEntity> loadTaskByNodeInTransaction(String nodeCode, String rootInstanceId)
      throws WorkFlowException {
    return config.dbTransaction().doInTransaction(() -> {
      return config.repository().getTaskByCode(nodeCode, rootInstanceId);
    });
  }

  @Override
  public WorkFlowTask getTask(String taskId, String rootInstanceId) throws WorkFlowException {
    WorkFlowTaskEntity entity = loadTaskInTransaction(taskId, rootInstanceId);
    return config.factory().factory(entity);
  }

  /**
   * 持久化节点数据到数据库
   * 
   */
  @Override
  public WorkFlowTransactionOperation doSave(WorkFlowNode node, String rootInstanceId) throws WorkFlowException {
    List<WorkFlowTaskEntity> entities = new ArrayList<>();

    String nodeCode = node.getCode();
    String nodeClass = node.getNodeClass();
    String nodeState = node.done() ? WorkFlowNodeState.DONE.name() : WorkFlowNodeState.DOING.name();

    for (WorkFlowTask task : node.getTasks()) {
      entities.add(toEntity(task, nodeClass, nodeCode, nodeState));
    }

    return config.dbTransaction().doInTransaction(() -> {
      config.repository().bulkSaveTask(entities);
      return MybatisWorkFlowTransaction.addTaskOperation(entities, config.repository());
    });
  }

  private WorkFlowTaskEntity toEntity(WorkFlowTask task, String nodeClass, String nodeCode, String nodeState) {
    WorkFlowTaskEntity entity = new WorkFlowTaskEntity();
    entity.setTaskId(task.getTaskId());
    entity.setTaskClass(task.getTaskClass());
    entity.setTaskState(task.completed() ? WorkFlowTaskState.DONE.name() : WorkFlowTaskState.DOING.name());
    entity.setConfig(task.getTaskConfig());
    entity.setCompleteUser(task.getCompleteUser());
    entity.setCompleteTime(task.getCompleteTime());
    entity.setInstanceId(task.getInstanceId());
    entity.setRootInstanceId(task.getRootInstanceId());
    entity.setNodeClass(nodeClass);
    entity.setNodeCode(nodeCode);
    entity.setNodeState(nodeState);
    return entity;
  }

  @Override
  public WorkFlowTransactionOperation doChangeTaskComplete(WorkFlowTask task) throws WorkFlowException {

    return config.dbTransaction().doInTransaction(() -> {
      int rows = config.repository().changeTaskComplete(task.getCompleteUser(), task.getCompleteTime(), task.getTaskId(),
              task.getRootInstanceId());
      if (rows != 1) {
        throw new ConcurrentException("节点任务不存在或已执行完成，任务ID：" + task.getTaskId());
      }
      return MybatisWorkFlowTransaction.addTaskCompleteOperation(task.getTaskId(), task.getRootInstanceId(), config.repository());
    });
  }

  @Override
  public WorkFlowTransactionOperation doSave(WorkFlowInstance instance, String rootInstanceId)
      throws WorkFlowException {
    WorkFlowInstanceEntity entity = new WorkFlowInstanceEntity();
    entity.setInstanceId(instance.instanceId());
    if (instance.done()) {
      entity.setState(WorkFlowInstanceState.DONE.name());
    } else if (instance.stopped()) {
      entity.setState(WorkFlowInstanceState.STOP.name());
    } else {
      entity.setState(WorkFlowInstanceState.DOING.name());
    }
    if (instance instanceof ParentWorkFlowInstance) {
      entity.setRootInstanceId(instance.instanceId());
      entity.setParentTaskId(null);
    } else if (instance instanceof ChildWorkFlowInstance) {
      entity.setRootInstanceId(((ChildWorkFlowInstance) instance).rootId());
      entity.setParentTaskId(((ChildWorkFlowInstance) instance).parentId());
    }
    entity.setDefinitionCode(instance.getDefinitionCode());
    entity.setDefinitionVersion(instance.getDefinitionVersion());
    entity.setId(instance.getId());

    return config.dbTransaction().doInTransaction(() -> {
      config.repository().save(entity);
      return MybatisWorkFlowTransaction.addInstanceOperation(entity, config.repository());
    });
  }

  @Override
  public WorkFlowTransactionOperation doChangeNodeComplete(WorkFlowNode node) throws WorkFlowException {
    return config.dbTransaction().doInTransaction(() -> {
      int rows = config.repository().changeNodeComplete(node.getCode(), node.getRootInstanceId());
      if (rows < 1) {
        throw new WorkFlowException("节点不存在或已执行完成，节点编码：" + node.getCode());
      }
      return MybatisWorkFlowTransaction.addNodeCompleteOperation(node.getCode(), node.getRootInstanceId(), config.repository());
    });
  }

  @Override
  public List<WorkFlowTask> loadActiveTask(String rootInstanceId) throws WorkFlowException {
    return config.dbTransaction().doInTransaction(() -> {
      List<WorkFlowTaskEntity> entities = config.repository().loadActiveTask(rootInstanceId);
      List<WorkFlowTask> tasks = new ArrayList<>();
      for (WorkFlowTaskEntity entity: entities) {
        tasks.add(config.factory().factory(entity));
      }
      return tasks;
    });
  }

}
