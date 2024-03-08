package group.devtool.workflow.core;

import java.util.HashSet;
import java.util.Set;

import group.devtool.workflow.core.MergeWorkFlowNodeDefinition.WorkFlowMergeStrategy;
import group.devtool.workflow.core.exception.WorkFlowException;

/**
 * 业务场景举例：
 * - A - B - D - E
 * | |
 * |- C - |
 * 0. B，C两个节点是并行节点，
 * 1. 当A节点执行完成后，流程进入 B、C两个节点，
 * 2. E节点需要在B、C两个节点都执行完之后才初始化
 * 可增加D节点（合并节点）实现以上业务场景
 * 
 * 实现原理：
 * 每个分支流转至合并节点时，初始化一个状态为已完成的合并任务节点，当已完成节点任务数量满足合并节点通过的条件时，流程流转至下一个节点
 */
public abstract class MergeWorkFlowNode extends AbstractWorkFlowNode {

  private final WorkFlowMergeStrategy config;

  public MergeWorkFlowNode(WorkFlowNodeDefinition definition, WorkFlowContext context) throws WorkFlowException {
    super(definition, context);
    MergeWorkFlowNodeDefinition mdf = (MergeWorkFlowNodeDefinition) definition;
    this.config = mdf.getConfig();
  }

  public MergeWorkFlowNode(String code, WorkFlowMergeStrategy config, WorkFlowTask[] tasks) throws WorkFlowException {
    super(code, tasks);
    this.config = config;
  }

  @Override
  public boolean done() throws WorkFlowException  {
    Set<String> branches = new HashSet<>();
    for (WorkFlowTask task: getTasks()) {
      MergeWorkFlowTask mt = (MergeWorkFlowTask) task;
      if (mt.completed() && config.branches().contains(mt.getBranch())) {
        branches.add(mt.getBranch());
      }
    }
    return branches.size() >= config.completeBranchNumber();
  }

  /**
   * 每次流转至合并节点，则生成一个节点任务实例，并且设置其任务状态为已完成
   */
  @Override
  protected WorkFlowTask[] initTask(WorkFlowNodeDefinition definition, WorkFlowContext context)
      throws WorkFlowException {
    MergeWorkFlowTask task = doInitTask(definition, context);
    task.complete(context);
    return new WorkFlowTask[] { task };
  }

  protected abstract MergeWorkFlowTask doInitTask(WorkFlowNodeDefinition definition, WorkFlowContext context) throws WorkFlowException;

}
