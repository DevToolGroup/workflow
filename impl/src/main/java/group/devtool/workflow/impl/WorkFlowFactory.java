package group.devtool.workflow.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.List;

import group.devtool.workflow.core.ChildWorkFlowNodeDefinition;
import group.devtool.workflow.core.DelayWorkFlowNodeDefinition;
import group.devtool.workflow.core.EndWorkFlowNodeDefinition;
import group.devtool.workflow.core.MergeWorkFlowNodeDefinition;
import group.devtool.workflow.core.StartWorkFlowNodeDefinition;
import group.devtool.workflow.core.TaskWorkFlowNodeDefinition;
import group.devtool.workflow.core.UserWorkFlowNodeDefinition;
import group.devtool.workflow.core.WorkFlowContext;
import group.devtool.workflow.core.WorkFlowDefinition;
import group.devtool.workflow.core.WorkFlowInstance;
import group.devtool.workflow.core.WorkFlowNode;
import group.devtool.workflow.core.WorkFlowNodeDefinition;
import group.devtool.workflow.core.WorkFlowTask;
import group.devtool.workflow.core.WorkFlowTask.WorkFlowTaskState;
import group.devtool.workflow.core.DelayWorkFlowNodeDefinition.DelayTaskConfig;
import group.devtool.workflow.core.MergeWorkFlowNodeDefinition.WorkFlowMergeStrategy;
import group.devtool.workflow.core.MergeWorkFlowTask.MergeWorkFlowTaskConfig;
import group.devtool.workflow.core.TaskWorkFlowNodeDefinition.JavaTaskConfig;
import group.devtool.workflow.core.UserWorkFlowNodeDefinition.WorkFlowUserConfig;
import group.devtool.workflow.core.WorkFlowInstance.WorkFlowInstanceState;
import group.devtool.workflow.core.exception.*;
import group.devtool.workflow.impl.UserWorkFlowTaskImpl.MybatisWorkFlowUserTaskConfig;

public class WorkFlowFactory {

	public WorkFlowFactory() {
	}

	/**
	 * 根据流程定义初始化流程实例
	 *
	 * @param definition 流程定义
	 * @return 流程实例
	 * @throws WorkFlowException
	 */
	public WorkFlowInstance factory(WorkFlowDefinition definition) throws WorkFlowException {
		if (!(definition instanceof WorkFlowDefinitionImpl)) {
			throw new CastWorkFlowClassException("类型不支持, 预期 MybatisWorkFlowDefinition");
		}
		return new ParentWorkFlowInstanceImpl(WorkFlowConfigurationImpl.CONFIG.idSupplier().getInstanceId(),
						definition);
	}

	/**
	 * 根据流程定义初始化子流程实例
	 *
	 * @param definition 流程定义
	 * @param taskId     流程任务ID
	 * @param rootId     根流程实例ID
	 * @return 流程实例
	 * @throws WorkFlowException
	 */
	public WorkFlowInstance childFactory(WorkFlowDefinition definition, String taskId, String rootId)
					throws WorkFlowException {
		if (!(definition instanceof WorkFlowDefinitionImpl)) {
			throw new CastWorkFlowClassException("类型不支持");
		}
		return new ChildWorkFlowInstanceImpl(WorkFlowConfigurationImpl.CONFIG.idSupplier().getInstanceId(), taskId,
						rootId, definition);
	}

	/**
	 * 根据流程实例持久化对象初始化流程实例
	 *
	 * @param entity     流程实例持久化对象
	 * @param definition 流程定义
	 * @return 流程实例
	 * @throws CastWorkFlowClassException
	 */
	public WorkFlowInstance factory(WorkFlowInstanceEntity entity, WorkFlowDefinition definition)
					throws CastWorkFlowClassException {
		if (null == entity) {
			throw new CastWorkFlowClassException("类型不支持");
		}
		if (entity.getParentTaskId() == null || entity.getParentTaskId().isEmpty()) {
			return new ParentWorkFlowInstanceImpl(entity.getId(), entity.getInstanceId(), definition,
							WorkFlowInstanceState.valueOf(entity.getState()));
		}
		return new ChildWorkFlowInstanceImpl(entity.getId(), entity.getInstanceId(), entity.getParentTaskId(),
						entity.getRootInstanceId(), definition,
						WorkFlowInstanceState.valueOf(entity.getState()));
	}

	/**
	 * 根据流程节点定义，初始化流程节点实例
	 *
	 * @param instanceId     流程实例ID
	 * @param rootInstanceId 根流程实例ID
	 * @param context        流程上下文
	 * @return 流程节点实例
	 * @throws WorkFlowException 流程异常
	 */
	public WorkFlowNode factory(WorkFlowNodeDefinition definition, String instanceId, String rootInstanceId,
															WorkFlowContext context) throws WorkFlowException {
		return NodeFactory.apply(definition, instanceId, rootInstanceId, context);
	}

