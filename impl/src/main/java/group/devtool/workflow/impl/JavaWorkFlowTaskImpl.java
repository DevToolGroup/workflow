package group.devtool.workflow.impl;

import java.io.Serializable;

import group.devtool.workflow.core.WorkFlowContext;
import group.devtool.workflow.core.TaskWorkFlowNodeDefinition.JavaTaskConfig;
import group.devtool.workflow.core.TaskWorkFlowTask.JavaWorkFlowTask;
import group.devtool.workflow.core.exception.WorkFlowException;
import group.devtool.workflow.core.exception.SerializeException;

/**
 * {@link JavaWorkFlowTask} 默认实现
 */
public class JavaWorkFlowTaskImpl extends JavaWorkFlowTask {

  private final String rootInstanceId;

  private Long completeTime;

  private final byte[] byteConfig;

  public JavaWorkFlowTaskImpl(String node, JavaTaskConfig config, String instanceId, String rootInstanceId)
      throws WorkFlowException {
    super(WorkFlowConfigurationImpl.CONFIG.idSupplier().getTaskId(), node, config, instanceId);
    this.rootInstanceId = rootInstanceId;
    this.byteConfig = getConfig(config);
  }

  public JavaWorkFlowTaskImpl(String taskId, String node, JavaTaskConfig config,
                              String instanceId, String rootInstanceId, WorkFlowTaskState state) throws SerializeException {
    super(taskId, node, config, instanceId, state);
    this.rootInstanceId = rootInstanceId;
    this.byteConfig = getConfig(config);
  }

  public String getRootInstanceId() {
    return rootInstanceId;
  }

  @Override
  public void doCustomComplete(WorkFlowContext context, Serializable result) {
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
