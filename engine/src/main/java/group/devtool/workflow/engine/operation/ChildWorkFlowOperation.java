package group.devtool.workflow.engine.operation;

import group.devtool.workflow.engine.WorkFlowContextImpl;
import group.devtool.workflow.engine.WorkFlowDispatch;
import group.devtool.workflow.engine.exception.OperationException;
import group.devtool.workflow.engine.runtime.ChildWorkFlowNode;
import group.devtool.workflow.engine.runtime.WorkFlowInstance;
import lombok.Getter;

import java.util.List;

@Getter
public class ChildWorkFlowOperation implements WorkFlowOperation {

	private final ChildWorkFlowNode node;

	private final List<WorkFlowInstance> instances;

	private final WorkFlowContextImpl context;

	private final String rootInstanceId;

	public ChildWorkFlowOperation(String rootInstanceId, ChildWorkFlowNode node, List<WorkFlowInstance> instances, WorkFlowContextImpl context) {
		this.rootInstanceId = rootInstanceId;
		this.node = node;
		this.instances = instances;
		this.context = context;
	}

	@Override
	public void operate(WorkFlowDispatch dispatch) throws OperationException {
		WorkFlowOperation[] operations = new WorkFlowOperation[instances.size()];
		for (int i = 0; i < instances.size(); i++) {
			WorkFlowInstance instance = instances.get(i);
			operations[i] = WorkFlowOperation.ofStart(node.getRootInstanceId(), instance, context);
		}
		dispatch.addOperation(operations);
	}
}
