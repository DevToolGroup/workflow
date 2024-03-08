package group.devtool.workflow.impl;

/**
 * 流程变量实体
 */
public class WorkFlowVariableEntity {

  private Long id;

  private String name;

  private byte[] value;

  private String node;

  private String taskId;

  private String rootInstanceId;

  public WorkFlowVariableEntity() {

  }

  public WorkFlowVariableEntity(Long id, String name, byte[] value, String node, String taskId, String instanceId) {
    this.id = id;
    this.name = name;
    this.value = value;
    this.node = node;
    this.taskId = taskId;
    this.rootInstanceId = instanceId;
  }

  public WorkFlowVariableEntity(String name, byte[] value, String node, String taskId, String instanceId) {
    this.name = name;
    this.value = value;
    this.node = node;
    this.taskId = taskId;
    this.rootInstanceId = instanceId;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public byte[] getValue() {
    return value;
  }

  public void setValue(byte[] value) {
    this.value = value;
  }

  public String getNode() {
    return node;
  }

  public void setNode(String node) {
    this.node = node;
  }

  public String getTaskId() {
    return taskId;
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  public String getRootInstanceId() {
    return rootInstanceId;
  }

  public void setRootInstanceId(String instanceId) {
    this.rootInstanceId = instanceId;
  }
}
