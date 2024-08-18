/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import group.devtool.workflow.engine.*;
import group.devtool.workflow.engine.common.InstanceScope;
import group.devtool.workflow.engine.common.JacksonUtils;
import group.devtool.workflow.engine.common.InstanceState;
import group.devtool.workflow.engine.runtime.*;
import group.devtool.workflow.engine.runtime.WorkFlowTask.WorkFlowTaskState;
import group.devtool.workflow.engine.runtime.WorkFlowNode.WorkFlowNodeState;
import group.devtool.workflow.engine.definition.WorkFlowDefinition;
import group.devtool.workflow.engine.definition.WorkFlowNodeDefinition;
import group.devtool.workflow.engine.exception.*;
import group.devtool.workflow.impl.entity.WorkFlowInstanceEntity;
import group.devtool.workflow.impl.entity.WorkFlowNodeEntity;
import group.devtool.workflow.impl.entity.WorkFlowTaskEntity;
import group.devtool.workflow.impl.entity.WorkFlowVariableEntity;
import group.devtool.workflow.engine.WorkFlowVariable.GlobalWorkFlowVariable;
import group.devtool.workflow.impl.repository.WorkFlowRepository;

/**
 * {@link WorkFlowService} 默认实现
 */
public class WorkFlowServiceImpl implements WorkFlowService {

	private final WorkFlowConfigurationImpl config;

	public WorkFlowServiceImpl() {
		this.config = WorkFlowConfigurationImpl.CONFIG;
	}

	@Override
	public WorkFlowContextImpl getContext(String instanceId, WorkFlowVariable... variables) {
		List<WorkFlowVariableEntity> entities = config.repository().loadVariable(instanceId);
		WorkFlowVariable[] dbVariables = new WorkFlowVariable[entities.size()];
		for (int i = 0; i < entities.size(); i++) {
			WorkFlowVariableEntity entity = entities.get(i);
			dbVariables[i] = WorkFlowVariable.global(entity.getName(), entity.getValue());
		}
		WorkFlowContextImpl context = new WorkFlowContextImpl(instanceId, dbVariables);
		context.addRuntimeVariable(variables);
		return context;
	}

	@Override
	public void saveVariable(WorkFlowContext context) {
		WorkFlowContextImpl implContext = (WorkFlowContextImpl) context;
		List<WorkFlowVariableEntity> addEntities = new ArrayList<>();

		for (WorkFlowVariable variable : implContext.getRuntimeVariables()) {
			if (variable instanceof GlobalWorkFlowVariable) {
				addEntities.add(new WorkFlowVariableEntity(variable.getName(), JacksonUtils.serialize(variable.getValue()), context.getRootInstanceId()));
			}
		}
		if (!addEntities.isEmpty()) {
			config.repository().bulkSaveVariable(addEntities);
		}
		implContext.clearRuntimeVariables();
	}

	@Override
	public WorkFlowInstance getInstance(String code) {
		WorkFlowDefinition definition = config.definitionService().load(code, code, null, false);
		return config.factory().factory(definition);
	}

	@Override
	public WorkFlowInstance getChildInstance(String definitionCode, String taskId, String rootInstanceId) {
		WorkFlowTaskEntity task = config.repository().loadTaskById(taskId, rootInstanceId);
		WorkFlowInstanceEntity parent = config.repository().loadInstance(task.getInstanceId(), rootInstanceId);
		WorkFlowDefinition child = config.definitionService().load(definitionCode, parent.getRootDefinitionCode(), parent.getDefinitionVersion(), false);
		return config.factory().childFactory(child, taskId, rootInstanceId);
	}

	@Override
	public WorkFlowInstance getInstance(String instanceId, String rootInstanceId) {
		WorkFlowInstanceEntity entity = config.repository().loadInstance(instanceId, rootInstanceId);
		if (null == entity) {
			throw new NotFoundWorkFlowInstance(instanceId, rootInstanceId);
		}
		WorkFlowDefinition definition = config.definitionService().load(entity.getDefinitionCode(),
						entity.getRootDefinitionCode(), entity.getDefinitionVersion(), false);
		return config.factory().factory(entity, definition);
	}

	@Override
	public WorkFlowNode getNode(WorkFlowNodeDefinition ndf, String instanceId, String rootInstanceId,
															WorkFlowContextImpl context) {
		return config.factory().factory(ndf, instanceId, rootInstanceId, context);
	}

