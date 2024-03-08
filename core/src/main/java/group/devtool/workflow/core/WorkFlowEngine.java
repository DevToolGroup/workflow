package group.devtool.workflow.core;

import java.util.ArrayList;
import java.util.List;

import group.devtool.workflow.core.WorkFlowCallback.WorkFlowEvent;
import group.devtool.workflow.core.exception.WorkFlowException;

/**
 * 流程引擎主要职责：
 * 1. 完成流程定义的部署及卸载
 * 2. 完成流程的启动、执行、终止
 * 3. 流程事件的触发
 */
public final class WorkFlowEngine {

  private final WorkFlowConfiguration configuration;

  public WorkFlowEngine(WorkFlowConfiguration configuration) {
    this.configuration = configuration;
  }

  /**
   * 部署流程定义
   * 
   * @param definition 流程定义
   * @throws WorkFlowException
   */
  public void deploy(WorkFlowDefinition definition) throws WorkFlowException {
    configuration.definitionService().deploy(definition);
  }

  /**
   * 加载流程定义
   * 
   * @param code    流程定义编码
   * @param version 流程定义版本
   * @return 流程定义
   * @throws WorkFlowException
   */
  public WorkFlowDefinition load(String code, Integer version) throws WorkFlowException {
    return configuration.definitionService().load(code, version, true);
  }

  /**
   * 卸载流程定义
   * 
   * @param code
   * @throws WorkFlowException
   */
  public void undeploy(String code) throws WorkFlowException {
    configuration.definitionService().undeploy(code);
  }

  /**
   * 加载流程图以及流程运行节点信息
   * 
   * @param rootInstanceId 根流程实例ID
   * @return 流程图
   * @throws WorkFlowException 
   */
  public List<WorkFlowTask> loadTask(String rootInstanceId) throws WorkFlowException {
    return configuration.service().loadActiveTask(rootInstanceId);
  }

  /**
   * 根据流程定义编码，启动流程引擎，初始化流程实例
   * 
   * @param code      流程定义编码
   * @param variables 流程启动参数
   * @return 根流程实例ID
   */
  public String start(String code, WorkFlowVariable... variables) throws WorkFlowException {
    WorkFlowInstance instance = configuration.service().getInstance(code);
    // 初始化流程上下文
    WorkFlowContext context = configuration.service().getContext(instance.instanceId());
    context.localVariable(variables);
    String instanceId = instance.instanceId();

    configuration.transaction().begin();
    try {
      start(instance.instanceId(), instance, context);
      configuration.service().save(context);
      configuration.transaction().commit();
    } catch (Exception exception) {
      configuration.transaction().rollback();
      throw exception;
    } finally {
      configuration.transaction().close();
    }

    return instanceId;
  }

  private void start(String rootInstanceId, WorkFlowInstance instance, WorkFlowContext context)
      throws WorkFlowException {
    // 启动实例
    WorkFlowNode node = instance.start((ndfs, id, rootId, ctx) -> {
      List<WorkFlowNode> nodes = new ArrayList<>();
      for (WorkFlowNodeDefinition ndf : ndfs) {
        nodes.add(configuration.service().getNode(ndf, id, rootId, ctx));
      }
      return nodes;
    }, context);

    // 节点前置操作
    node.beforeComplete();

    // 上下文设置
    context.position(node.getCode());
    context.instanceId(instance.instanceId());
    context.snapshot(null);

    // 节点、实例持久化
    configuration.service().save(node, rootInstanceId);
    configuration.service().save(instance, rootInstanceId);

    // 回调
    configuration.callback().callback(WorkFlowEvent.START, context);

    // 流程继续流转
    run(rootInstanceId, instance, node.getCode(), context);
  }

  /**
   * 流程节点流转，如果流程是嵌套子流程的话，递归流转
   * 
   * @param variables      流转变量
   * @param taskId         流程任务ID
   * @param rootInstanceId 根流程实例ID
   */
  public void run(String rootInstanceId, String taskId, WorkFlowVariable... variables) throws WorkFlowException {
    // 初始化上下文 -- 上下文在并发条件下
    WorkFlowContext context = configuration.service().getContext(rootInstanceId);
    context.localVariable(variables);
    configuration.transaction().begin();
    try {
      // 运行流程引擎
      run(rootInstanceId, taskId, context);
      // 持久化上下文
      configuration.service().save(context);
      configuration.transaction().commit();
    } catch (Exception e) {
      configuration.transaction().rollback();
      throw e;
    } finally {
      configuration.transaction().close();
    }
  }

