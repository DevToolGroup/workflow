/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.engine;

import java.util.ArrayList;
import java.util.List;

import group.devtool.workflow.engine.WorkFlowCallback.WorkFlowEvent;
import group.devtool.workflow.engine.definition.WorkFlowDefinition;
import group.devtool.workflow.engine.definition.WorkFlowNodeDefinition;
import group.devtool.workflow.engine.exception.*;
import group.devtool.workflow.engine.runtime.*;

/**
 * 流程引擎主要职责：
 * 1. 完成流程定义的部署及卸载
 * 2. 完成流程的启动、执行、终止
 * 3. 流程事件的触发
 */
public final class WorkFlowEngine {

	private final WorkFlowConfiguration config;

	public WorkFlowEngine(WorkFlowConfiguration config) {
		this.config = config;
	}

	/**
	 * 部署流程定义
	 *
	 * @param definition 流程定义
	 */
	public void deploy(WorkFlowDefinition definition) throws WorkFlowException {
		config.dbTransaction().doInTransaction(() -> {
			config.definitionService().deploy(definition);
			return null;
		});
	}

	/**
	 * 加载流程定义
	 *
	 * @param code    流程定义编码
	 * @param version 流程定义版本
	 * @return 流程定义
	 */
	public WorkFlowDefinition load(String code, Integer version) throws WorkFlowException {
		return config.dbTransaction().doInTransaction(() -> {
			return config.definitionService().load(code, code, version, true);
		});
	}

	/**
	 * 卸载流程定义
	 *
	 * @param code 流程定义编码
	 */
	public void undeploy(String code) throws WorkFlowException {
		config.dbTransaction().doInTransaction(() -> {
			config.definitionService().undeploy(code);
			return null;
		});
	}

	/**
	 * 加载流程图以及流程运行节点信息
	 *
	 * @param rootInstanceId 根流程实例ID
	 * @return 流程图
	 */
	public List<WorkFlowTask> loadTask(String rootInstanceId) throws WorkFlowException {
		return config.dbTransaction().doInTransaction(() -> {
			return config.service().loadActiveTask(rootInstanceId);
		});
	}

	/**
	 * 根据流程定义编码，启动流程引擎，初始化流程实例
	 *
	 * @param code      流程定义编码
	 * @param variables 流程启动参数
	 * @return 操作ID
	 */
	public String start(String code, WorkFlowVariable... variables) throws WorkFlowException {
		return config.dbTransaction().doInTransaction(() -> {
				WorkFlowService service = config.service();
				WorkFlowInstance instance = service.getInstance(code);

				// 初始化流程上下文
				WorkFlowContextImpl context = service.getContext(instance.getInstanceId(), variables);
			try {
				// 启动流程实例
				start(instance.getInstanceId(), instance, context);
				return instance.getInstanceId();
			} catch (Exception e) {
				config.callback().callback(WorkFlowEvent.EXCEPTION, context);
				throw e;
			}
		});
	}

	private void start(String rootInstanceId, WorkFlowInstance instance, WorkFlowContextImpl context) {
		WorkFlowService service = config.service();

		// 启动实例
		WorkFlowNode node = instance.start((ndfs, id, rootId, ctx) -> {
			List<WorkFlowNode> nodes = new ArrayList<>();
			for (WorkFlowNodeDefinition ndf : ndfs) {
				nodes.add(service.getNode(ndf, id, rootId, ctx));
			}
			return nodes;
		}, context);

		// 节点前置操作
		node.beforeComplete();

		// 上下文设置
		context.setNode(node);

		// 持久化节点、实例、上下文变量
		service.save(instance, rootInstanceId);
		service.save(node);
		service.changeNodeComplete(node);
		service.saveVariable(context);

		// 节点后置操作
		node.afterComplete();

		// 回调
		doCallback(WorkFlowEvent.START, context);

		// 流程继续流转
		roll(rootInstanceId, instance, node.getNodeCode(), context);
	}

	/**
	 * 根据任务ID执行任务。
	 * 如果任务完成判断流程节点是否完成，如果完成，递归流转。
	 *
	 * @param variables      流转变量
	 * @param taskId         流程任务ID
	 * @param rootInstanceId 根流程实例ID
	 */
	public void run(String rootInstanceId, String taskId, WorkFlowVariable... variables) throws WorkFlowException {
		config.dbTransaction().doInTransaction(() -> {
			// 初始化上下文
			WorkFlowContextImpl context = config.service().getContext(rootInstanceId, variables);
			try {
				// 运行流程引擎
				run(rootInstanceId, taskId, context);
			} catch (Exception e) {
				config.callback().callback(WorkFlowEvent.EXCEPTION, context);
				throw e;
			}

			return true;
		});
	}

