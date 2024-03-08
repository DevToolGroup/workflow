package group.devtool.workflow.impl;

import group.devtool.workflow.core.DelayWorkFlowNode;
import group.devtool.workflow.core.DelayWorkFlowNodeDefinition;
import group.devtool.workflow.core.DelayWorkFlowTask;
import group.devtool.workflow.core.WorkFlowContext;
import group.devtool.workflow.core.WorkFlowNodeDefinition;
import group.devtool.workflow.core.WorkFlowScheduler;
import group.devtool.workflow.core.WorkFlowScheduler.DelayItem;
import group.devtool.workflow.core.WorkFlowTask;
import group.devtool.workflow.core.exception.WorkFlowException;
import group.devtool.workflow.core.exception.NotSupportWorkFlowTaskClass;
import group.devtool.workflow.impl.WorkFlowSchedulerImpl.DelayItemImpl;
import group.devtool.workflow.core.DelayWorkFlowNodeDefinition.DelayTaskConfig;
import group.devtool.workflow.core.DelayWorkFlowNodeDefinition.TaskWorker;

public class DelayWorkFlowNodeImpl extends DelayWorkFlowNode {

  private final String instanceId;

  private final String rootInstanceId;

  public DelayWorkFlowNodeImpl(WorkFlowNodeDefinition definition, String instanceId, String rootInstanceId,
                               WorkFlowContext context) throws WorkFlowException {
    super(definition, context);
    this.instanceId = instanceId;
    this.rootInstanceId = rootInstanceId;
  }

  public DelayWorkFlowNodeImpl(String code, String instanceId, String rootInstanceId, WorkFlowTask... tasks)
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
  protected DelayWorkFlowTask doInitTask(DelayWorkFlowNodeDefinition definition, WorkFlowContext context)
      throws WorkFlowException {
    DelayTaskConfig config = definition.getConfig();
    if (TaskWorker.JAVA == config.getTask().getWorker()) {
      return new DelayJavaWorkFlowTaskImpl(getCode(), config, instanceId, rootInstanceId);
    }
    throw new NotSupportWorkFlowTaskClass("任务节点执行器类型暂不支持，执行器类型：" + config.getTask().getWorker().name());
  }

  public String getNodeClass() {
    return "DELAY";
  }

  @Override
  protected WorkFlowScheduler getScheduler() {
    return WorkFlowConfigurationImpl.CONFIG.taskScheduler();
  }

  @Override
  protected DelayItem doInitDelayItem(DelayWorkFlowTask task) {
    DelayJavaWorkFlowTaskImpl myTask = (DelayJavaWorkFlowTaskImpl) task;
    return new DelayItemImpl(myTask.delay(), myTask.getTaskId(), myTask.getRootInstanceId());
  }

}
