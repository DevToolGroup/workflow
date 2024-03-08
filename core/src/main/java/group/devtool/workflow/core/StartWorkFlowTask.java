package group.devtool.workflow.core;

/**
 * 启动节点对应的流程任务
 */
public abstract class StartWorkFlowTask extends AbstractWorkFlowTask {

  public StartWorkFlowTask(String taskId, String node, String instanceId) {
    super(taskId, node, instanceId);
  }

  public StartWorkFlowTask(String taskId, String node, String instanceId, WorkFlowTaskState state) {
    super(taskId, node, instanceId, state);
  }

}
