package group.devtool.workflow.core;


import group.devtool.workflow.core.UserWorkFlowNodeDefinition.WorkFlowUserConfig;
import group.devtool.workflow.core.exception.WorkFlowException;

/**
 * 用户节点
 */
public abstract class UserWorkFlowNode extends AbstractWorkFlowNode {

  protected WorkFlowUserConfig config;

  public UserWorkFlowNode(WorkFlowNodeDefinition definition, WorkFlowContext context)
      throws WorkFlowException {
    super(definition, context);
    this.config = ((UserWorkFlowNodeDefinition)definition).getConfig();
  }

  public UserWorkFlowNode(String code, WorkFlowUserConfig config, WorkFlowTask... tasks) throws WorkFlowException {
    super(code, tasks);
    this.config = config;
  }

  @Override
  public boolean done() throws WorkFlowException {
    int confirming = 0;
    for (WorkFlowTask task : getTasks()) {
      if (task.completed()) {
        confirming += 1;
      }
    }
    return confirming >= config.confirm();
  }

  @Override
  protected WorkFlowTask[] initTask(WorkFlowNodeDefinition definition, WorkFlowContext context)
      throws WorkFlowException {
    UserWorkFlowNodeDefinition userDefinition = (UserWorkFlowNodeDefinition) definition;
    return doInitTask(userDefinition, context);
  }

  protected abstract UserWorkFlowTask[] doInitTask(UserWorkFlowNodeDefinition definition, WorkFlowContext context)
      throws WorkFlowException;

}
