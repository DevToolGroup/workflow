/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.impl;

import java.util.Collections;
import java.util.List;

import group.devtool.workflow.engine.*;
import group.devtool.workflow.engine.common.InstanceScope;
import group.devtool.workflow.engine.common.InstanceState;
import group.devtool.workflow.engine.exception.*;
import group.devtool.workflow.engine.definition.*;
import group.devtool.workflow.engine.runtime.*;
import group.devtool.workflow.impl.definition.UserWorkFlowNodeDefinitionImpl.UserWorkFlowConfigImpl;
import group.devtool.workflow.impl.definition.EventWorkFlowNodeDefinitionImpl.EventWorkFlowConfigImpl;
import group.devtool.workflow.impl.definition.TaskWorkFlowNodeDefinitionImpl.JavaTaskWorkFlowConfigImpl;
import group.devtool.workflow.impl.definition.DelayWorkFlowNodeDefinitionImpl.JavaDelayWorkFlowConfigImpl;
import group.devtool.workflow.impl.definition.ChildWorkFlowNodeDefinitionImpl.ChildWorkFlowConfigImpl;
import group.devtool.workflow.impl.runtime.*;
import group.devtool.workflow.impl.runtime.UserWorkFlowTaskImpl.UserWorkFlowTaskConfigImpl;
import group.devtool.workflow.impl.runtime.EventWorkFlowTaskImpl.EventWorkFlowTaskConfigImpl;
import group.devtool.workflow.impl.runtime.JavaWorkFlowTaskImpl.JavaTaskWorkFlowTaskConfigImpl;
import group.devtool.workflow.impl.runtime.JavaDelayWorkFlowTaskImpl.JavaDelayWorkFlowTaskConfigImpl;
import group.devtool.workflow.engine.runtime.WorkFlowTask.WorkFlowTaskState;
import group.devtool.workflow.impl.entity.WorkFlowInstanceEntity;
import group.devtool.workflow.impl.entity.WorkFlowTaskEntity;

import static group.devtool.workflow.engine.common.JacksonUtils.deserialize;

/**
 * 流程实体构造工厂
 */
public class WorkFlowFactory {

	private final WorkFlowConfigurationImpl config;

	public WorkFlowFactory() {
		this.config = WorkFlowConfigurationImpl.CONFIG;
	}

	/**
	 * 根据流程定义初始化流程实例
	 *
	 * @param definition 流程定义
	 * @return 流程实例
	 */
	public WorkFlowInstance factory(WorkFlowDefinition definition) {
		return new ParentWorkFlowInstanceImpl(config.idSupplier().getInstanceId(), definition);
	}

	/**
	 * 根据流程定义初始化子流程实例
	 *
	 * @param definition 流程定义
	 * @param taskId     流程任务ID
	 * @param rootId     根流程实例ID
	 * @return 流程实例
	 */
	public WorkFlowInstance childFactory(WorkFlowDefinition definition, String taskId, String rootId) {
		return new ChildWorkFlowInstanceImpl(config.idSupplier().getInstanceId(),
						taskId,
						rootId,
						definition);
	}

	/**
	 * 根据流程实例持久化对象初始化流程实例
	 *
	 * @param entity     流程实例持久化对象
	 * @param definition 流程定义
	 * @return 流程实例
	 */
	public WorkFlowInstance factory(WorkFlowInstanceEntity entity, WorkFlowDefinition definition) {
		if (entity.getScope().equals(InstanceScope.ROOT.name())) {
			return new ParentWorkFlowInstanceImpl(entity.getId(), entity.getInstanceId(), definition,
							InstanceState.valueOf(entity.getState()));
		} else if (entity.getScope().equals(InstanceScope.CHILD.name())) {
			return new ChildWorkFlowInstanceImpl(entity.getId(), entity.getInstanceId(), entity.getParentTaskId(),
							entity.getRootInstanceId(), definition,
							InstanceState.valueOf(entity.getState()));
		} else {
			throw new NotSupportWorkFlowInstanceScope("实例SCOPE不支持，实例ID：" + entity.getInstanceId() + "，SCOPE：" + entity.getScope());
		}

	}

