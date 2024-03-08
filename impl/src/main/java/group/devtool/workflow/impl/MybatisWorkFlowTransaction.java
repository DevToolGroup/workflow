package group.devtool.workflow.impl;

import java.util.List;
import java.util.stream.Collectors;

import group.devtool.workflow.core.AbstractWorkFlowTransaction;
import group.devtool.workflow.core.exception.WorkFlowException;

public class MybatisWorkFlowTransaction extends AbstractWorkFlowTransaction {

  private String seq;

  public MybatisWorkFlowTransaction() {
  }

  @Override
  public String seq() {
    return seq;
  }

  @Override
  protected void doBegin() throws WorkFlowException {
    seq = WorkFlowConfigurationImpl.CONFIG.idSupplier().getTransactionId();
  }

  @Override
  protected void beforeRollback() {
    WorkFlowConfigurationImpl.CONFIG.dbTransaction().begin();
  }

  @Override
  protected void afterRollback() {
    WorkFlowConfigurationImpl.CONFIG.dbTransaction().commit();
    WorkFlowConfigurationImpl.CONFIG.dbTransaction().close();
  }

  @Override
  public void commit() throws WorkFlowException {
    WorkFlowConfigurationImpl.CONFIG.dbTransaction().doInTransaction(() -> {
      WorkFlowConfigurationImpl.CONFIG.repository().cleanTransactionOperation(seq, getRootInstanceId());
      return true;
    });
  }

  public static WorkFlowTransactionOperation addVariableOperation(List<WorkFlowVariableEntity> addEntities,
      WorkFlowRepository repository) throws WorkFlowException {

    VariableAddOperation operation = new VariableAddOperation(
        addEntities.stream().map(i -> i.getId()).collect(Collectors.toList()),
        addEntities.get(0).getRootInstanceId(),
        repository);

    operation.setTxId(AbstractWorkFlowTransaction.current().seq());
    MybatisWorkFlowTransactionOperationEntity entity = operation.toEntity();
    repository.addTransactionOperation(entity);
    operation.setId(entity.getId());
    return operation;
  }

  public static WorkFlowTransactionOperation addTaskOperation(List<WorkFlowTaskEntity> entities,
      WorkFlowRepository repository) throws WorkFlowException {

    TaskAddOperation operation = new TaskAddOperation(
        entities.stream().map(i -> i.getTaskId()).collect(Collectors.toList()),
        entities.get(0).getRootInstanceId(),
        repository);

    operation.setTxId(AbstractWorkFlowTransaction.current().seq());

    MybatisWorkFlowTransactionOperationEntity entity = operation.toEntity();
    repository.addTransactionOperation(entity);
    operation.setId(entity.getId());
    return operation;
  }

  public static WorkFlowTransactionOperation addTaskCompleteOperation(String taskId, String rootInstanceId,
      WorkFlowRepository repository) throws WorkFlowException {
    TaskCompleteOperation operation = new TaskCompleteOperation(taskId, rootInstanceId, repository);

    operation.setTxId(AbstractWorkFlowTransaction.current().seq());

    MybatisWorkFlowTransactionOperationEntity entity = operation.toEntity();
    repository.addTransactionOperation(entity);
    operation.setId(entity.getId());
    return operation;
  }

  public static WorkFlowTransactionOperation addInstanceOperation(WorkFlowInstanceEntity entity,
                                                                  WorkFlowRepository repository) throws WorkFlowException {

    InstanceAddOperation operation = new InstanceAddOperation(entity.getInstanceId(),
        entity.getRootInstanceId(),
        repository);
    operation.setTxId(AbstractWorkFlowTransaction.current().seq());

    MybatisWorkFlowTransactionOperationEntity operationEntity = operation.toEntity();
    repository.addTransactionOperation(operationEntity);
    operation.setId(operationEntity.getId());
    return operation;
  }

  public static WorkFlowTransactionOperation addNodeCompleteOperation(String code, String rootInstanceId,
      WorkFlowRepository repository) throws WorkFlowException {

    NodeCompleteOperation operation = new NodeCompleteOperation(code, rootInstanceId, repository);
    operation.setTxId(AbstractWorkFlowTransaction.current().seq());

    MybatisWorkFlowTransactionOperationEntity entity = operation.toEntity();
    repository.addTransactionOperation(entity);
    operation.setId(entity.getId());
    return operation;
  }

  public static class VariableAddOperation implements WorkFlowTransactionOperation {

    private final List<Long> deleteIds;

    private final String rootInstanceId;

    private final WorkFlowRepository repository;

    private String txId;

    private Long id;

    public VariableAddOperation(List<Long> ids, String rootInstanceId, WorkFlowRepository repository) {
      this.deleteIds = ids;
      this.repository = repository;
      this.rootInstanceId = rootInstanceId;
    }

    public void setTxId(String seq) {
      this.txId = seq;
    }

    @Override
    public void cancel() throws WorkFlowException {
      repository.deleteVariableById(deleteIds, rootInstanceId);
      repository.doRollbackOperation(id, txId, rootInstanceId);
    }

    public List<Long> getDeleteIds() {
      return deleteIds;
    }

    @Override
    public String getRootInstanceId() {
      return rootInstanceId;
    }

    public String getTxId() {
      return txId;
    }

    public MybatisWorkFlowTransactionOperationEntity toEntity() {
      MybatisWorkFlowTransactionOperationEntity entity = new MybatisWorkFlowTransactionOperationEntity();
      entity.setTxId(txId);
      entity.setRootInstanceId(rootInstanceId);
      entity.setVariableId(deleteIds.stream().map(i -> String.valueOf(i)).collect(Collectors.joining(",")));
      entity.setType(MybatisWorkFlowTransactionOperationEntity.VARIABLE_ADD);
      return entity;
    }

