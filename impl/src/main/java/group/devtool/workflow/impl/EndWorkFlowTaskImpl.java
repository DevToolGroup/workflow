package group.devtool.workflow.impl;

import group.devtool.workflow.core.EndWorkFlowTask;
import group.devtool.workflow.core.WorkFlowContext;
import group.devtool.workflow.core.exception.WorkFlowException;

/**
 * {@link EndWorkFlowTask} 默认实现
 */
public class EndWorkFlowTaskImpl extends EndWorkFlowTask {

  private Long completeTime;

  private final String rootInstanceId;

  public EndWorkFlowTaskImpl(String node, String instanceId, String rootInstanceId) throws WorkFlowException {
    super(WorkFlowConfigurationImpl.CONFIG.idSupplier().getTaskId(), node, instanceId);
    this.rootInstanceId = rootInstanceId;
  }

  public EndWorkFlowTaskImpl(String taskId, String nodeCode, WorkFlowTaskState taskState, String instanceId,
                             String rootInstanceId) {
    super(taskId, nodeCode, instanceId, taskState);
    this.rootInstanceId = rootInstanceId;
  }

  @Override
  protected void doComplete(WorkFlowContext context) throws WorkFlowException {
    completeTime = System.currentTimeMillis();
  }

  public String getRootInstanceId() {
    return rootInstanceId;
  }

  public String getTaskClass() {
    return "END";
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
