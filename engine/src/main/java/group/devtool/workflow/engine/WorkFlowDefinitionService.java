/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.engine;

import group.devtool.workflow.engine.definition.WorkFlowDefinition;
import group.devtool.workflow.engine.exception.*;

/**
 * 流程定义服务，主要实现流程定义的持久化以及加载
 */
public interface WorkFlowDefinitionService {

	/**
	 * 根据流程定义编码及版本，查询流程定义
	 *
	 * @param code      流程定义编码
	 * @param rootCode  根流程定义编码
	 * @param version   流程定义版本
	 * @param recursion 是否递归查询完整流程
	 * @return 流程定义
	 */
	WorkFlowDefinition load(String code, String rootCode, Integer version, Boolean recursion);

	/**
	 * 流程定义部署，如果流程定义已存在，则更新流程定义的版本，并卸载历史版本
	 *
	 * @param definition 流程定义
	 * 
	 */
	void deploy(WorkFlowDefinition definition) throws TransactionException;

	/**
	 * 流程定义卸载，如果流程定义已存在，则更新流程定义的版本
	 *
	 * @param code 流程定义
	 */
	void undeploy(String code) throws TransactionException;

}
