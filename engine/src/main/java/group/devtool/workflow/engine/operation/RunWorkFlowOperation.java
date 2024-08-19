package group.devtool.workflow.engine.operation;

import group.devtool.workflow.engine.*;
import group.devtool.workflow.engine.exception.OperationException;
import group.devtool.workflow.engine.runtime.WorkFlowNode;
import group.devtool.workflow.engine.runtime.WorkFlowTask;
import lombok.Getter;

@Getter
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
	public void operate(WorkFlowDispatch dispatch) throws OperationException {
		WorkFlowConfiguration config = dispatch.getConfig();
		WorkFlowService service = config.service();

		WorkFlowTask task = service.getTask(taskId, rootInstanceId);
		// 任务操作
		task.complete(context);
		// 流程上下文中设置当前节点
		context.setTask(task);

		service.changeTaskComplete(task);
		service.saveVariable(context);

		WorkFlowNode latest = service.getNode(task.getNodeId(), task.getRootInstanceId());

		// 并发加锁
		service.lockNode(task.getNodeId(), task.getRootInstanceId(), latest.getVersion());

		if (!latest.done()) {
			return;
		}

		service.changeNodeComplete(latest);
		latest.afterComplete();

		dispatch.addCallback(WorkFlowCallback.WorkFlowEvent.COMPLETE, context);

		dispatch.addOperation(WorkFlowOperation.ofNext(rootInstanceId, latest.getInstanceId(), latest, context));
	}
}
