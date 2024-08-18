package group.devtool.workflow.engine.operation;

import group.devtool.workflow.engine.*;
import group.devtool.workflow.engine.runtime.WorkFlowInstance;

public class StopWorkFlowOperation implements WorkFlowOperation {

	private final String instanceId;

	private final WorkFlowContextImpl context;

	public StopWorkFlowOperation(String instanceId, WorkFlowContextImpl context) {
		this.instanceId = instanceId;
		this.context = context;
	}

	@Override
	public void operate(WorkFlowDispatch dispatch) {
		WorkFlowConfiguration config = dispatch.getConfig();
		WorkFlowService service = config.service();

		WorkFlowInstance instance = service.getInstance(instanceId, instanceId);

		instance.stop();
		service.changeInstanceStop(instance);
		context.setInstanceId(instanceId);

		dispatch.addCallback(WorkFlowCallback.WorkFlowEvent.STOP, context);
	}
}
