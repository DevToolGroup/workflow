package group.devtool.workflow.core;

import java.util.Arrays;

import group.devtool.workflow.core.WorkFlowScheduler.DelayItem;
import group.devtool.workflow.core.exception.WorkFlowException;

/**
 * 延时节点
 */
public abstract class DelayWorkFlowNode extends AbstractWorkFlowNode {

  public DelayWorkFlowNode(WorkFlowNodeDefinition definition, WorkFlowContext context) throws WorkFlowException {
    super(definition, context);
  }

  public DelayWorkFlowNode(String code, WorkFlowTask... tasks) throws WorkFlowException {
    super(code, tasks);
  }

  @Override
  public boolean done() throws WorkFlowException {
    return Arrays.asList(getTasks()).stream().allMatch(WorkFlowTask::completed);
  }

  @Override
  protected WorkFlowTask[] initTask(WorkFlowNodeDefinition definition, WorkFlowContext context)
      throws WorkFlowException {
    DelayWorkFlowNodeDefinition delayDefinition = (DelayWorkFlowNodeDefinition) definition;
    DelayWorkFlowTask task = doInitTask(delayDefinition, context);
    getScheduler().addTask(doInitDelayItem(task));
    return new WorkFlowTask[] { task };
  }

  /**
   * @return 流程调度器
   */
  protected abstract WorkFlowScheduler getScheduler();

  /**
   * 初始化延时任务
   * 
   * @param task 流程任务实例
   * @return 延时任务实例
   */
  protected abstract DelayItem doInitDelayItem(DelayWorkFlowTask task);

  /**
   * 根据定义初始化任务实例
   * 
   * @param definition 任务配置
   * @param context 流程上下文
   * @return 任务实例
   * @throws WorkFlowException 流程异常
   */
  protected abstract DelayWorkFlowTask doInitTask(DelayWorkFlowNodeDefinition definition, WorkFlowContext context)
      throws WorkFlowException;

}
