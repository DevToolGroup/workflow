package group.devtool.workflow.core;

import group.devtool.workflow.core.exception.WorkFlowException;
import group.devtool.workflow.core.exception.InitTaskException;

import java.util.Arrays;

/**
 * 结束节点
 */
public abstract class EndWorkFlowNode extends AbstractWorkFlowNode {

  public EndWorkFlowNode(WorkFlowNodeDefinition definition, WorkFlowContext context) throws WorkFlowException {
    super(definition, context);
  }

  public EndWorkFlowNode(String code, WorkFlowTask... tasks) throws WorkFlowException {
    super(code, tasks);
  }

  @Override
  public boolean done() throws WorkFlowException {
    return Arrays.stream(getTasks()).allMatch(WorkFlowTask::completed);
  }

  @Override
  protected WorkFlowTask[] initTask(WorkFlowNodeDefinition definition, WorkFlowContext context)
      throws WorkFlowException {
    EndWorkFlowTask[] tasks = doInitTask(definition);
    if (tasks.length != 1) {
      throw new InitTaskException("结束节点必须有且仅有一个任务");
    }
    // 任务状态直接设置为已完成
    for (EndWorkFlowTask task : tasks) {
      task.complete(context);
    }
    return tasks;
  }

  protected abstract EndWorkFlowTask[] doInitTask(WorkFlowNodeDefinition definition) throws WorkFlowException;
}
