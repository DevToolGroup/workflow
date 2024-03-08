package group.devtool.workflow.impl;

import group.devtool.workflow.core.MergeWorkFlowTask;
import group.devtool.workflow.core.WorkFlowContext;
import group.devtool.workflow.core.exception.WorkFlowException;

/**
 * {@link MergeWorkFlowTask} 默认实现
 */
public class MergeWorkFlowTaskImpl extends MergeWorkFlowTask {

  private String rootInstanceId;

  private Long completeTime;

  public MergeWorkFlowTaskImpl(String node, String instanceId, String rootInstanceId) throws WorkFlowException {
    super(WorkFlowConfigurationImpl.CONFIG.idSupplier().getTaskId(), node, instanceId);
    this.rootInstanceId = rootInstanceId;
  }

  public MergeWorkFlowTaskImpl(String branch, String taskId, String node, String instanceId, String rootInstanceId,
                               WorkFlowTaskState state) {
    super(branch, taskId, node, instanceId, state);
    this.rootInstanceId = rootInstanceId;
  }

  @Override
  protected void doCustomComplete(WorkFlowContext context) throws WorkFlowException {
    // do nothing
    completeTime = System.currentTimeMillis();
  }

  public String getRootInstanceId() {
    return rootInstanceId;
  }

  public String getTaskClass() {
    return "MERGE";
  }

  public String getCompleteUser() {
    return null;
  }

  public Long getCompleteTime() {
    return completeTime;
  }

  public byte[] getTaskConfig() {
    return null;
  }

}
