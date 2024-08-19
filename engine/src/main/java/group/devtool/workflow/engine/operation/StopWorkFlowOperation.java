package group.devtool.workflow.engine.operation;

import group.devtool.workflow.engine.*;
import group.devtool.workflow.engine.runtime.WorkFlowInstance;
import lombok.Getter;

@Getter
public class StopWorkFlowOperation implements WorkFlowOperation {

	private final String rootInstanceId;

	private final WorkFlowContextImpl context;

	public StopWorkFlowOperation(String instanceId, WorkFlowContextImpl context) {
		this.rootInstanceId = instanceId;
		this.context = context;
	}

	@Override
	public void operate(WorkFlowDispatch dispatch) {
		WorkFlowConfiguration config = dispatch.getConfig();
		WorkFlowService service = config.service();

		WorkFlowInstance instance = service.getInstance(rootInstanceId, rootInstanceId);

		instance.stop();
		service.changeInstanceStop(instance);
		context.setInstanceId(rootInstanceId);

		dispatch.addCallback(WorkFlowCallback.WorkFlowEvent.STOP, context);
	}
}
