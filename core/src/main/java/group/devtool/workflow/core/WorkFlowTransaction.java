package group.devtool.workflow.core;

import group.devtool.workflow.core.exception.WorkFlowException;

/**
 * 存储事务
 */
public interface WorkFlowTransaction {

	<T> T doInTransaction(WorkFlowTransactionOperate<T> operate) throws WorkFlowException;

	@FunctionalInterface
	interface WorkFlowTransactionOperate<T> {

		T apply() throws WorkFlowException;

	}

}
