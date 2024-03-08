package group.devtool.workflow.impl;

import group.devtool.workflow.core.UserWorkFlowNode;
import group.devtool.workflow.core.UserWorkFlowNodeDefinition;
import group.devtool.workflow.core.UserWorkFlowTask;
import group.devtool.workflow.core.WorkFlowContext;
import group.devtool.workflow.core.WorkFlowNodeDefinition;
import group.devtool.workflow.core.WorkFlowTask;
import group.devtool.workflow.core.exception.WorkFlowException;
import group.devtool.workflow.impl.UserWorkFlowTaskImpl.MybatisWorkFlowUserTaskConfig;
import group.devtool.workflow.core.UserWorkFlowNodeDefinition.WorkFlowUserConfig;

/**
 * {@link UserWorkFlowNode} 默认实现
 */
public class UserWorkFlowNodeImpl extends UserWorkFlowNode {

  private final String instanceId;

  private final String rootInstanceId;

  public UserWorkFlowNodeImpl(WorkFlowNodeDefinition definition, String instanceId, String rootInstanceId,
                              WorkFlowContext context) throws WorkFlowException {
    super(definition, context);
    this.instanceId = instanceId;
    this.rootInstanceId = rootInstanceId;
  }

  public UserWorkFlowNodeImpl(String code, WorkFlowUserConfig config, String instanceId, String rootInstanceId,
                              WorkFlowTask... tasks)
      throws WorkFlowException {
    super(code, config, tasks);
    this.instanceId = instanceId;
    this.rootInstanceId = rootInstanceId;
  }

  @Override
  protected UserWorkFlowTask[] doInitTask(UserWorkFlowNodeDefinition definition, WorkFlowContext context) throws WorkFlowException {
    WorkFlowUserConfig config = definition.getConfig();
    UserWorkFlowTaskImpl[] tasks = new UserWorkFlowTaskImpl[config.member().size()];
    for (int i = 0; i < config.member().size(); i++) {
      MybatisWorkFlowUserTaskConfig userTaskConfig = new MybatisWorkFlowUserTaskConfig(config.member().get(i), config.member(), config.confirm());
      tasks[i] = new UserWorkFlowTaskImpl(getCode(), userTaskConfig, instanceId, rootInstanceId);
    }
    return tasks;
  }

  @Override
  public String getInstanceId() {
    return instanceId;
  }

  @Override
  public String getRootInstanceId() {
    return rootInstanceId;
  }

  @Override
  public String getNodeClass() {
    return "USER";
  }

}
