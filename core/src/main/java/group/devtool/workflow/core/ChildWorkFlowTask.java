package group.devtool.workflow.core;

public abstract class ChildWorkFlowTask extends AbstractWorkFlowTask {

  public ChildWorkFlowTask(String taskId, String node, String instanceId) {
    super(taskId, node, instanceId);
  }

  public ChildWorkFlowTask(String taskId, String node, String instanceId, WorkFlowTaskState state) {
    super(taskId, node, instanceId, state);
  }
}
