package group.devtool.workflow.impl;

import group.devtool.workflow.core.TaskWorkFlowNode;
import group.devtool.workflow.core.TaskWorkFlowNodeDefinition;
import group.devtool.workflow.core.TaskWorkFlowTask;
import group.devtool.workflow.core.WorkFlowContext;
import group.devtool.workflow.core.WorkFlowNodeDefinition;
import group.devtool.workflow.core.WorkFlowTask;
import group.devtool.workflow.core.TaskWorkFlowNodeDefinition.JavaTaskConfig;
import group.devtool.workflow.core.TaskWorkFlowNodeDefinition.TaskWorker;
import group.devtool.workflow.core.TaskWorkFlowNodeDefinition.WorkFlowTaskConfig;
import group.devtool.workflow.core.exception.WorkFlowException;
import group.devtool.workflow.core.exception.NotSupportWorkFlowTaskClass;

/**
 * 任务节点
 */
public class TaskWorkFlowNodeImpl extends TaskWorkFlowNode {

  private final String instanceId;

  private final String rootInstanceId;

  public TaskWorkFlowNodeImpl(WorkFlowNodeDefinition definition, String instanceId, String rootInstanceId,
                              WorkFlowContext context) throws WorkFlowException {
    super(definition, context);
    this.instanceId = instanceId;
    this.rootInstanceId = rootInstanceId;
  }

  public TaskWorkFlowNodeImpl(String code, String instanceId, String rootInstanceId, WorkFlowTask... tasks)
      throws WorkFlowException {
    super(code, tasks);
    this.instanceId = instanceId;
    this.rootInstanceId = rootInstanceId;
  }

  @Override
  public String getInstanceId() {
    return instanceId;
  }

  public String getRootInstanceId() {
    return rootInstanceId;
  }

  @Override
  protected TaskWorkFlowTask doInitTask(TaskWorkFlowNodeDefinition definition, WorkFlowContext context)
      throws WorkFlowException {
    WorkFlowTaskConfig config = definition.getConfig();
    if (TaskWorker.JAVA == config.getWorker()) {
      JavaTaskConfig jc = (JavaTaskConfig) config;
      return new JavaWorkFlowTaskImpl(getCode(), jc, instanceId, rootInstanceId);
    }
    throw new NotSupportWorkFlowTaskClass("任务节点执行器类型暂不支持，执行器类型：" + config.getWorker().name());
  }

  public String getNodeClass() {
    return "TASK";
  }

}
