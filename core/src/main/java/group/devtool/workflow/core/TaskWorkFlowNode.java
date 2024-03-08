package group.devtool.workflow.core;

import group.devtool.workflow.core.exception.WorkFlowException;

import java.util.Arrays;

/**
 * 抽象任务节点，在初始化后直接执行子任务
 */
public abstract class TaskWorkFlowNode extends AbstractWorkFlowNode {

  public TaskWorkFlowNode(WorkFlowNodeDefinition definition, WorkFlowContext context) throws WorkFlowException {
    super(definition, context);
  }

  public TaskWorkFlowNode(String code, WorkFlowTask... tasks) throws WorkFlowException {
    super(code, tasks);
  }

  @Override
  public boolean done() throws WorkFlowException {
    return Arrays.asList(getTasks()).stream().allMatch(WorkFlowTask::completed);
  }

  @Override
  protected WorkFlowTask[] initTask(WorkFlowNodeDefinition definition, WorkFlowContext context)
      throws WorkFlowException {
    TaskWorkFlowNodeDefinition taskDefinition = (TaskWorkFlowNodeDefinition) definition;
    TaskWorkFlowTask task = doInitTask(taskDefinition, context);
    task.complete(context);
    return new WorkFlowTask[] { task };
  }

  /**
   * 根据定义初始化任务实例
   * 
   * @param definition 任务配置
   * @param context
   * @return 任务实例
   * @throws WorkFlowException
   */
  protected abstract TaskWorkFlowTask doInitTask(TaskWorkFlowNodeDefinition definition, WorkFlowContext context)
      throws WorkFlowException;

}
