package group.devtool.workflow.impl;

import java.io.Serializable;

import group.devtool.workflow.core.StartWorkFlowTask;
import group.devtool.workflow.core.WorkFlowContext;
import group.devtool.workflow.core.WorkFlowVariable;
import group.devtool.workflow.core.exception.NotFoundWorkFlowVariable;
import group.devtool.workflow.core.exception.WorkFlowException;

/**
 * {@link StartWorkFlowTask} 默认实现
 */
public class StartWorkFlowTaskImpl extends StartWorkFlowTask {

  private String account;

  private final String rootInstanceId;

  private Long completeTime;

  public StartWorkFlowTaskImpl(String node, String instanceId, String rootInstanceId) throws WorkFlowException, WorkFlowException {
    super(WorkFlowConfigurationImpl.CONFIG.idSupplier().getTaskId(), node, instanceId);
    this.rootInstanceId = rootInstanceId;
  }

  public StartWorkFlowTaskImpl(String taskId, String node, String instanceId, String rootInstanceId,
                               WorkFlowTaskState taskState) {
    super(taskId, node, instanceId, taskState);
    this.rootInstanceId = rootInstanceId;
  }

  @Override
  protected void doComplete(WorkFlowContext context) throws WorkFlowException {
    account = (String) context.lookup(WorkFlowVariable.USER);
    if (null == account) {
      throw new NotFoundWorkFlowVariable("开始节点缺少操作用户参数");
    }
    completeTime = System.currentTimeMillis();
  }

  public Serializable getAccount() {
    return account;
  }

  public String getRootInstanceId() {
    return rootInstanceId;
  }

  public String getTaskClass() {
    return "START";
  }

  public String getCompleteUser() {
    return account;
  }

  public Long getCompleteTime() {
    return completeTime;
  }

  public byte[] getTaskConfig() {
    return null;
  }

}