	/**
	 * 根据流程节点定义，初始化流程节点实例
	 *
	 * @param instanceId     流程实例ID
	 * @param rootInstanceId 根流程实例ID
	 * @param context        流程上下文
	 * @return 流程节点实例
	 */
	public WorkFlowNode factory(WorkFlowNodeDefinition definition, String instanceId, String rootInstanceId,
															WorkFlowContextImpl context) {
		return NodeFactory.apply(definition, instanceId, rootInstanceId, config, context);
	}

	/**
	 * 根据流程任务初始化流程节点
	 *
	 * @param nodeId         流程节点ID
	 * @param nodeCode       流程节点编码
	 * @param version        流程节点版本
	 * @param nodeClass      流程节点类型
	 * @param nodeConfig     流程节点配置
	 * @param tasks          流程任务
	 * @param instanceId     流程实例ID
	 * @param rootInstanceId 根流程实例ID
	 * @return 流程节点实例
	 */
	public WorkFlowNode factory(String nodeId, String nodeCode, Integer version, String nodeClass, String nodeConfig,
															List<WorkFlowTask> tasks,
															String instanceId, String rootInstanceId) {

		return NodeInit.apply(nodeClass, nodeId, nodeCode, version, nodeConfig, instanceId, rootInstanceId,
						tasks.toArray(new WorkFlowTask[0]));
	}

	/**
	 * 根据流程任务实体初始化流程任务实例
	 *
	 * @param task 流程任务实体
	 * @return 流程任务实例
	 */
	public WorkFlowTask factory(WorkFlowTaskEntity task) {
		return TaskFactory.apply(task);
	}

	private enum NodeFactory {
		// 开始节点
		START(StartWorkFlowNodeDefinition.class, (definition, instanceId, rootInstanceId, config, context) -> {
			String nodeId = WorkFlowConfigurationImpl.CONFIG.idSupplier().getNodeId();
			WorkFlowNode node = new StartWorkFlowNodeImpl(nodeId, definition, instanceId, rootInstanceId, context);
			return Collections.singletonList(node);
		}),
		// 用户节点
		USER(UserWorkFlowNodeDefinition.class, (definition, instanceId, rootInstanceId, config, context) -> {
			String nodeId = WorkFlowConfigurationImpl.CONFIG.idSupplier().getNodeId();
			WorkFlowNode node = new UserWorkFlowNodeImpl(nodeId, definition, instanceId, rootInstanceId, context);
			return Collections.singletonList(node);
		}),
		// 事件节点
		EVENT(EventWorkFlowNodeDefinition.class, (definition, instanceId, rootInstanceId, config, context) -> {
			String nodeId = WorkFlowConfigurationImpl.CONFIG.idSupplier().getNodeId();
			WorkFlowNode node = new EventWorkFlowNodeImpl(nodeId, definition, instanceId, rootInstanceId, context);
			return Collections.singletonList(node);
		}),
		// 任务节点
		TASK(TaskWorkFlowNodeDefinition.class, (definition, instanceId, rootInstanceId, config, context) -> {
			String nodeId = WorkFlowConfigurationImpl.CONFIG.idSupplier().getNodeId();
			WorkFlowNode node = new TaskWorkFlowNodeImpl(nodeId, definition, instanceId, rootInstanceId, context);
			return Collections.singletonList(node);
		}),
		// 延时任务节点
		DELAY(DelayWorkFlowNodeDefinition.class, (definition, instanceId, rootInstanceId, config, context) -> {
			String nodeId = WorkFlowConfigurationImpl.CONFIG.idSupplier().getNodeId();
			WorkFlowNode node = new DelayWorkFlowNodeImpl(nodeId, definition, instanceId, rootInstanceId,
							context);
			return Collections.singletonList(node);
		}),
		// 子流程节点
		CHILD(ChildWorkFlowNodeDefinition.class, (definition, instanceId, rootInstanceId, config, context) -> {
			String nodeId = WorkFlowConfigurationImpl.CONFIG.idSupplier().getNodeId();
			WorkFlowNode node = new ChildWorkFlowNodeImpl(nodeId, definition, instanceId, rootInstanceId, context);
			return Collections.singletonList(node);
		}),
		// 结束节点
		END(EndWorkFlowNodeDefinition.class, (definition, instanceId, rootInstanceId, config, context) -> {
			String nodeId = WorkFlowConfigurationImpl.CONFIG.idSupplier().getNodeId();
			WorkFlowNode node = new EndWorkFlowNodeImpl(nodeId, definition, instanceId, rootInstanceId, context);
			return Collections.singletonList(node);
		}),
		;

