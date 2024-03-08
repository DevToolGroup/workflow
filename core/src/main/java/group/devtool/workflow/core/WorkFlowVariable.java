package group.devtool.workflow.core;

import java.io.Serializable;

/**
 * 流程变量
 */
public class WorkFlowVariable {

  public static final String USER = "USER";

  private final String name;

  private final Object value;

  private String node;

  private String taskId;

  protected WorkFlowVariable(String name, Serializable value) {
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public Object getValue() {
    return value;
  }

  public String getNode() {
    return node;
  }

  public String getTaskId() {
    return taskId;
  }

  public void bindTask(String taskId, String nodeCode) {
    this.taskId = taskId;
    this.node = nodeCode;
  }

  public static WorkFlowVariable bound(String name, Serializable value, String taskId, String node) {
    WorkFlowVariable variable = new WorkFlowVariable(name, value);
    variable.bindTask(taskId, node);
    return variable;
  }

  public static WorkFlowVariable suspend(String name, Serializable value) {
    WorkFlowVariable variable = new WorkFlowVariable(name, value);
    return variable;
  }
}
