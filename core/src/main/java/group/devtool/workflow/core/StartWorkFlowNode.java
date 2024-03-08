package group.devtool.workflow.core;

import group.devtool.workflow.core.exception.WorkFlowException;
import group.devtool.workflow.core.exception.InitTaskException;

import java.util.Arrays;

/**
 * 流程启动节点
 */
public abstract class StartWorkFlowNode extends AbstractWorkFlowNode {

  public StartWorkFlowNode(WorkFlowNodeDefinition definition, WorkFlowContext context)
      throws WorkFlowException {
    super(definition, context);
  }

  public StartWorkFlowNode(String code, WorkFlowTask... tasks) throws WorkFlowException {
    super(code, tasks);
  }

  @Override
  public boolean done() throws WorkFlowException {
    return Arrays.asList(getTasks()).stream().allMatch(WorkFlowTask::completed);
  }

  @Override
  public void beforeComplete() {
    
  }

  @Override
  protected WorkFlowTask[] initTask(WorkFlowNodeDefinition definition, WorkFlowContext context)
      throws WorkFlowException {
    StartWorkFlowTask[] tasks = doInitTask(definition);
    if (tasks.length != 1) {
      throw new InitTaskException("开始节点必须有且仅有一个任务");
    }
    // 任务状态直接设置为已完成
    for (StartWorkFlowTask task : tasks) {
      task.complete(context);
    }
    return tasks;
  }

  protected abstract StartWorkFlowTask[] doInitTask(WorkFlowNodeDefinition definition) throws WorkFlowException;

}
