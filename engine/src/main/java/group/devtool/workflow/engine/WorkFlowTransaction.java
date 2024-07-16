/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.engine;

import group.devtool.workflow.engine.exception.WorkFlowException;

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