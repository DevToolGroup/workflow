package group.devtool.workflow.engine.operation;

import group.devtool.workflow.engine.*;
import group.devtool.workflow.engine.definition.WorkFlowNodeDefinition;
import group.devtool.workflow.engine.exception.OperationException;
import group.devtool.workflow.engine.runtime.ChildWorkFlowInstance;
import group.devtool.workflow.engine.runtime.ChildWorkFlowNode;
import group.devtool.workflow.engine.runtime.WorkFlowInstance;
import group.devtool.workflow.engine.runtime.WorkFlowNode;

import java.util.ArrayList;
import java.util.List;

public class NextWorkFlowOperation implements WorkFlowOperation {

	private final String rootInstanceId;

	private final String instanceId;

	private final WorkFlowNode node;

	private final WorkFlowContextImpl context;

	public NextWorkFlowOperation(String rootInstanceId, String instanceId, WorkFlowNode node, WorkFlowContextImpl context) {
		this.rootInstanceId = rootInstanceId;
		this.instanceId = instanceId;
		this.node = node;
		this.context = context;
	}

	@Override
	public void operate(WorkFlowDispatch dispatch) throws OperationException {
		if (!node.done()) {
			return;
		}

		WorkFlowConfiguration config = dispatch.getConfig();
		WorkFlowService service = config.service();

		// 持久化节点状态
		service.changeNodeComplete(node);
		service.saveVariable(context);
		node.afterComplete();

		dispatch.addCallback(WorkFlowCallback.WorkFlowEvent.COMPLETE, context);

		WorkFlowInstance instance = service.getInstance(instanceId, rootInstanceId);

		if (instance.done()) {
			doInstanceComplete(dispatch, service, instance);
			return;
		}

		doInstanceContinue(dispatch, instance, service);
	}

	private void doInstanceContinue(WorkFlowDispatch dispatch, WorkFlowInstance instance, WorkFlowService service) {
		List<WorkFlowNode> nodes = instance.next((ndfs, instanceId, rootId, ctx) -> {
			List<WorkFlowNode> ns = new ArrayList<>();
			for (WorkFlowNodeDefinition ndf : ndfs) {
				ns.add(service.getNode(ndf, instanceId, rootId, ctx));
			}
			return ns;
		}, node.getNodeCode(), context);

		WorkFlowOperation[] next = new WorkFlowOperation[nodes.size()];
		for (int i = 0; i < nodes.size(); i++) {
			WorkFlowNode child = nodes.get(i);
			child.beforeComplete();

			service.save(child);
			context.setNode(child);

			dispatch.addCallback(WorkFlowCallback.WorkFlowEvent.CREATED, context);

			if (child instanceof ChildWorkFlowNode) {
				// 启动子流程
				List<WorkFlowInstance> instances = ((ChildWorkFlowNode) child).instances((definitionCode, taskId) ->
								service.getChildInstance(definitionCode, taskId, node.getRootInstanceId())
				);
				next[i] = WorkFlowOperation.ofChild(rootInstanceId, (ChildWorkFlowNode) child, instances, context);
			} else {
				next[i] = WorkFlowOperation.ofNext(rootInstanceId, instanceId, child, context);
			}
		}
		dispatch.addOperation(next);
	}

	private void doInstanceComplete(WorkFlowDispatch dispatch, WorkFlowService service, WorkFlowInstance instance) {
		// 持久化流程实例
		service.changeInstanceComplete(instance, rootInstanceId);
		// 流程结束，回调
		dispatch.addCallback(WorkFlowCallback.WorkFlowEvent.END, context);

		// 判断已完成实例是否是嵌套子流程，如果流程实例是嵌套子流程，则判断父流程节点的状态
		if (instance instanceof ChildWorkFlowInstance) {
			ChildWorkFlowInstance child = (ChildWorkFlowInstance) instance;
			dispatch.addOperation(WorkFlowOperation.ofRun(rootInstanceId, child.getParentId(), context));
		}
	}

}