    @Override
    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
    }
  }

  public static class TaskAddOperation implements WorkFlowTransactionOperation {

    private String txId;

    private final List<String> deleteIds;

    private final String rootInstanceId;

    private final WorkFlowRepository repository;

    private Long id;

    public TaskAddOperation(List<String> taskIds, String rootInstanceId, WorkFlowRepository repository) {
      this.deleteIds = taskIds;
      this.repository = repository;
      this.rootInstanceId = rootInstanceId;
    }

    @Override
    public void cancel() throws WorkFlowException {
      repository.deleteTaskById(deleteIds, rootInstanceId);
      repository.doRollbackOperation(id, txId, rootInstanceId);
    }

    public void setTxId(String seq) {
      this.txId = seq;
    }

    public String getTxId() {
      return txId;
    }

    public List<String> getDeleteIds() {
      return deleteIds;
    }

    @Override
    public String getRootInstanceId() {
      return rootInstanceId;
    }

    @Override
    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
    }

    public MybatisWorkFlowTransactionOperationEntity toEntity() {
      MybatisWorkFlowTransactionOperationEntity entity = new MybatisWorkFlowTransactionOperationEntity();
      entity.setTxId(txId);
      entity.setRootInstanceId(rootInstanceId);
      entity.setTaskId(deleteIds.stream().collect(Collectors.joining(",")));
      entity.setType(MybatisWorkFlowTransactionOperationEntity.TASK_ADD);
      return entity;
    }
  }

  public static class InstanceAddOperation implements WorkFlowTransactionOperation {

    private String txId;

    private final String instanceId;

    private final String rootInstanceId;

    private final WorkFlowRepository repository;

    private Long id;

    public InstanceAddOperation(String instanceId, String rootInstanceId, WorkFlowRepository repository) {
      this.instanceId = instanceId;
      this.repository = repository;
      this.rootInstanceId = rootInstanceId;
    }

    @Override
    public void cancel() throws WorkFlowException {
      repository.deleteInstanceById(instanceId, rootInstanceId);
      repository.doRollbackOperation(id, txId, rootInstanceId);
    }

    public void setTxId(String txId) {
      this.txId = txId;
    }

    public String getTxId() {
      return txId;
    }

    public String getInstanceId() {
      return instanceId;
    }

    @Override
    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
    }

    @Override
    public String getRootInstanceId() {
      return rootInstanceId;
    }

    public MybatisWorkFlowTransactionOperationEntity toEntity() {
      MybatisWorkFlowTransactionOperationEntity entity = new MybatisWorkFlowTransactionOperationEntity();
      entity.setTxId(txId);
      entity.setInstanceId(instanceId);
      entity.setRootInstanceId(rootInstanceId);
      entity.setType(MybatisWorkFlowTransactionOperationEntity.INSTANCE_ADD);
      return entity;
    }
  }

  public static class TaskCompleteOperation implements WorkFlowTransactionOperation {

    private final WorkFlowRepository repository;

    private String txId;

    private final String taskId;

    private final String rootInstanceId;

    private Long id;

    public TaskCompleteOperation(String taskId, String rootInstanceId, WorkFlowRepository repository) {
      this.taskId = taskId;
      this.rootInstanceId = rootInstanceId;
      this.repository = repository;
    }

    @Override
    public void cancel() throws WorkFlowException {
      repository.changeTaskDoing(taskId, rootInstanceId);
      repository.doRollbackOperation(id, txId, rootInstanceId);
    }

    public void setTxId(String txId) {
      this.txId = txId;
    }

    public String getTxId() {
      return txId;
    }

    public String getTaskId() {
      return taskId;
    }

    @Override
    public String getRootInstanceId() {
      return rootInstanceId;
    }

    @Override
    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
    }

    public MybatisWorkFlowTransactionOperationEntity toEntity() {
      MybatisWorkFlowTransactionOperationEntity entity = new MybatisWorkFlowTransactionOperationEntity();
      entity.setTxId(txId);
      entity.setRootInstanceId(rootInstanceId);
      entity.setTaskId(taskId);
      entity.setType(MybatisWorkFlowTransactionOperationEntity.TASK_COMPLETE);
      return entity;
    }
  }

  public static class NodeCompleteOperation implements WorkFlowTransactionOperation {

    private final WorkFlowRepository repository;

    private final String nodeCode;

    private final String rootInstanceId;

    private String txId;

    private Long id;

    public NodeCompleteOperation(String nodeCode, String rootInstanceId,
        WorkFlowRepository repository) {
      this.nodeCode = nodeCode;
      this.rootInstanceId = rootInstanceId;
      this.repository = repository;
    }

    @Override
    public void cancel() throws WorkFlowException {
      repository.changeNodeDoing(nodeCode, rootInstanceId);
      repository.doRollbackOperation(id, txId, rootInstanceId);
    }

    public String getTxId() {
      return txId;
    }

    public void setTxId(String txId) {
      this.txId = txId;
    }

    public String getNodeCode() {
      return nodeCode;
    }

    @Override
    public String getRootInstanceId() {
      return rootInstanceId;
    }

    @Override
    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
    }

    public MybatisWorkFlowTransactionOperationEntity toEntity() {
      MybatisWorkFlowTransactionOperationEntity entity = new MybatisWorkFlowTransactionOperationEntity();
      entity.setTxId(txId);
      entity.setNodeCode(nodeCode);
      entity.setRootInstanceId(rootInstanceId);
      entity.setType(MybatisWorkFlowTransactionOperationEntity.NODE_COMPLETE);
      return entity;
    }
  }

}
