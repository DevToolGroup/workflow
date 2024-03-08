package group.devtool.workflow.core;

import group.devtool.workflow.core.exception.WorkFlowException;

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
	 * @throws WorkFlowException 流程异常
	 */
	WorkFlowContext getContext(String rootInstanceId, WorkFlowVariable... variables)
					throws WorkFlowException;

	/**
	 * 持久化流程上下文
	 *
	 * @param context 流程上下文
	 * @throws WorkFlowException 流程异常
	 */
	void save(WorkFlowContext context) throws WorkFlowException;

	/**
	 * 根据流程定义编码初始化流程实例
	 *
	 * @param code 流程定义编码
	 * @return 流程实例
	 * @throws WorkFlowException 流程异常
	 */
	WorkFlowInstance getInstance(String code) throws WorkFlowException;

	/**
	 * 根据流程定义初始化流程实例
	 *
	 * @param definitionCode 流程定义
	 * @param taskId         流程任务ID
	 * @param rootInstanceId 根流程实例ID
	 * @return 流程实例
	 * @throws WorkFlowException 流程异常
	 */
	WorkFlowInstance getChildInstance(String definitionCode, String taskId, String rootInstanceId)
					throws WorkFlowException;

	/**
	 * 持久化流程实例
	 *
	 * @param instance       流程实例
	 * @param rootInstanceId 根流程实例ID
	 * @throws WorkFlowException 流程异常
	 */
	void save(WorkFlowInstance instance, String rootInstanceId) throws WorkFlowException;

	/**
	 * 加载流程实例
	 *
	 * @param instanceId     流程实例ID
	 * @param rootInstanceId 根流程实例ID
	 * @return 流程实例
	 * @throws WorkFlowException 流程异常
	 */
	WorkFlowInstance getInstance(String instanceId, String rootInstanceId) throws WorkFlowException;

	/**
	 * 根据流程节点定义初始化流程节点
	 *
	 * @param ndf            流程定义
	 * @param instanceId     流程实例ID
	 * @param rootInstanceId 根流程实例ID
	 * @param context        流程上下文
	 * @return 流程节点
	 */
	WorkFlowNode getNode(WorkFlowNodeDefinition ndf, String instanceId, String rootInstanceId,
											 WorkFlowContext context) throws WorkFlowException;

	/**
	 * 加载流程节点
	 *
	 * @param nodeCode       流程节点编码
	 * @param rootInstanceId 根流程实例ID
	 * @return 流程节点
	 * @throws WorkFlowException 流程异常
	 */
	WorkFlowNode getNode(String nodeCode, String rootInstanceId) throws WorkFlowException;

	/**
	 * 加载流程任务
	 *
	 * @param taskId         节点任务ID
	 * @param rootInstanceId 根流程实例ID
	 * @return 流程任务
	 * @throws WorkFlowException 流程异常
	 */
	WorkFlowTask getTask(String taskId, String rootInstanceId) throws WorkFlowException;

	/**
	 * 持久化流程节点
	 *
	 * @param node           流程节点
	 * @param rootInstanceId 根流程实例ID
	 * @throws WorkFlowException 流程异常
	 */
	void save(WorkFlowNode node, String rootInstanceId) throws WorkFlowException;

	/**
	 * 修改流程节点状态为已完成
	 *
	 * @param node 流程节点
	 * @throws WorkFlowException 流程异常
	 */
	void changeNodeComplete(WorkFlowNode node) throws WorkFlowException;

	/**
	 * 修改流程任务状态为已完成
	 *
	 * @param task 流程任务
	 * @throws WorkFlowException 流程异常
	 */
	void changeTaskComplete(WorkFlowTask task) throws WorkFlowException;

	/**
	 * 查询当前待执行的任务信息
	 *
	 * @param rootInstanceId 根流程实例ID
	 * @return 待执行任务列表
	 * @throws WorkFlowException 流程异常
	 */
	List<WorkFlowTask> loadActiveTask(String rootInstanceId) throws WorkFlowException;
}
