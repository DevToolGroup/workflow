package group.devtool.workflow.impl;

/**
 * 流程任务实体
 */
public class WorkFlowTaskEntity {

  private Long id;

  private String taskId;

  private String taskClass;

  private String taskState;

  private String completeUser;

  private Long completeTime;

  private byte[] config;

  private String nodeCode;

  private String nodeClass;

  private String nodeState;

  private String instanceId;

  private String rootInstanceId;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTaskId() {
    return taskId;
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  public String getTaskClass() {
    return taskClass;
  }

  public void setTaskClass(String taskClass) {
    this.taskClass = taskClass;
  }

  public String getTaskState() {
    return taskState;
  }

  public void setTaskState(String taskState) {
    this.taskState = taskState;
  }

  public String getCompleteUser() {
    return completeUser;
  }

  public void setCompleteUser(String completeUser) {
    this.completeUser = completeUser;
  }

  public Long getCompleteTime() {
    return completeTime;
  }

  public void setCompleteTime(Long completeTime) {
    this.completeTime = completeTime;
  }

  public byte[] getConfig() {
    return config;
  }

  public void setConfig(byte[] config) {
    this.config = config;
  }

  public void setNodeCode(String nodeCode) {
    this.nodeCode = nodeCode;
  }

  public void setNodeClass(String nodeClass) {
    this.nodeClass = nodeClass;
  }

  public void setNodeState(String nodeState) {
    this.nodeState = nodeState;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public String getRootInstanceId() {
    return rootInstanceId;
  }

  public void setRootInstanceId(String rootInstanceId) {
    this.rootInstanceId = rootInstanceId;
  }

  public MybatisWorkFlowNodeEntity getNode() {
    return new MybatisWorkFlowNodeEntity(nodeCode, nodeClass, nodeState);
  }

  public class MybatisWorkFlowNodeEntity {

    private String nodeCode;

    private String nodeClass;

    private String nodeState;

    public MybatisWorkFlowNodeEntity(String nodeCode, String nodeClass, String nodeState) {
      this.nodeCode = nodeCode;
      this.nodeClass = nodeClass;
      this.nodeState = nodeState;
    }

    public MybatisWorkFlowNodeEntity() {

    }

    public String getNodeCode() {
      return nodeCode;
    }

    public void setNodeCode(String nodeCode) {
      this.nodeCode = nodeCode;
    }

    public String getNodeClass() {
      return nodeClass;
    }

    public void setNodeClass(String nodeClass) {
      this.nodeClass = nodeClass;
    }

    public String getNodeState() {
      return nodeState;
    }

    public void setNodeState(String nodeState) {
      this.nodeState = nodeState;
    }

  }

}