		private final Class<? extends WorkFlowNodeDefinition> clazz;

		private final NodeFunction factory;

		NodeFactory(Class<? extends WorkFlowNodeDefinition> clazz, NodeFunction factory) {
			this.clazz = clazz;
			this.factory = factory;
		}

		public static WorkFlowNode apply(WorkFlowNodeDefinition definition, String instanceId, String rootInstanceId,
																		 WorkFlowConfigurationImpl config, WorkFlowContextImpl context) {
			NodeFactory[] values = NodeFactory.values();
			for (NodeFactory value : values) {
				if (value.clazz.isAssignableFrom(definition.getClass())) {
					List<WorkFlowNode> nodes = value.factory.init(definition, instanceId, rootInstanceId, config, context);
					return nodes.get(0);
				}
			}
			throw new NotSupportWorkFlowNodeClass("类型不支持");
		}
	}

	private interface NodeFunction {
		List<WorkFlowNode> init(WorkFlowNodeDefinition definition,
														String instanceId,
														String rootInstanceId,
														WorkFlowConfiguration config,
														WorkFlowContextImpl context);
	}

	private enum TaskFactory {

		START("START", (entity) -> {
			return new StartWorkFlowTaskImpl(entity.getTaskId(),
							entity.getNodeId(),
							entity.getNodeCode(),
							entity.getInstanceId(),
							entity.getRootInstanceId(),
							WorkFlowTaskState.valueOf(entity.getTaskState()));
		}),
		USER("USER", (entity) -> {
			return new UserWorkFlowTaskImpl(entity.getTaskId(),
							entity.getNodeId(),
							entity.getNodeCode(),
							deserialize(entity.getConfig(), UserWorkFlowTaskConfigImpl.class),
							entity.getInstanceId(),
							entity.getRootInstanceId(),
							WorkFlowTaskState.valueOf(entity.getTaskState()));
		}),
		EVENT("EVENT", (entity) -> {
			return new EventWorkFlowTaskImpl(entity.getTaskId(),
							entity.getNodeId(),
							entity.getNodeCode(),
							deserialize(entity.getConfig(), EventWorkFlowTaskConfigImpl.class),
							entity.getInstanceId(),
							entity.getRootInstanceId(),
							WorkFlowTaskState.valueOf(entity.getTaskState()));
		}),
		JAVA("JAVA", (entity) -> {
			return new JavaWorkFlowTaskImpl(entity.getTaskId(),
							entity.getNodeId(),
							entity.getNodeCode(),
							deserialize(entity.getConfig(), JavaTaskWorkFlowTaskConfigImpl.class),
							entity.getInstanceId(),
							entity.getRootInstanceId(),
							WorkFlowTaskState.valueOf(entity.getTaskState()));
		}),
		DELAYJAVA("DELAYJAVA", (entity) -> {
			return new JavaDelayWorkFlowTaskImpl(entity.getTaskId(),
							entity.getNodeId(),
							entity.getNodeCode(),
							deserialize(entity.getConfig(), JavaDelayWorkFlowTaskConfigImpl.class),
							entity.getInstanceId(),
							entity.getRootInstanceId(),
							WorkFlowTaskState.valueOf(entity.getTaskState()));
		}),
		CHILD("CHILD", (entity) -> {
			return new ChildWorkFlowTaskImpl(entity.getTaskId(),
							entity.getNodeId(),
							entity.getNodeCode(),
							entity.getInstanceId(),
							entity.getRootInstanceId(),
							WorkFlowTaskState.valueOf(entity.getTaskState()));
		}),
		END("END", (entity) -> {
			return new EndWorkFlowTaskImpl(entity.getTaskId(),
							entity.getNodeId(),
							entity.getNodeCode(),
							WorkFlowTaskState.valueOf(entity.getTaskState()),
							entity.getInstanceId(),
							entity.getRootInstanceId());

		}),

