package group.devtool.workflow.engine.operate;

import group.devtool.workflow.engine.*;
import group.devtool.workflow.engine.runtime.WorkFlowInstance;
import group.devtool.workflow.engine.runtime.WorkFlowNode;
import group.devtool.workflow.engine.runtime.WorkFlowTask;

public class RunWorkFlowOperation implements WorkFlowOperation {

    private final String rootInstanceId;
    private final String taskId;

    private final WorkFlowContextImpl context;

    public RunWorkFlowOperation(String rootInstanceId, String taskId, WorkFlowContextImpl context) {
        this.rootInstanceId = rootInstanceId;
        this.taskId = taskId;
        this.context = context;
    }

    @Override
    public void operate(WorkFlowDispatch dispatch) {
        WorkFlowConfiguration config = dispatch.getConfig();

        WorkFlowNode node = changeTaskComplete(rootInstanceId, taskId, context, config);
        if (!node.done()) {
            return;
        }
        dispatch.addCallback(WorkFlowCallback.WorkFlowEvent.COMPLETE, context);
        WorkFlowInstance instance = config.service().getInstance(node.getInstanceId(), rootInstanceId);

        dispatch.addOperation(WorkFlowOperation.ofNext(rootInstanceId, instance, node, context));
    }

    private WorkFlowNode changeTaskComplete(String rootInstanceId,
                                            String taskId,
                                            WorkFlowContextImpl context,
                                            WorkFlowConfiguration config) {

        WorkFlowTask task = config.service().getTask(taskId, rootInstanceId);
        // 任务操作
        task.complete(context);
        // 流程上下文中设置当前节点
        context.setTask(task);

        config.service().changeTaskComplete(task);
        config.service().saveVariable(context);

        WorkFlowNode latest = config.service().getNode(task.getNodeId(), task.getRootInstanceId());

        // 加锁，保证并发操作一致
        config.service().lockNode(task.getNodeId(), task.getRootInstanceId(), latest.getVersion());

        if (latest.done()) {
            config.service().changeNodeComplete(latest);
            latest.afterComplete();
        }

        return latest;
    }
}
