/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.engine;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 流程上下文，保存流程运行过程中涉及的流程实例信息，节点信息，变量信息
 */
public interface WorkFlowContext extends Serializable {

	String USER = "USER";

	Object lookup(String name);

	/**
	 * @return 返回流程流转过程中所有的变量数据，按照添加的顺序排列
	 */
	List<WorkFlowVariable> getVariables();

	/**
	 * @return 返回流程实例流转过程中所有的变量，重名的变量按照出现顺序，后边的覆盖前边的。
	 */
	Map<String, Serializable> getVariableMap();

	/**
	 * @return 流程实例ID
	 */
	String getInstanceId();

	/**
	 * @return 根流程实例ID
	 */
	String getRootInstanceId();

	/**
	 * @return 当前流转节点编码
	 */
	String getNodeCode();

	/**
	 * @return 当前流转节点ID
	 */
	String getNodeId();

	/**
	 * 判断当前流程是否是子流程
	 *
	 * @return 如果是子流程返回true，相反返回false
	 */
	boolean isChildInstance();

	String getTaskId();
}
