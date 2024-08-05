package group.devtool.workflow.engine.operate;

import group.devtool.workflow.engine.WorkFlowCallback;
import group.devtool.workflow.engine.WorkFlowConfiguration;
import group.devtool.workflow.engine.WorkFlowContextImpl;
import group.devtool.workflow.engine.WorkFlowDispatch;
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
        WorkFlowInstance instance = config.service().getInstance(instanceId, instanceId);

        instance.stop();
        config.service().changeInstanceStop(instance);
        context.setInstanceId(instanceId);

        dispatch.addCallback(WorkFlowCallback.WorkFlowEvent.STOP, context);
    }
}