		;

		private final String taskClass;

		private final TaskFunction deserialize;

		TaskFactory(String taskClass, TaskFunction deserialize) {
			this.taskClass = taskClass;
			this.deserialize = deserialize;
		}

		public static WorkFlowTask apply(WorkFlowTaskEntity task) throws NotSupportWorkFlowTaskClass {
			TaskFactory[] values = TaskFactory.values();
			for (TaskFactory value : values) {
				if (value.taskClass.equals(task.getTaskClass())) {
					return value.deserialize.apply(task);
				}
			}
			throw new NotSupportWorkFlowTaskClass("类型不支持");
		}

	}

	private interface TaskFunction {

		WorkFlowTask apply(WorkFlowTaskEntity task);

	}

	private enum NodeInit {

		START("START", (nodeId, nodeCode, version, config, instanceId, rootInstanceId, tasks) -> {
			return new StartWorkFlowNodeImpl(nodeId, nodeCode, version, null, instanceId, rootInstanceId, tasks);
		}),
		END("END", (nodeId, nodeCode, version, config, instanceId, rootInstanceId, tasks) -> {
			return new EndWorkFlowNodeImpl(nodeId, nodeCode, version, null, instanceId, rootInstanceId, tasks);
		}),
		USER("USER", (nodeId, nodeCode, version, config, instanceId, rootInstanceId, tasks) -> {
			return new UserWorkFlowNodeImpl(nodeId, nodeCode, version, deserialize(config, UserWorkFlowConfigImpl.class), instanceId,
							rootInstanceId, tasks);
		}),
		EVENT("EVENT", (nodeId, nodeCode, version, config, instanceId, rootInstanceId, tasks) -> {
			return new EventWorkFlowNodeImpl(nodeId, nodeCode, version, deserialize(config, EventWorkFlowConfigImpl.class), instanceId,
							rootInstanceId, tasks);
		}),
		CHILD("CHILD", (nodeId, nodeCode, version, config, instanceId, rootInstanceId, tasks) -> {
			return new ChildWorkFlowNodeImpl(nodeId, nodeCode, version, deserialize(config, ChildWorkFlowConfigImpl.class), instanceId, rootInstanceId, tasks);
		}),
		TASK("TASK", (nodeId, nodeCode, version, config, instanceId, rootInstanceId, tasks) -> {
			return new TaskWorkFlowNodeImpl(nodeId, nodeCode, version, deserialize(config, JavaTaskWorkFlowConfigImpl.class), instanceId, rootInstanceId, tasks);
		}),
		DELAY("DELAY", (nodeId, nodeCode, version, config, instanceId, rootInstanceId, tasks) -> {
			return new DelayWorkFlowNodeImpl(nodeId, nodeCode, version, deserialize(config, JavaDelayWorkFlowConfigImpl.class), instanceId, rootInstanceId, tasks);
		}),
		;

		private final String clazz;

		private final NodeConstruct construct;

		NodeInit(String clazz, NodeConstruct construct) {
			this.clazz = clazz;
			this.construct = construct;
		}

		public static WorkFlowNode apply(String nodeClass, String nodeId, String nodeCode, Integer version, String nodeConfig,
																		 String instanceId, String rootInstanceId, WorkFlowTask... tasks) {
			for (NodeInit init : NodeInit.values()) {
				if (init.getClazz().equals(nodeClass)) {
					return init.getConstruct().construct(nodeId, nodeCode, version, nodeConfig, instanceId, rootInstanceId, tasks);
				}
			}
			throw new NotSupportWorkFlowNodeClass("类型不支持");
		}

		NodeConstruct getConstruct() {
			return construct;
		}

		String getClazz() {
			return clazz;
		}
	}


	private interface NodeConstruct {

		WorkFlowNode construct(String nodeId,
													 String nodeCode,
													 Integer version,
													 String nodeConfig,
													 String instanceId,
													 String rootInstanceId,
													 WorkFlowTask... tasks) throws DeserializeException;

	}
}
