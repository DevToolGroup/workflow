/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.engine.definition;

import group.devtool.workflow.engine.WorkFlowContextImpl;

/**
 * 流程节点间连线定义
 */
public interface WorkFlowLinkDefinition {

	/**
	 * @return 连接线编码
	 */
	String getCode();

	String getSource();

	/**
	 * 连接线目标节点
	 *
	 * @return 目标节点编码
	 */
	String getTarget();

	/**
	 * 根据流程上下文判断当前连接线条件是否满足
	 *
	 * @param sourceCode 来源节点
	 * @param context 流程上下文
	 * @return true: 满足, false: 相反
	 */
	boolean match(String sourceCode, WorkFlowContextImpl context);

}
