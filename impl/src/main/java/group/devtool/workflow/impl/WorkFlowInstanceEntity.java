package group.devtool.workflow.impl;

/**
 * 流程实例实体
 */
public class WorkFlowInstanceEntity {

  private Long id;

  private String state;

  private String instanceId;

  private String definitionCode;

  private int definitionVersion;

  private String parentTaskId;

  private String rootInstanceId;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String id) {
    this.instanceId = id;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getDefinitionCode() {
    return definitionCode;
  }

  public void setDefinitionCode(String definitionCode) {
    this.definitionCode = definitionCode;
  }

  public int getDefinitionVersion() {
    return definitionVersion;
  }

  public void setDefinitionVersion(int definitionVersion) {
    this.definitionVersion = definitionVersion;
  }

  public String getRootInstanceId() {
    return rootInstanceId;
  }

  public String getParentTaskId() {
    return parentTaskId;
  }

  public void setParentTaskId(String parentId) {
    this.parentTaskId = parentId;
  }

  public void setRootInstanceId(String rootId) {
    this.rootInstanceId = rootId;
  }

}
