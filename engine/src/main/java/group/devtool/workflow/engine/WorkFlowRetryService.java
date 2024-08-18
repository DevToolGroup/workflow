package group.devtool.workflow.engine;

import group.devtool.workflow.engine.operation.RetryWorkFlowOperation;
import group.devtool.workflow.engine.operation.WorkFlowOperation;


/**
 * 流程操作异常重试服务
 */
public interface WorkFlowRetryService {

	/**
	 * 添加流程操作
	 * @param operations 流程操作
	 */
	void addOperation(WorkFlowOperation... operations);

	/**
	 * 更新流程操作
	 * @param retryOperation 流程操作
	 */
	void changeOperation(RetryWorkFlowOperation retryOperation);

	/**
	 * 重试流程操作
	 */
	void retryOperation();

}
