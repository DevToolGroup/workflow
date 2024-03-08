package group.devtool.workflow.core;

import group.devtool.workflow.core.exception.WorkFlowException;

/**
 * 流程定义服务，主要实现流程定义的持久化以及加载
 */
public interface WorkFlowDefinitionService {

	/**
	 * 根据流程定义编码及版本，查询流程定义
	 *
	 * @param code      流程定义编码
	 * @param version   流程定义版本
	 * @param recursion 是否递归查询完整流程
	 * @return 流程定义
	 * @throws WorkFlowException 流程异常
	 */
	WorkFlowDefinition load(String code, Integer version, Boolean recursion) throws WorkFlowException;

	/**
	 * 流程定义部署，如果流程定义已存在，则更新流程定义的版本，并卸载历史版本
	 *
	 * @param definition 流程定义
	 * @throws WorkFlowException 流程异常
	 */
	void deploy(WorkFlowDefinition definition) throws WorkFlowException;

	/**
	 * 流程定义卸载，如果流程定义已存在，则更新流程定义的版本
	 *
	 * @param code 流程定义
	 * @throws WorkFlowException
	 */
	void undeploy(String code) throws WorkFlowException;

}