	/**
	 * 根据事件执行任务。
	 * 如果任务完成判断流程节点是否完成，如果完成，递归流转。
	 *
	 * @param rootInstanceId 根流程实例ID
	 * @param node           节点编码
	 * @param event          事件名称
	 * @param variables      流转变量
	 * @throws NotFoundWorkFlowTask 流程异常
	 */
	public void trigger(String rootInstanceId, String node, String event, WorkFlowVariable... variables) throws NotFoundWorkFlowTask {
		WorkFlowService service = config.service();

		// 初始化上下文 -- 上下文在并发条件下
		WorkFlowContextImpl context = service.getContext(rootInstanceId, variables);
		WorkFlowTask task = service.getTask(event, node, rootInstanceId);
		if (null == task) {
			throw new NotFoundWorkFlowTask("事件任务未找到。事件编码：" + event + " ，节点编码：" + node + "，流程实例ID：" + rootInstanceId);
		}
		// 运行流程引擎
		run(rootInstanceId, task.getTaskId(), context);
	}

	private void run(String rootInstanceId, String taskId, WorkFlowContextImpl context) {
		WorkFlowNode node = changeTaskComplete(rootInstanceId, taskId, context);
		if (!node.done()) {
			return;
		}
		// 查询当前任务对应的流程实例
		WorkFlowInstance instance = config.service().getInstance(node.getInstanceId(), rootInstanceId);

		// 节点流转
		roll(rootInstanceId, instance, node.getNodeCode(), context);
	}

	private WorkFlowNode changeTaskComplete(String rootInstanceId, String taskId, WorkFlowContextImpl context) {
		WorkFlowTask task = config.service().getTask(taskId, rootInstanceId);
		// 任务操作
		task.complete(context);
		// 流程上下文中设置当前节点
		context.setTask(task);

		config.service().changeTaskComplete(task);
		config.service().saveVariable(context);

		WorkFlowNode latest = config.service().getNode(task.getNodeId(), task.getRootInstanceId());

		if (latest.done()) {
			config.service().changeNodeComplete(latest);
			latest.afterComplete();
			doCallback(WorkFlowEvent.COMPLETE, context);
		} else {
			// 加锁，保证并发操作一致
			config.service().lockNode(task.getNodeId(), task.getRootInstanceId(), latest.getVersion());
		}
		return latest;
	}


	private void roll(String rootInstanceId, WorkFlowInstance instance, String nodeCode, WorkFlowContextImpl context) {
		// 节点流转
		next(instance, nodeCode, context);
		// 判断流程实例是否已完成，如果没完成则直接返回
		if (!instance.done()) {
			return;
		}
		// 持久化流程实例
		config.service().changeInstanceComplete(instance, rootInstanceId);
		// 流程结束，回调
		doCallback(WorkFlowEvent.END, context);

		// 判断已完成实例是否是嵌套子流程，如果流程实例是嵌套子流程，则判断父流程节点的状态
		if (instance instanceof ChildWorkFlowInstance) {
			ChildWorkFlowInstance child = (ChildWorkFlowInstance) instance;
			run(rootInstanceId, child.getParentId(), context);
		}
	}

	private void next(WorkFlowInstance instance, String nodeCode, WorkFlowContextImpl context) {
		// 初始化待执行任务节点列表
		List<WorkFlowNode> nodes = instance.next((ndfs, instanceId, rootId, ctx) -> {
			List<WorkFlowNode> ns = new ArrayList<>();
			for (WorkFlowNodeDefinition ndf : ndfs) {
				ns.add(config.service().getNode(ndf, instanceId, rootId, ctx));
			}
			return ns;
		}, nodeCode, context);


		// 保存流程节点实例，并触发节点创建事件
		for (WorkFlowNode node : nodes) {
			node.beforeComplete();

			config.service().save(node);
			context.setNode(node);

			doCallback(WorkFlowEvent.CREATED, context);

			if (node instanceof ChildWorkFlowNode) {
				doChild((ChildWorkFlowNode) node, context);
			}

			if (!node.done()) {
				return;
			}

			config.service().changeNodeComplete(node);
			config.service().saveVariable(context);

			node.afterComplete();

			doCallback(WorkFlowEvent.COMPLETE, context);

			next(instance, node.getNodeCode(), context);
		}
	}

	private void doChild(ChildWorkFlowNode node, WorkFlowContextImpl context) {
		// 启动子流程
		List<WorkFlowInstance> instances = node.instances((definitionCode, taskId) ->
						config.service().getChildInstance(definitionCode, taskId, node.getRootInstanceId())
		);
		for (WorkFlowInstance childInstance : instances) {
			// 启动子流程
			start(node.getRootInstanceId(), childInstance, context);
		}
	}

	private void doCallback(WorkFlowEvent event, WorkFlowContextImpl context) {
		config.callback().callback(event, context);
	}

	/**
	 * 终止流程实例
	 *
	 * @param instanceId 流程
	 */
	public void stop(String instanceId) throws WorkFlowException {
		WorkFlowContextImpl context = config.dbTransaction().doInTransaction(() -> {
			WorkFlowInstance instance = config.service().getInstance(instanceId, instanceId);
			// 终止流程
			instance.stop();
			// 开启事务，持久化流程实例状态
			config.service().changeInstanceStop(instance);
			WorkFlowContextImpl ctx = config.service().getContext(instanceId);
			ctx.setInstanceId(instanceId);
			return ctx;
		});
		config.callback().callback(WorkFlowEvent.STOP, context);
	}

}
