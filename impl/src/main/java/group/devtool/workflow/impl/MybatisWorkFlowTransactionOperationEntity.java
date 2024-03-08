package group.devtool.workflow.impl;

public class MybatisWorkFlowTransactionOperationEntity {
  
  public static final String VARIABLE_ADD = "VARIABLE_ADD";
  public static final String INSTANCE_ADD = "INSTANCE_ADD";
  public static final String TASK_ADD = "TASK_ADD";
  public static final String TASK_COMPLETE = "TASK_COMPLETE";
  public static final String NODE_COMPLETE = "NODE_COMPLETE";

  private Long id;

  private String txId;

  private String type;

  private String instanceId;

  private String taskId;

  private String variableId;

  private String rootInstanceId;

  private String nodeCode;

  private Long txTimestamp;
  
  private String state;

  public MybatisWorkFlowTransactionOperationEntity() {

  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTxId() {
    return txId;
  }

  public void setTxId(String txId) {
    this.txId = txId;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public String getTaskId() {
    return taskId;
  }

  public void setTaskId(String taskIds) {
    this.taskId = taskIds;
  }

  public String getVariableId() {
    return variableId;
  }

  public void setVariableId(String variableId) {
    this.variableId = variableId;
  }

  public String getRootInstanceId() {
    return rootInstanceId;
  }

  public void setRootInstanceId(String rootInstanceId) {
    this.rootInstanceId = rootInstanceId;
  }

  public String getNodeCode() {
    return nodeCode;
  }

  public void setNodeCode(String nodeCode) {
    this.nodeCode = nodeCode;
  }

  public Long getTxTimestamp() {
    return txTimestamp;
  }

  public void setTxTimestamp(Long txTimestamp) {
    this.txTimestamp = txTimestamp;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }
}