  private void run(String rootInstanceId, String taskId, WorkFlowContext context) throws WorkFlowException {
    WorkFlowTask task = configuration.service().getTask(taskId, rootInstanceId);
    // 任务操作
    task.complete(context);

    // 流程变量设置 上下文中设置当前节点
    context.position(task.getNodeCode());
    context.instanceId(task.getInstanceId());
    context.snapshot(task.getTaskId());

    // 任务状态变更
    configuration.service().changeTaskComplete(task);

    // 如果节点节点状态未完成，则直接返回，相反，继续流转
    WorkFlowNode node = configuration.service().getNode(task.getNodeCode(), rootInstanceId);
    if (!node.done()) {
      return;
    }
    // 节点完成后置操作
    node.afterComplete();

    // 保存节点状态
    configuration.service().changeNodeComplete(node);
    configuration.callback().callback(WorkFlowEvent.COMPLETE, context);

    // 查询当前任务对应的流程实例
    WorkFlowInstance instance = configuration.service().getInstance(task.getInstanceId(), rootInstanceId);
    String nodeCode = node.getCode();
    // 清理局部的变量
    task = null;
    node = null;
    run(rootInstanceId, instance, nodeCode, context);
  }

  private void run(String rootInstanceId, WorkFlowInstance instance, String nodeCode, WorkFlowContext context)
      throws WorkFlowException {
    // 节点流转
    next(rootInstanceId, instance, nodeCode, context);
    // 判断流程实例是否已完成，如果没完成则直接返回
    if (!instance.done()) {
      return;
    }
    configuration.service().save(instance, rootInstanceId);
    // 流程结束，回调
    configuration.callback().callback(WorkFlowEvent.END, context);
    // 判断已完成实例是否是嵌套子流程，如果不是则直接返回
    if (!(instance instanceof ChildWorkFlowInstance child)) {
      return;
    }
		String parentTaskId = child.parentId();
    // 清理局部的变量
    child = null;
    instance = null;
    // 如果流程实例是嵌套子流程，则判断父流程节点的状态
    run(rootInstanceId, parentTaskId, context);
  }

  private void next(String rootInstanceId, WorkFlowInstance instance, String nodeCode, WorkFlowContext context)
      throws WorkFlowException {
    // 调用instance next 方法待执行任务节点列表
    List<WorkFlowNode> nodes = instance.next((ndfs, instanceId, rootId, ctx) -> {
      List<WorkFlowNode> ns = new ArrayList<>();
      for (WorkFlowNodeDefinition ndf : ndfs) {
        ns.add(configuration.service().getNode(ndf, instanceId, rootId, ctx));
      }
      return ns;
    }, nodeCode, context);
    // 保存流程节点实例，并触发节点创建事件
    for (WorkFlowNode node : nodes) {
      // 节点前置操作
      node.beforeComplete();
      // 保存node
      configuration.service().save(node, rootInstanceId);

      // 设置上下文当前节点位置
      context.position(node.getCode());

      // 节点创建事件回调
      configuration.callback().callback(WorkFlowEvent.CREATED, context);

      // 如果是嵌套子流程
      if (node instanceof AbstractChildWorkFlowNode) {
        nextChild(rootInstanceId, (AbstractChildWorkFlowNode) node, context);
      }

      // 如果节点直接完成，则继续往下流转，相反返回等待触发
      if (!node.done()) {
        continue;
      }

      node.afterComplete();
      configuration.service().changeNodeComplete(node);
      // 节点完成事件触发
      configuration.callback().callback(WorkFlowEvent.COMPLETE, context);

      // 继续流转
      next(rootInstanceId, instance, node.getCode(), context);
    }
  }

  private void nextChild(String rootInstanceId, AbstractChildWorkFlowNode node, WorkFlowContext context)
      throws WorkFlowException {
    List<WorkFlowInstance> instances = node.instances((definitionCode, taskId) -> {
      return configuration.service().getChildInstance(definitionCode, taskId, rootInstanceId);
    });
    for (WorkFlowInstance childInstance : instances) {
      start(rootInstanceId, childInstance, context);
    }
  }

  /**
   * 终止流程实例
   * 
   * @param instanceId 流程
   * @throws WorkFlowException
   */
  public void stop(String instanceId) throws WorkFlowException {
    WorkFlowInstance instance = configuration.service().getInstance(instanceId, instanceId);
    instance.stop();
    configuration.transaction().begin();
    try {
      configuration.service().save(instance, instanceId);
      configuration.transaction().commit();
      WorkFlowContext context = configuration.service().getContext(instanceId);
      context.instanceId(instanceId);
      configuration.callback().callback(WorkFlowEvent.STOP, context);
    } catch (Throwable e) {
      configuration.transaction().rollback();
      throw e;
    } finally {
      configuration.transaction().close();
    }
  }

}
