/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.engine;

import group.devtool.workflow.engine.definition.WorkFlowNodeDefinition;
import group.devtool.workflow.engine.runtime.WorkFlowInstance;
import group.devtool.workflow.engine.runtime.WorkFlowNode;
import group.devtool.workflow.engine.runtime.WorkFlowTask;

import java.util.List;

/**
 * 流程服务主要完成：
 * 1. 加载流程实例化信息（流程实例，流程节点，流程任务），
 * 其中流程实例化信息中传入的根流程实例ID的目的在于后续数据库分库分表
 * <p>
 * 2. 根据流程定义初始化流程实例
 */
public interface WorkFlowService {

	/**
	 * 初始化流程上下文
	 *
	 * @param rootInstanceId 流程实例ID
	 * @param variables      流程变量
	 * @return 流程上下文
	 */
	WorkFlowContextImpl getContext(String rootInstanceId, WorkFlowVariable... variables);

	/**
	 * 持久化流程变量
	 *
	 * @param context      流程上下文
	 */
	void saveVariable(WorkFlowContext context);

	/**
	 * 根据流程定义编码初始化流程实例
	 *
	 * @param code 流程定义编码
	 * @return 流程实例
	 */
	WorkFlowInstance getInstance(String code);

	/**
	 * 根据流程定义初始化流程实例
	 *
	 * @param definitionCode 流程定义
	 * @param taskId         流程任务ID
	 * @param rootInstanceId 根流程实例ID
	 * @return 流程实例
	 */
	WorkFlowInstance getChildInstance(String definitionCode, String taskId, String rootInstanceId);

	/**
	 * 持久化流程实例
	 *
	 * @param instance       流程实例
	 * @param rootInstanceId 根流程实例ID
	 */
	void save(WorkFlowInstance instance, String rootInstanceId);

	/**
	 * 加载流程实例
	 *
	 * @param instanceId     流程实例ID
	 * @param rootInstanceId 根流程实例ID
	 * @return 流程实例
	 */
	WorkFlowInstance getInstance(String instanceId, String rootInstanceId);

	/**
	 * 根据流程节点定义初始化流程节点
	 *
	 * @param ndf            流程定义
	 * @param instanceId     流程实例ID
	 * @param rootInstanceId 根流程实例ID
	 * @param context        流程上下文
	 * @return 流程节点
	 */
	WorkFlowNode getNode(WorkFlowNodeDefinition ndf, String instanceId, String rootInstanceId, WorkFlowContextImpl context);

	/**
	 * 加载流程节点
	 *
	 * @param nodeId         流程节点ID
	 * @param rootInstanceId 根流程实例ID
	 * @return 流程节点
	 */
	WorkFlowNode getNode(String nodeId, String rootInstanceId);

	/**
	 * 加载流程任务
	 *
	 * @param taskId         节点任务ID
	 * @param rootInstanceId 根流程实例ID
	 * @return 流程任务
	 */
	WorkFlowTask getTask(String taskId, String rootInstanceId);

	/**
	 * 根据流程节点，以及事件名称，加载流程事件任务
	 *
	 * @param event          事件名称
	 * @param node           节点编码
	 * @param rootInstanceId 根流程实例ID
	 * @return 流程事件任务
	 */
	WorkFlowTask getTask(String event, String node, String rootInstanceId);

	/**
	 * 持久化流程节点
	 *
	 * @param node           流程节点
	 */
	void save(WorkFlowNode node);

	/**
	 * 节点乐观锁
	 *
	 * @param nodeId         节点ID
	 * @param rootInstanceId 根流程实例ID
	 * @param version        节点版本
	 */
	void lockNode(String nodeId, String rootInstanceId, Integer version);

	/**
	 * 修改流程实例状态为已完成
	 *
	 * @param instance       流程实例
	 * @param rootInstanceId 根流程实例ID
	 */
	void changeInstanceComplete(WorkFlowInstance instance, String rootInstanceId);

	/**
	 * 修改流程节点状态为已完成
	 *
	 * @param node 流程节点
	 */
	void changeNodeComplete(WorkFlowNode node);

	/**
	 * 修改流程任务状态为已完成
	 *
	 * @param task 流程任务
	 */
	void changeTaskComplete(WorkFlowTask task);

	/**
	 * 查询当前待执行的任务信息
	 *
	 * @param rootInstanceId 根流程实例ID
	 * @return 待执行任务列表
	 */
	List<WorkFlowTask> loadActiveTask(String rootInstanceId);

	void changeInstanceStop(WorkFlowInstance instance);


}
