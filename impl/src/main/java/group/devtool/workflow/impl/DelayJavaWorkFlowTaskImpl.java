package group.devtool.workflow.impl;

import group.devtool.workflow.core.WorkFlowContext;
import group.devtool.workflow.core.DelayWorkFlowNodeDefinition.DelayTaskConfig;
import group.devtool.workflow.core.DelayWorkFlowTask.DelayJavaWorkFlowTask;
import group.devtool.workflow.core.exception.WorkFlowException;

/**
 * {@link DelayJavaWorkFlowTask} 默认实现类
 */
public class DelayJavaWorkFlowTaskImpl extends DelayJavaWorkFlowTask {

  private final String rootInstanceId;

  private Long completeTime;

  private final byte[] byteConfig;

  public DelayJavaWorkFlowTaskImpl(String node, DelayTaskConfig config, String instanceId, String rootInstanceId) throws WorkFlowException {
    super(WorkFlowConfigurationImpl.CONFIG.idSupplier().getTaskId(), node, config, instanceId);
    this.rootInstanceId = rootInstanceId;
    this.byteConfig = getConfig(config);
  }

  public DelayJavaWorkFlowTaskImpl(String taskId, String node,
                                   DelayTaskConfig config, String instanceId, String rootInstanceId,
                                   WorkFlowTaskState state) throws WorkFlowException {
    super(taskId, node, config, instanceId, state);
    this.rootInstanceId = rootInstanceId;
    this.byteConfig = getConfig(config);
  }

  public Long delay() {
    return getConfig().getTime() * getConfig().getUnit().getMills();
  }

  public String getRootInstanceId() {
    return rootInstanceId;
  }

  @Override
  public void doCustomComplete(WorkFlowContext context) {
    // do nothing
    completeTime = System.currentTimeMillis();
  }

  public String getTaskClass() {
    return "JAVA";
  }

  public String getCompleteUser() {
    return null;
  }

  public Long getCompleteTime() {
    return completeTime;
  }

  public byte[] getTaskConfig() {
    return byteConfig;
  }

}
