package group.devtool.workflow.core;

import group.devtool.workflow.core.exception.WorkFlowException;

/**
 * {@link WorkFlowTask} 抽象实现类，用于定义流程任务的基本业务流程
 */
public abstract class AbstractWorkFlowTask implements WorkFlowTask {

  private final String taskId;

  private final String node;

  private final String instanceId;

  private WorkFlowTaskState state;

  public AbstractWorkFlowTask(String taskId, String node, String instanceId) {
    this(taskId, node, instanceId, WorkFlowTaskState.DOING);
  }

  public AbstractWorkFlowTask(String taskId, String node, String instanceId, WorkFlowTaskState state) {
    this.taskId = taskId;
    this.node = node;
    this.state = state;
    this.instanceId = instanceId;
  }

  @Override
  public void complete(WorkFlowContext context) throws WorkFlowException {
    doComplete(context);
    state = WorkFlowTaskState.DONE;
  }

  protected abstract void doComplete(WorkFlowContext context) throws WorkFlowException;

  @Override
  public String getTaskId() {
    return taskId;
  }

  @Override
  public String getNodeCode() {
    return node;
  }

  @Override
  public String getInstanceId() {
    return instanceId;
  }

  @Override
  public boolean completed() {
    return WorkFlowTaskState.DONE == state;
  }
}
