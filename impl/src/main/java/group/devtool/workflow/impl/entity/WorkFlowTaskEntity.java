/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.impl.entity;

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

  private String config;

  private String nodeId;

  private String nodeCode;

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

  public String getConfig() {
    return config;
  }

  public void setConfig(String config) {
    this.config = config;
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

  public String getNodeId() {
    return nodeId;
  }

  public void setNodeId(String nodeId) {
    this.nodeId = nodeId;
  }

  public String getNodeCode() {
    return nodeCode;
  }

  public void setNodeCode(String nodeCode) {
    this.nodeCode = nodeCode;
  }
}
