/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.engine.runtime;

import group.devtool.workflow.engine.WorkFlowContextImpl;
import group.devtool.workflow.engine.definition.WorkFlowNodeDefinition;
import group.devtool.workflow.engine.exception.NotSupportWorkFlowNodeClass;

import java.util.List;

/**
 * 流程实例
 */
public interface WorkFlowInstance {

	/**
	 * @return 流程实例ID
	 */
	String getInstanceId();

	/**
	 * 判断流程实例是否结束
	 *
	 * @return 如果结束返回true，相反返回false
	 */
	boolean done();

	/**
	 * 判断流程实例是否终止
	 *
	 * @return 如果终止返回true，相反返回false
	 */
	boolean stopped();

	/**
	 * 启动流程实例
	 *
	 * @param initNode 流转节点初始化方法
	 * @param context  流程上下文
	 * @return 开始节点
	 * @ 流程异常
	 */
	WorkFlowNode start(InitNode initNode, WorkFlowContextImpl context);

	/**
	 * 流程流转
	 *
	 * @param initNode 流转节点初始化方法
	 * @param nodeCode 流程节点
	 * @param context 流程上下文
	 * @return 节点列表
	 * @ 流程异常
	 */
	List<WorkFlowNode> next(InitNode initNode, String nodeCode, WorkFlowContextImpl context) ;

	/**
	 * 终止流程实例
	 */
	void stop();

	/**
	 * @return 流程定义编码
	 */
	String getDefinitionCode();

	/**
	 * @return 流程定义版本
	 */
	Integer getDefinitionVersion();

	String getRootDefinitionCode();

	/**
	 * 节点初始化
	 */
	interface InitNode {

		/**
		 * 初始化流程流转的下一节点
		 *
		 * @param definitions    节点定义
		 * @param instanceId     流程实例ID
		 * @param rootInstanceId 根流程实例ID
		 * @param context        流程上下文
		 * @return 初始化的流程节点列表
		 * @ 流程异常
		 */
		List<WorkFlowNode> init(List<WorkFlowNodeDefinition> definitions, String instanceId, String rootInstanceId,
														WorkFlowContextImpl context) throws NotSupportWorkFlowNodeClass;

	}
}