	@Override
	public WorkFlowNode getNode(String nodeId, String rootInstanceId) {
		WorkFlowRepository repository = config.repository();

		WorkFlowNodeEntity node = repository.loadNode(nodeId, rootInstanceId);
		if (null == node) {
			throw new NotFoundWorkFlowNode(nodeId, rootInstanceId);
		}
		return construct(node.getNodeId(), node.getNodeCode(), node.getVersion(), node.getNodeClass(), node.getConfig(),
						rootInstanceId);
	}

	@Override
	public WorkFlowTask getTask(String taskId, String rootInstanceId) {
		WorkFlowTaskEntity entity = config.repository().loadTaskById(taskId, rootInstanceId);
		return config.factory().factory(entity);
	}

	@Override
	public WorkFlowTask getTask(String event, String node, String rootInstanceId) {
		List<WorkFlowTaskEntity> tasks = config.repository().loadTaskByNodeId(node, rootInstanceId);
		WorkFlowFactory factory = config.factory();
		for (WorkFlowTaskEntity entity : tasks) {
			WorkFlowTask task = factory.factory(entity);
			if (task instanceof EventWorkFlowTask) {
				EventWorkFlowTask eventTask = (EventWorkFlowTask) task;
				if (eventTask.getEventConfig().getWaiting().equals(event)) {
					return eventTask;
				}
			}
		}
		return null;
	}

	/**
	 * 持久化节点数据到数据库
	 */
	@Override
	public void save(WorkFlowNode node) {
		List<WorkFlowTaskEntity> entities = new ArrayList<>();

		WorkFlowNodeEntity dbNode = config.repository().loadNode(node.getNodeId(), node.getRootInstanceId());
		if (null != dbNode) {
			List<WorkFlowTaskEntity> dbTasks = config.repository().loadTaskByNodeId(dbNode.getNodeId(), node.getRootInstanceId());
			Set<String> dbTaskIds = dbTasks.stream().map(WorkFlowTaskEntity::getTaskId).collect(Collectors.toSet());
			for (WorkFlowTask task : node.getTasks()) {
				if (!dbTaskIds.contains(task.getTaskId())) {
					entities.add(toEntity(task, node.getNodeId(), node.getNodeCode()));
				}
			}
			if (!entities.isEmpty()) {
				config.repository().bulkSaveTask(entities);
			}

		} else {
			for (WorkFlowTask task : node.getTasks()) {
				entities.add(toEntity(task, node.getNodeId(), node.getNodeCode()));
			}
			String nodeState = WorkFlowNodeState.DOING.name();
			config.repository().save(toEntity(node, nodeState));
			config.repository().bulkSaveTask(entities);
		}
	}

	private WorkFlowNodeEntity toEntity(WorkFlowNode node, String state) {
		WorkFlowNodeEntity entity = new WorkFlowNodeEntity();
		entity.setNodeId(node.getNodeId());
		entity.setNodeCode(node.getNodeCode());
		entity.setNodeClass(node.getNodeClass());
		entity.setNodeState(state);
		entity.setVersion(node.getVersion());
		entity.setConfig(node.getConfigText());
		entity.setInstanceId(node.getInstanceId());
		entity.setRootInstanceId(node.getRootInstanceId());
		return entity;
	}

	private WorkFlowTaskEntity toEntity(WorkFlowTask task, String nodeId, String nodeCode) {
		WorkFlowTaskEntity entity = new WorkFlowTaskEntity();
		entity.setTaskId(task.getTaskId());
		entity.setTaskClass(task.getTaskClass());
		entity.setTaskState(task.completed() ? WorkFlowTaskState.DONE.name() : WorkFlowTaskState.DOING.name());
		entity.setConfig(task.getTaskConfig());
		entity.setCompleteUser(task.getCompleteUser());
		entity.setCompleteTime(task.getCompleteTime());
		entity.setNodeId(nodeId);
		entity.setNodeCode(nodeCode);
		entity.setInstanceId(task.getInstanceId());
		entity.setRootInstanceId(task.getRootInstanceId());
		return entity;
	}

	@Override
	public void lockNode(String nodeId, String rootInstanceId, Integer version) {
		config.repository().lockNode(nodeId, rootInstanceId, version);
	}

	@Override
	public void changeTaskComplete(WorkFlowTask task) {
		int rows = config.repository().changeTaskComplete(task.getCompleteUser(), task.getCompleteTime(), task.getTaskId(),
						task.getRootInstanceId());
		if (rows != 1) {
			throw new ConcurrencyException("任务状态已完成或不存在"
							+ "。任务ID：" + task.getTaskId()
							+ "，实例ID：" + task.getRootInstanceId());
		}
	}