	/**
	 * 根据流程任务初始化流程节点
	 *
	 * @param nodeCode       流程节点编码
	 * @param nodeClass      流程节点类型
	 * @param nodeConfig     流程节点配置
	 * @param instanceId     流程实例ID
	 * @param rootInstanceId 根流程实例ID
	 * @param tasks          流程任务
	 * @return 流程节点实例
	 * @throws InitNodeException 流程节点实例初始化异常
	 * @throws WorkFlowException                  流程运行异常
	 */
	public WorkFlowNode factory(String nodeCode, String nodeClass, byte[] nodeConfig, String instanceId,
															String rootInstanceId, List<WorkFlowTask> tasks)
					throws WorkFlowException {

		return NodeInit.apply(nodeClass, nodeCode, nodeConfig, instanceId, rootInstanceId,
						tasks.toArray(new WorkFlowTask[0]));
	}

	/**
	 * 根据流程任务实体初始化流程任务实例
	 *
	 * @param entity 流程任务实体
	 * @return 流程任务实例
	 * @throws WorkFlowException
	 */
	public WorkFlowTask factory(WorkFlowTaskEntity entity) throws WorkFlowException {
		return TaskFactory.apply(entity);
	}

	private enum NodeFactory {

		START(StartWorkFlowNodeDefinition.class, (definition, instanceId, rootInstanceId, context) -> {
			WorkFlowNode node = new StartWorkFlowNodeImpl(definition, instanceId, rootInstanceId, context);
			return Collections.singletonList(node);
		}),

