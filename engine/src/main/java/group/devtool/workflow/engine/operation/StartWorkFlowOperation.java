package group.devtool.workflow.engine.operation;

import group.devtool.workflow.engine.*;
import group.devtool.workflow.engine.definition.WorkFlowNodeDefinition;
import group.devtool.workflow.engine.exception.OperationException;
import group.devtool.workflow.engine.runtime.WorkFlowInstance;
import group.devtool.workflow.engine.runtime.WorkFlowNode;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class StartWorkFlowOperation implements WorkFlowOperation {

	private final String rootInstanceId;

	private final WorkFlowInstance instance;

	private final WorkFlowContextImpl context;

	public StartWorkFlowOperation(String rootInstanceId, WorkFlowInstance instance, WorkFlowContextImpl context) {
		this.rootInstanceId = rootInstanceId;
		this.instance = instance;
		this.context = context;
	}

	@Override
	public void operate(WorkFlowDispatch dispatch) throws OperationException {
		WorkFlowConfiguration config = dispatch.getConfig();

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
		dispatch.addCallback(WorkFlowCallback.WorkFlowEvent.START, context);

		// 流程继续流转
		dispatch.addOperation(WorkFlowOperation.ofNext(rootInstanceId, instance.getInstanceId(), node, context));
	}
}
