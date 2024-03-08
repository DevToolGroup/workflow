package group.devtool.workflow.core;

import group.devtool.workflow.core.UserWorkFlowNodeDefinition.WorkFlowUserConfig;
import group.devtool.workflow.core.exception.NotFoundWorkFlowVariable;
import group.devtool.workflow.core.exception.WorkFlowException;
import group.devtool.workflow.core.exception.NotUserTaskPermission;

/**
 * 用户任务
 */
public abstract class UserWorkFlowTask extends AbstractWorkFlowTask {

  private final WorkFlowUserTaskConfig userConfig;

  public UserWorkFlowTask(String taskId, String node, WorkFlowUserTaskConfig userConfig, String instanceId) {
    super(taskId, node, instanceId);
    this.userConfig = userConfig;
  }

  public UserWorkFlowTask(String taskId, String node, WorkFlowUserTaskConfig userConfig, String instanceId, WorkFlowTaskState state) {
    super(taskId, node, instanceId, state);
    this.userConfig = userConfig;
  }

  @Override
  protected void doComplete(WorkFlowContext context) throws WorkFlowException {
    String confirmUser = (String) context.lookup(WorkFlowVariable.USER);
    if (null == confirmUser) {
      throw new NotFoundWorkFlowVariable("用户节点必须传递用户信息");
    }
    if (!userConfig.pendingUser().equals(confirmUser)) {
      throw new NotUserTaskPermission("用户勿权限操作当前节点");
    }
    doCustomComplete(context);
  }

  protected abstract void doCustomComplete(WorkFlowContext context) throws WorkFlowException;


  public static abstract class WorkFlowUserTaskConfig implements WorkFlowUserConfig {

    public abstract String pendingUser();

  }

}
