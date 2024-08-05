package group.devtool.workflow.engine.operate;

import group.devtool.workflow.engine.WorkFlowCallback;
import group.devtool.workflow.engine.WorkFlowConfiguration;
import group.devtool.workflow.engine.WorkFlowContextImpl;
import group.devtool.workflow.engine.WorkFlowDispatch;
import group.devtool.workflow.engine.definition.WorkFlowNodeDefinition;
import group.devtool.workflow.engine.runtime.ChildWorkFlowInstance;
import group.devtool.workflow.engine.runtime.ChildWorkFlowNode;
import group.devtool.workflow.engine.runtime.WorkFlowInstance;
import group.devtool.workflow.engine.runtime.WorkFlowNode;

import java.util.ArrayList;
import java.util.List;

public class NextWorkFlowOperation implements WorkFlowOperation {
    private final String rootInstanceId;

    private final WorkFlowInstance instance;

    private final WorkFlowNode node;

    private final WorkFlowContextImpl context;

    public NextWorkFlowOperation(String rootInstanceId, WorkFlowInstance instance, WorkFlowNode node, WorkFlowContextImpl context) {
        this.rootInstanceId = rootInstanceId;
        this.instance = instance;
        this.node = node;
        this.context = context;
    }

    @Override
    public void operate(WorkFlowDispatch dispatch) {
        WorkFlowConfiguration config = dispatch.getConfig();
        if (!node.done()) {
            return;
        }
        // 持久化节点状态
        doNodeCompleted(dispatch, config);

        if (instance.done()) {
            // 持久化流程实例
            config.service().changeInstanceComplete(instance, rootInstanceId);
            // 流程结束，回调
            dispatch.addCallback(WorkFlowCallback.WorkFlowEvent.END, context);

            // 判断已完成实例是否是嵌套子流程，如果流程实例是嵌套子流程，则判断父流程节点的状态
            if (instance instanceof ChildWorkFlowInstance) {
                ChildWorkFlowInstance child = (ChildWorkFlowInstance) instance;
                dispatch.addOperation(WorkFlowOperation.ofRun(rootInstanceId, child.getParentId(), context));
            }

        } else {
            // 继续流转
            dispatch.addOperation(doNext(dispatch, config));
        }
    }

    private WorkFlowOperation[] doNext(WorkFlowDispatch dispatch, WorkFlowConfiguration config) {
        // 初始化待执行任务节点列表
        List<WorkFlowNode> nodes = instance.next((ndfs, instanceId, rootId, ctx) -> {
            List<WorkFlowNode> ns = new ArrayList<>();
            for (WorkFlowNodeDefinition ndf : ndfs) {
                ns.add(config.service().getNode(ndf, instanceId, rootId, ctx));
            }
            return ns;
        }, node.getNodeCode(), context);

        WorkFlowOperation[] next = new WorkFlowOperation[nodes.size()];

        for (int i = 0; i < nodes.size(); i++) {
            WorkFlowNode child = nodes.get(i);
            doNodeStarted(dispatch, config, child);
            if (child instanceof ChildWorkFlowNode) {
                next[i] = doChild((ChildWorkFlowNode) child, context, config);
            } else {
                next[i] = WorkFlowOperation.ofNext(rootInstanceId, instance, child, context);
            }
        }
        return next;
    }

    private void doNodeStarted(WorkFlowDispatch dispatch, WorkFlowConfiguration config, WorkFlowNode node) {
        node.beforeComplete();

        config.service().save(node);
        context.setNode(node);

        dispatch.addCallback(WorkFlowCallback.WorkFlowEvent.CREATED, context);
    }

    private void doNodeCompleted(WorkFlowDispatch dispatch, WorkFlowConfiguration config) {
        config.service().changeNodeComplete(node);
        config.service().saveVariable(context);

        node.afterComplete();

        dispatch.addCallback(WorkFlowCallback.WorkFlowEvent.COMPLETE, context);
    }

    private WorkFlowOperation doChild(ChildWorkFlowNode node, WorkFlowContextImpl context, WorkFlowConfiguration config) {
        // 启动子流程
        List<WorkFlowInstance> instances = node.instances((definitionCode, taskId) ->
                config.service().getChildInstance(definitionCode, taskId, node.getRootInstanceId())
        );
        return WorkFlowOperation.ofChild(node, instances, context);
    }
}
