package group.devtool.workflow.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import group.devtool.workflow.core.ChildWorkFlowNodeDefinition.WorkFlowChildConfig;
import group.devtool.workflow.core.exception.WorkFlowException;

/**
 * 嵌套子流程抽象类，子类通过实现{@code doInitTask}完成任务的初始化
 */
public abstract class AbstractChildWorkFlowNode extends AbstractWorkFlowNode implements ChildWorkFlowNode {

  private WorkFlowChildConfig config;

  public AbstractChildWorkFlowNode(WorkFlowNodeDefinition definition, WorkFlowContext context)
      throws WorkFlowException {
    super(definition, context);
    this.config = ((ChildWorkFlowNodeDefinition) definition).getConfig();
  }

  public AbstractChildWorkFlowNode(String code, WorkFlowTask... tasks) throws WorkFlowException {
    super(code, tasks);
  }

  @Override
  public List<WorkFlowInstance> instances(ChildFactory childFactory) throws WorkFlowException {
    List<WorkFlowInstance> instances = new ArrayList<>();
    for (WorkFlowTask task : getTasks()) {
      instances.add(childFactory.apply(config.childCode(), task.getTaskId()));
    }
    return instances;
  }

  @Override
  public boolean done() throws WorkFlowException {
    return Arrays.asList(getTasks()).stream().allMatch(WorkFlowTask::completed);
  }

  @Override
  protected WorkFlowTask[] initTask(WorkFlowNodeDefinition definition, WorkFlowContext context)
      throws WorkFlowException {
    ChildWorkFlowNodeDefinition childDefinition = (ChildWorkFlowNodeDefinition) definition;
    WorkFlowChildConfig config = childDefinition.getConfig();
    ChildWorkFlowTask[] tasks = doInitTask(config.getTaskNumber(), context);
    return tasks;
  }

  protected abstract ChildWorkFlowTask[] doInitTask(int taskNumber, WorkFlowContext context) throws WorkFlowException;

}
