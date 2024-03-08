package group.devtool.workflow.impl;

import group.devtool.workflow.core.UserWorkFlowTask;
import group.devtool.workflow.core.WorkFlowContext;
import group.devtool.workflow.core.WorkFlowVariable;
import group.devtool.workflow.core.exception.WorkFlowException;

import java.util.List;

/**
 * {@link UserWorkFlowTask} 默认实现
 */
public class UserWorkFlowTaskImpl extends UserWorkFlowTask  {

  private Long completeTime;

  private final String rootInstanceId;

  private String completeUser;

  private final byte[] byteConfig;

  public UserWorkFlowTaskImpl(String node, WorkFlowUserTaskConfig config, String instanceId, String rootInstanceId)
      throws WorkFlowException {
    super(WorkFlowConfigurationImpl.CONFIG.idSupplier().getTaskId(), node, config, instanceId);
    this.rootInstanceId = rootInstanceId;
    this.byteConfig = getConfig(config);
  }

  public UserWorkFlowTaskImpl(String taskId, String node, WorkFlowUserTaskConfig config, String instanceId,
                              String rootInstanceId, WorkFlowTaskState taskState) throws WorkFlowException {
    super(taskId, node, config, instanceId, taskState);
    this.rootInstanceId = rootInstanceId;
    this.byteConfig = getConfig(config);
  }

  @Override
  protected void doCustomComplete(WorkFlowContext context) throws WorkFlowException {
    completeTime = System.currentTimeMillis();
    completeUser = (String) context.lookup(WorkFlowVariable.USER);
  }

  @Override
  public String getRootInstanceId() {
    return rootInstanceId;
  }

  @Override
  public String getTaskClass() {
    return "USER";
  }

  @Override
  public String getCompleteUser() {
    return completeUser;
  }

  @Override
  public Long getCompleteTime() {
    return completeTime;
  }

  public static class MybatisWorkFlowUserTaskConfig extends WorkFlowUserTaskConfig {

    private final String pendingUser;

    private final List<String> member;

    private final Integer confirm;

    public MybatisWorkFlowUserTaskConfig(String pendingUser, List<String> member, Integer confirm) {
      this.pendingUser = pendingUser;
      this.member = member;
      this.confirm = confirm;
    }

    @Override
    public List<String> member() {
      return member;
    }

    @Override
    public Integer confirm() {
      return confirm;
    }

    @Override
    public String pendingUser() {
      return pendingUser;
    }

  }

  @Override
  public byte[] getTaskConfig() {
    return byteConfig;
  }
}
