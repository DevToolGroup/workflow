/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.engine.exception;

/**
 * 流程存储事务异常
 */
public class WorkFlowTransactionException extends WorkFlowRuntimeException {

	public WorkFlowTransactionException(String message) {
		super(message);
	}
}
