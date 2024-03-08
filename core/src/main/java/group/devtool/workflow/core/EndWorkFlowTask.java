package group.devtool.workflow.core;

/**
 * 流程结束节点任务
 */
public abstract class EndWorkFlowTask extends AbstractWorkFlowTask {

  public EndWorkFlowTask(String taskId, String node, String instanceId) {
    super(taskId, node, instanceId);
  }

  public EndWorkFlowTask(String taskId, String node, String instanceId, WorkFlowTaskState state) {
    super(taskId, node, instanceId, state);
  }
}