	@Override
	public void save(WorkFlowInstance instance, String rootInstanceId) {
		WorkFlowInstanceEntity entity = new WorkFlowInstanceEntity();
		entity.setInstanceId(instance.getInstanceId());
		if (instance.done()) {
			entity.setState(InstanceState.DONE.name());
		} else if (instance.stopped()) {
			entity.setState(InstanceState.STOP.name());
		} else {
			entity.setState(InstanceState.DOING.name());
		}
		if (instance instanceof ParentWorkFlowInstance) {
			entity.setRootInstanceId(instance.getInstanceId());
			entity.setParentTaskId(null);
			entity.setScope(InstanceScope.ROOT.name());
		} else if (instance instanceof ChildWorkFlowInstance) {
			entity.setRootInstanceId(((ChildWorkFlowInstance) instance).getRootInstanceId());
			entity.setParentTaskId(((ChildWorkFlowInstance) instance).getParentId());
			entity.setScope(InstanceScope.CHILD.name());
		}
		entity.setDefinitionCode(instance.getDefinitionCode());
		entity.setDefinitionVersion(instance.getDefinitionVersion());
		entity.setRootDefinitionCode(instance.getRootDefinitionCode());
		config.repository().save(entity);
	}

	@Override
	public void changeInstanceComplete(WorkFlowInstance instance, String rootInstanceId) {
		int rows = config.repository().changeInstanceComplete(instance.getInstanceId(), rootInstanceId);
		if (rows != 1) {
			throw new ConcurrencyException("实例状态已完成或不存在"
							+ "，实例ID：" + instance.getInstanceId()
							+ "，根实例ID：" + rootInstanceId);
		}

	}

	@Override
	public void changeNodeComplete(WorkFlowNode node) {
		int rows = config.repository().changeNodeComplete(node.getNodeId(), node.getRootInstanceId(), node.getVersion());
		if (rows != 1) {
			throw new ConcurrencyException("节点状态已完成或不存在"
							+ "，节点编码：" + node.getNodeCode()
							+ "，根实例ID：" + node.getRootInstanceId());
		}
	}

	@Override
	public List<WorkFlowTask> loadActiveTask(String rootInstanceId) {
		List<WorkFlowTaskEntity> entities = config.repository().loadActiveTask(rootInstanceId);
		List<WorkFlowTask> tasks = new ArrayList<>();
		for (WorkFlowTaskEntity entity : entities) {
			tasks.add(config.factory().factory(entity));
		}
		return tasks;
	}

	@Override
	public void changeInstanceStop(WorkFlowInstance instance) {
		int rows = config.repository().changeInstanceStop(instance.getInstanceId(), instance.getInstanceId());
		if (rows != 1) {
			throw new ConcurrencyException("实例状态1⃣已停止或不存在"
							+ "，实例ID：" + instance.getInstanceId());
		}
	}

	public WorkFlowNode getActiveNodeByCode(String code, String rootInstanceId) {
		WorkFlowNodeEntity node = config.repository().loadActiveNodeByCode(code, rootInstanceId);
		if (null == node) {
			return null;
		}
		return construct(node.getNodeId(),
						node.getNodeCode(),
						node.getVersion(),
						node.getNodeClass(),
						node.getConfig(),
						rootInstanceId);
	}

	private WorkFlowNode construct(String nodeId, String nodeCode, Integer version, String nodeClass, String nodeConfig,
																 String rootInstanceId) {
		List<WorkFlowTaskEntity> entities = config.repository().loadTaskByNodeId(nodeId, rootInstanceId);
		if (entities.isEmpty()) {
			throw new NotFoundWorkFlowNode(nodeId, rootInstanceId);
		}

		String instanceId = entities.get(0).getInstanceId();
		List<WorkFlowTask> tasks = new ArrayList<>(entities.size());
		for (WorkFlowTaskEntity entity : entities) {
			tasks.add(config.factory().factory(entity));
		}

		return config.factory().factory(nodeId,
						nodeCode,
						version,
						nodeClass,
						nodeConfig,
						tasks,
						instanceId,
						rootInstanceId);
	}


	public WorkFlowNode getChildActiveNodeByCode(String nodeCode, String taskId, String instanceId) {
		WorkFlowNodeEntity node = config.repository().loadChildActiveNodeByCode(nodeCode, taskId, instanceId);
		if (null == node) {
			return null;
		}
		return construct(node.getNodeId(),
						node.getNodeCode(),
						node.getVersion(),
						node.getNodeClass(),
						node.getConfig(),
						instanceId);
	}
}
