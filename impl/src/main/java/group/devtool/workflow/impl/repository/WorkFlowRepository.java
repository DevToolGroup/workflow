/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.impl.repository;

import group.devtool.workflow.engine.exception.ConcurrencyException;
import group.devtool.workflow.impl.WorkFlowConfigurationImpl;
import group.devtool.workflow.impl.entity.WorkFlowNodeEntity;
import group.devtool.workflow.impl.entity.WorkFlowInstanceEntity;
import group.devtool.workflow.impl.entity.WorkFlowTaskEntity;
import group.devtool.workflow.impl.entity.WorkFlowVariableEntity;

import java.util.List;

/**
 * 流程存储服务
 */
public class WorkFlowRepository {

	private final WorkFlowConfigurationImpl config;

	public WorkFlowRepository() {
		this.config = WorkFlowConfigurationImpl.CONFIG;
	}

	/**
	 * 加载流程变量
	 *
	 * @param instanceId 流程实例ID
	 * @return 流程变量列表
	 */
	public List<WorkFlowVariableEntity> loadVariable(String instanceId) {
		return config.getMapper().loadVariable(instanceId);
	}

	/**
	 * 持久化流程变量
	 *
	 * @param entities 流程变量
	 */
	public void bulkSaveVariable(List<WorkFlowVariableEntity> entities) {
		config.getMapper().bulkSaveVariable(entities);
	}

	/**
	 * 加载流程实例实体对象
	 *
	 * @param instanceId     流程实例ID
	 * @param rootInstanceId 根流程实例ID
	 * @return 流程实例实体对象
	 */
	public WorkFlowInstanceEntity loadInstance(String instanceId, String rootInstanceId) {
		return config.getMapper().loadInstance(instanceId, rootInstanceId);
	}

	/**
	 * 根据流程节点编码加载关联的流程任务实体
	 *
	 * @param nodeId         节点编码
	 * @param rootInstanceId 根流程实例ID
	 * @return 流程任务实体列表
	 */
	public List<WorkFlowTaskEntity> loadTaskByNodeId(String nodeId, String rootInstanceId) {
		return config.getMapper().loadTaskByNodeId(nodeId, rootInstanceId);
	}

	/**
	 * 根据流程任务ID加载流程任务实体
	 *
	 * @param taskId         流程任务ID
	 * @param rootInstanceId 根流程实例ID
	 * @return 流程任务实体
	 */
	public WorkFlowTaskEntity loadTaskById(String taskId, String rootInstanceId) {
		return config.getMapper().loadTaskByTaskId(taskId, rootInstanceId);
	}

	/**
	 * 根据流程节点ID加载流程任务实体
	 *
	 * @param nodeId         节点ID
	 * @param rootInstanceId 根流程实例ID
	 * @return 流程节点
	 */
	public WorkFlowNodeEntity loadNode(String nodeId, String rootInstanceId) {
		return config.getMapper().loadNodeById(nodeId, rootInstanceId);
	}

	/**
	 * 修改流程任务状态为已完成
	 *
	 * @param completeUser   完成用户
	 * @param completeTime   完成时间
	 * @param taskId         流程任务实例ID
	 * @param rootInstanceId 根流程实例ID
	 */
	public int changeTaskComplete(String completeUser, Long completeTime, String taskId, String rootInstanceId) {
		return config.getMapper().changeTaskComplete(completeUser, completeTime, taskId, rootInstanceId);
	}

	/**
	 * 修改流程节点状态为已完成
	 *
	 * @param nodeId         流程节点ID
	 * @param rootInstanceId 根流程实例ID
	 * @param version 节点版本
	 * @return 成功行数
	 */
	public int changeNodeComplete(String nodeId, String rootInstanceId, Integer version) {
		return config.getMapper().changeNodeComplete(nodeId, rootInstanceId, version);
	}

	/**
	 * 修改流程实例状态为已完成
	 *
	 * @param instanceId     流程实例ID
	 * @param rootInstanceId 根流程实例ID
	 * @return 成功行数
	 */
	public int changeInstanceComplete(String instanceId, String rootInstanceId) {
		return config.getMapper().changeInstanceComplete(instanceId, rootInstanceId);
	}

	/**
	 * 新增或修改流程实例
	 *
	 * @param entity 流程实例
	 */
	public void save(WorkFlowInstanceEntity entity) {
		config.getMapper().saveInstance(entity);
	}

	/**
	 * @param entity 流程节点实例
	 */
	public void save(WorkFlowNodeEntity entity) {
		config.getMapper().saveNode(entity);
	}

	/**
	 * 批量保存任务实例
	 *
	 * @param entities 任务实例
	 */
	public void bulkSaveTask(List<WorkFlowTaskEntity> entities) {
		config.getMapper().bulkSaveTask(entities);
	}

	public List<WorkFlowTaskEntity> loadActiveTask(String rootInstanceId) {
		return config.getMapper().loadActiveTask(rootInstanceId);
	}

	public void lockNode(String nodeId, String rootInstanceId, Integer version) {
		int rows = config.getMapper().lockNode(nodeId, rootInstanceId, version);
		if (rows == 0) {
			throw new ConcurrencyException("节点并发操作异常。节点ID：" + nodeId);
		}
	}

	public int changeInstanceStop(String instanceId, String rootInstanceId) {
		return config.getMapper().changeInstanceStop(instanceId, rootInstanceId);

	}

	@SuppressWarnings("only test")
	public WorkFlowNodeEntity loadActiveNodeByCode(String code, String rootInstanceId) {
		return config.getMapper().loadActiveNodeByCode(code, rootInstanceId);
	}

	@SuppressWarnings("only test")
	public WorkFlowNodeEntity loadChildActiveNodeByCode(String nodeCode, String taskId, String instanceId) {
		WorkFlowInstanceEntity instanceEntity = config.getMapper().loadParentInstance(taskId, instanceId);
		return config.getMapper().loadChildActiveNodeByCode(nodeCode, instanceEntity.getInstanceId(), instanceEntity.getRootInstanceId());
	}
}