		USER(UserWorkFlowNodeDefinition.class, (definition, instanceId, rootInstanceId, context) -> {
			WorkFlowNode node = new UserWorkFlowNodeImpl(definition, instanceId, rootInstanceId, context);
			return Collections.singletonList(node);
		}),
		TASK(TaskWorkFlowNodeDefinition.class, (definition, instanceId, rootInstanceId, context) -> {
			WorkFlowNode node = new TaskWorkFlowNodeImpl(definition, instanceId, rootInstanceId, context);
			return Collections.singletonList(node);
		}),
		DELAY(DelayWorkFlowNodeDefinition.class, (definition, instanceId, rootInstanceId, context) -> {
			WorkFlowNode node = new DelayWorkFlowNodeImpl(definition, instanceId, rootInstanceId,
							context);
			return Collections.singletonList(node);
		}),
		CHILD(ChildWorkFlowNodeDefinition.class, (definition, instanceId, rootInstanceId, context) -> {
			WorkFlowNode node = new ChildWorkFlowNodeImpl(definition, instanceId, rootInstanceId, context);
			return Collections.singletonList(node);
		}),
		MERGE(MergeWorkFlowNodeDefinition.class, (definition, instanceId, rootInstanceId, context) -> {
			WorkFlowNode node = new MergeWorkFlowNodeImpl(definition, instanceId, rootInstanceId, context);
			return Collections.singletonList(node);
		}),
		END(EndWorkFlowNodeDefinition.class, (definition, instanceId, rootInstanceId, context) -> {
			WorkFlowNode node = new EndWorkFlowNodeImpl(definition, instanceId, rootInstanceId, context);
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
																		 WorkFlowContext context) throws WorkFlowException {
			NodeFactory[] values = NodeFactory.values();
			for (NodeFactory value : values) {
				if (value.clazz.isAssignableFrom(definition.getClass())) {
					List<WorkFlowNode> nodes = value.factory.init(definition, instanceId,
									rootInstanceId, context);
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
														WorkFlowContext context) throws WorkFlowException;
	}

	private enum TaskFactory {

		START("START", (entity) -> {
			return new StartWorkFlowTaskImpl(entity.getTaskId(),
							entity.getNode().getNodeCode(),
							entity.getInstanceId(),
							entity.getRootInstanceId(),
							WorkFlowTaskState.valueOf(entity.getTaskState()));
		}),
		USER("USER", (entity) -> {
			return new UserWorkFlowTaskImpl(entity.getTaskId(),
							entity.getNode().getNodeCode(),
							deserialize(entity.getConfig(), MybatisWorkFlowUserTaskConfig.class),
							entity.getInstanceId(),
							entity.getRootInstanceId(),
							WorkFlowTaskState.valueOf(entity.getTaskState()));
		}),
		JAVA("JAVA", (entity) -> {
			return new JavaWorkFlowTaskImpl(entity.getTaskId(),
							entity.getNode().getNodeCode(),
							deserialize(entity.getConfig(), JavaTaskConfig.class),
							entity.getInstanceId(),
							entity.getRootInstanceId(),
							WorkFlowTaskState.valueOf(entity.getTaskState()));
		}),
		DELAYJAVA("DELAYJAVA", (entity) -> {
			return new DelayJavaWorkFlowTaskImpl(entity.getTaskId(),
							entity.getNode().getNodeCode(),
							deserialize(entity.getConfig(), DelayTaskConfig.class),
							entity.getInstanceId(),
							entity.getRootInstanceId(),
							WorkFlowTaskState.valueOf(entity.getTaskState()));
		}),
		CHILD("CHILD", (entity) -> {
			return new ChildWorkFlowTaskImpl(entity.getTaskId(),
							entity.getNode().getNodeCode(),
							entity.getInstanceId(),
							entity.getRootInstanceId(),
							WorkFlowTaskState.valueOf(entity.getTaskState()));
		}),
		MERGE("MERGE", (entity) -> {
			MergeWorkFlowTaskConfig taskConfig = deserialize(entity.getConfig(), MergeWorkFlowTaskConfig.class);
			return new MergeWorkFlowTaskImpl(taskConfig.completeBranchCode(), entity.getTaskId(),
							entity.getNode().getNodeCode(),
							entity.getInstanceId(),
							entity.getRootInstanceId(), WorkFlowTaskState.valueOf(entity.getTaskState()));
		}),
		END("END", (entity) -> {
			return new EndWorkFlowTaskImpl(entity.getTaskId(),
							entity.getNode().getNodeCode(),
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

		public static WorkFlowTask apply(WorkFlowTaskEntity entity)
						throws WorkFlowException {
			TaskFactory[] values = TaskFactory.values();
			for (TaskFactory value : values) {
				if (value.taskClass.equals(entity.getTaskClass())) {
					return value.deserialize.apply(entity);
				}
			}
			throw new NotSupportWorkFlowTaskClass("类型不支持");
		}

		public static <T> T deserialize(byte[] config, Class<? extends T> clazz)
						throws DeserializeException {
			try (ByteArrayInputStream bis = new ByteArrayInputStream(config);
					 ObjectInputStream ois = new ObjectInputStream(bis)) {
				Object obj = ois.readObject();
				if (clazz.isAssignableFrom(obj.getClass())) {
					return clazz.cast(obj);
				}
				throw new CastWorkFlowClassException("任务配置反序列化失败，配置类型不匹配");
			} catch (Exception e) {
				throw new DeserializeException("任务配置反序列化失败，异常：" + e.getMessage());
			}
		}
	}

	private interface TaskFunction {

		WorkFlowTask apply(WorkFlowTaskEntity entity) throws WorkFlowException;

	}

	private enum NodeInit {

		START("START", (code, config, instanceId, rootInstanceId, tasks) -> {
			return new StartWorkFlowNodeImpl(code, instanceId, rootInstanceId, tasks);
		}),
		END("END", (code, config, instanceId, rootInstanceId, tasks) -> {
			return new EndWorkFlowNodeImpl(code, instanceId, rootInstanceId, tasks);
		}),
		USER("USER", (code, config, instanceId, rootInstanceId, tasks) -> {
			return new UserWorkFlowNodeImpl(code, deserialize(config, WorkFlowUserConfig.class), instanceId,
							rootInstanceId, tasks);
		}),
		CHILD("CHILD", (code, config, instanceId, rootInstanceId, tasks) -> {
			return new ChildWorkFlowNodeImpl(code, instanceId, rootInstanceId, tasks);
		}),
		TASK("TASK", (code, config, instanceId, rootInstanceId, tasks) -> {
			return new TaskWorkFlowNodeImpl(code, instanceId, rootInstanceId, tasks);
		}),
		DELAY("DELAY", (code, config, instanceId, rootInstanceId, tasks) -> {
			return new DelayWorkFlowNodeImpl(code, instanceId, rootInstanceId, tasks);
		}),
		MERGE("MERGE", (code, config, instanceId, rootInstanceId, tasks) -> {
			return new MergeWorkFlowNodeImpl(code, deserialize(config, WorkFlowMergeStrategy.class), instanceId,
							rootInstanceId, tasks);
		}),
		;

		private final String clazz;

		private final NodeConstruct construct;

		NodeInit(String clazz, NodeConstruct construct) {
			this.clazz = clazz;
			this.construct = construct;
		}

		public static WorkFlowNode apply(String nodeClass, String nodeCode, byte[] nodeConfig, String instanceId,
																		 String rootInstanceId, WorkFlowTask... tasks) throws WorkFlowException {

			for (NodeInit init : NodeInit.values()) {
				if (init.getClazz().equals(nodeClass)) {
					return init.getConstruct().construct(nodeCode, nodeConfig, instanceId, rootInstanceId, tasks);
				}
			}
			throw new NotSupportWorkFlowNodeClass("类型不支持");
		}

		public static <T> T deserialize(byte[] config, Class<T> clazz) throws WorkFlowException {
			try (ByteArrayInputStream bis = new ByteArrayInputStream(config);
					 ObjectInputStream ois = new ObjectInputStream(bis)) {
				Object oo = ois.readObject();
				if (!clazz.isAssignableFrom(oo.getClass())) {
					throw new CastWorkFlowClassException("节点配置反序列化失败，类型不匹配");
				}
				return clazz.cast(oo);
			} catch (ClassNotFoundException | IOException e) {
				throw new DeserializeException("节点配置反序列化异常，异常：" + e.getMessage());
			}
		}

		NodeConstruct getConstruct() {
			return construct;
		}

		String getClazz() {
			return clazz;
		}
	}

	private interface NodeConstruct {

		WorkFlowNode construct(String nodeCode, byte[] nodeConfig, String instanceId, String rootInstanceId,
													 WorkFlowTask... tasks)
						throws WorkFlowException;

	}
}
