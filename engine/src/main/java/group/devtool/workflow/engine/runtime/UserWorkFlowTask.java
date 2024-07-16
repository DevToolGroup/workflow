/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.engine.runtime;

import group.devtool.workflow.engine.WorkFlowContextImpl;
import group.devtool.workflow.engine.exception.NotFoundWorkFlowVariable;
import group.devtool.workflow.engine.exception.NotUserTaskPermission;

/**
 * 用户任务
 */
public abstract class UserWorkFlowTask extends AbstractWorkFlowTask {

  private final UserWorkFlowTaskConfig userConfig;

  public UserWorkFlowTask(String taskId, String nodeId, String nodeCode, UserWorkFlowTaskConfig userConfig, String instanceId) {
    super(taskId, nodeId, nodeCode, instanceId);
    this.userConfig = userConfig;
  }

  public UserWorkFlowTask(String taskId, String nodeId, String nodeCode, UserWorkFlowTaskConfig userConfig, String instanceId, WorkFlowTaskState state) {
    super(taskId, nodeId, nodeCode, instanceId, state);
    this.userConfig = userConfig;
  }

  @Override
  protected void doComplete(WorkFlowContextImpl context)  {
    String confirmUser = (String) context.lookup(context.USER);
    if (null == confirmUser) {
      throw new NotFoundWorkFlowVariable("用户节点必须传递用户信息");
    }
    if (!userConfig.getPendingUser().equals(confirmUser)) {
      throw new NotUserTaskPermission("用户勿权限操作当前节点");
    }
    doCustomComplete(context);
  }

  protected abstract void doCustomComplete(WorkFlowContextImpl context) ;


  public static abstract class UserWorkFlowTaskConfig implements WorkFlowTaskConfig {

    public abstract String getPendingUser();

  }

}
