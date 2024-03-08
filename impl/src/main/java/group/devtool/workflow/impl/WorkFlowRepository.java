package group.devtool.workflow.impl;

import java.util.List;

import group.devtool.workflow.core.WorkFlowException;

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
   * @throws WorkFlowException 数据库事务异常
   */
  public List<WorkFlowVariableEntity> loadVariable(String instanceId) throws WorkFlowException {
    WorkFlowMapper mapper = config.mapper();
    return mapper.loadVariable(instanceId);
  }

  /**
   * 持久化流程变量
   * 
   * @param entities
   * @throws WorkFlowException 数据库事务异常
   */
  public void bulkSaveVariable(List<WorkFlowVariableEntity> entities)
      throws WorkFlowException {
    WorkFlowMapper mapper = config.mapper();
    mapper.bulkSaveVariable(entities);
  }

  /**
   * 加载流程实例实体对象
   * 
   * @param instanceId     流程实例ID
   * @param rootInstanceId 根流程实例ID
   * @return 流程实例实体对象
   * @throws WorkFlowException 数据库事务异常
   */
  public WorkFlowInstanceEntity getInstance(String instanceId, String rootInstanceId)
      throws WorkFlowException {
    WorkFlowMapper mapper = config.mapper();
    return mapper.loadInstance(instanceId, rootInstanceId);
  }

  /**
   * 根据流程节点编码加载关联的流程任务实体
   * 
   * @param nodeCode       节点编码
   * @param rootInstanceId 根流程实例ID
   * @return 流程任务实体列表
   * @throws WorkFlowException 数据库事务异常
   */
  public List<WorkFlowTaskEntity> getTaskByCode(String nodeCode, String rootInstanceId)
      throws WorkFlowException {
    WorkFlowMapper mapper = config.mapper();
    return mapper.loadTaskByActiveNode(nodeCode, rootInstanceId);
  }

  /**
   * 根据流程任务ID加载流程任务实体
   * 
   * @param taskId         流程任务ID
   * @param rootInstanceId 根流程实例ID
   * @return 流程任务实体
   */
  public WorkFlowTaskEntity getTaskById(String taskId, String rootInstanceId) throws WorkFlowException {
    WorkFlowMapper mapper = config.mapper();
    return mapper.loadTaskByTaskId(taskId, rootInstanceId);
  }

  /**
   * 修改流程任务状态为已完成
   * 
   * @param completeUser   完成用户
   * @param completeTime   完成时间
   * @param taskId         流程任务实例ID
   * @param rootInstanceId 根流程实例ID
   * @throws WorkFlowException
   */
  public int changeTaskComplete(String completeUser, Long completeTime, String taskId, String rootInstanceId)
      throws WorkFlowException {
    WorkFlowMapper mapper = config.mapper();
    return mapper.changeTaskComplete(completeUser, completeTime, taskId, rootInstanceId);
  }

  /**
   * 修改流程任务状态为已完成
   * 
   * @param rootInstanceId 根流程实例ID
   * @throws WorkFlowException 数据库事务异常
   */
  public int changeNodeComplete(String code, String rootInstanceId) throws WorkFlowException {
    WorkFlowMapper mapper = config.mapper();
    return mapper.changeNodeComplete(code, rootInstanceId);
  }

  /**
   * 新增或修改流程实例
   * 
   * @param entity 流程实例
   * @throws WorkFlowException 数据库事务异常
   */
  public void save(WorkFlowInstanceEntity entity) throws WorkFlowException {
    if (null != entity.getId()) {
      WorkFlowMapper mapper = config.mapper();
      mapper.updateInstance(entity);
    } else {
      WorkFlowMapper mapper = config.mapper();
      mapper.saveInstance(entity);
    }
  }

  /**
   * 批量保存任务实例
   * 
   * @param entities 任务实例
   * @throws WorkFlowException 数据库事务异常
   */
  public void bulkSaveTask(List<WorkFlowTaskEntity> entities) throws WorkFlowException {
    WorkFlowMapper mapper = config.mapper();
    mapper.bulkSaveTask(entities);
  }

  public void addTransactionOperation(MybatisWorkFlowTransactionOperationEntity entity)
      throws WorkFlowException {
    WorkFlowMapper mapper = config.mapper();
    entity.setTxTimestamp(System.currentTimeMillis());
    mapper.addTransactionOperation(entity);
  }

  public void deleteTaskById(List<String> deleteIds, String rootInstanceId) {
    WorkFlowMapper mapper = config.mapper();
    mapper.deleteTaskById(deleteIds, rootInstanceId);
  }

  public void deleteVariableById(List<Long> deleteIds, String rootInstanceId) {
    WorkFlowMapper mapper = config.mapper();
    mapper.deleteVariableById(deleteIds, rootInstanceId);
  }

  public void changeTaskDoing(String taskId, String rootInstanceId) {
    WorkFlowMapper mapper = config.mapper();
    mapper.changeTaskDoing(taskId, rootInstanceId);
  }

  public void deleteInstanceById(String instanceId, String rootInstanceId) {
    WorkFlowMapper mapper = config.mapper();
    mapper.deleteInstanceById(instanceId, rootInstanceId);
  }

  public void changeNodeDoing(String nodeCode, String rootInstanceId) {
    WorkFlowMapper mapper = config.mapper();
    mapper.changeNodeDoing(nodeCode, rootInstanceId);
  }

  public void cleanTransactionOperation(String seq, String rootInstanceId) {
    WorkFlowMapper mapper = config.mapper();
    mapper.cleanTransactionOperation(seq, rootInstanceId);
  }

  public void doRollbackOperation(Long id, String txId, String rootInstanceId) {
    WorkFlowMapper mapper = config.mapper();
    mapper.doRollbackOperation(id, txId, rootInstanceId);
  }

  public List<MybatisWorkFlowTransactionOperationEntity> loadTransactionOperation(String rootInstanceId) {
    WorkFlowMapper mapper = config.mapper();
    return mapper.loadTransactionOperation(rootInstanceId);
  }

  public List<WorkFlowTaskEntity> loadActiveTask(String rootInstanceId) {
    WorkFlowMapper mapper = config.mapper();
    return mapper.loadActiveTask(rootInstanceId);
  }

}
