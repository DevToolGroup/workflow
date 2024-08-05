package group.devtool.workflow.engine.operate;

import com.sun.org.apache.xerces.internal.impl.dv.xs.SchemaDVFactoryImpl;
import group.devtool.workflow.engine.WorkFlowConfiguration;
import group.devtool.workflow.engine.WorkFlowContextImpl;
import group.devtool.workflow.engine.WorkFlowDispatch;
import group.devtool.workflow.engine.runtime.ChildWorkFlowNode;
import group.devtool.workflow.engine.runtime.WorkFlowInstance;

import java.util.List;

public class ChildWorkFlowOperation implements WorkFlowOperation {
    private final ChildWorkFlowNode node;
    private final List<WorkFlowInstance> instances;
    private final WorkFlowContextImpl context;

    public ChildWorkFlowOperation(ChildWorkFlowNode node, List<WorkFlowInstance> instances, WorkFlowContextImpl context) {
        this.node = node;
        this.instances = instances;
        this.context = context;
    }

    @Override
    public void operate(WorkFlowDispatch dispatch) {
        WorkFlowOperation[] operations = new WorkFlowOperation[instances.size()];
        for (int i = 0; i < instances.size(); i++) {
            WorkFlowInstance instance = instances.get(i);
            operations[i] = WorkFlowOperation.ofStart(node.getRootInstanceId(), instance, context);
        }
        dispatch.addOperation(operations);
    }
}
