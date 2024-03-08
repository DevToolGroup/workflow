package group.devtool.workflow.core;

import java.util.List;

import group.devtool.workflow.core.MergeWorkFlowNodeDefinition.WorkFlowMergeStrategy;
import group.devtool.workflow.core.exception.WorkFlowException;

/**
 * 流程合并任务
 */
public abstract class MergeWorkFlowTask extends AbstractWorkFlowTask {

  private String branch;

  public MergeWorkFlowTask(String taskId, String node, String instanceId) {
    super(taskId, node, instanceId);
  }

  public MergeWorkFlowTask(String branch, String taskId, String node, String instanceId, WorkFlowTaskState state) {
    super(taskId, node, instanceId, state);
    this.branch = branch;
  }

  @Override
  protected void doComplete(WorkFlowContext context) throws WorkFlowException {
    this.branch = context.getPosition();
    doCustomComplete(context);
  }

  public String getBranch() {
    return branch;
  }

  protected abstract void doCustomComplete(WorkFlowContext context) throws WorkFlowException;

  public class MergeWorkFlowTaskConfig implements WorkFlowMergeStrategy {

    private final String completeBranchCode;

    private final int completeBranchNumber;

    private final List<String> branches;

    public MergeWorkFlowTaskConfig(String completeBranchCode, int completeBranch, List<String> branches) {
      this.completeBranchNumber = completeBranch;
      this.branches = branches;
      this.completeBranchCode = completeBranchCode;
    }

    public String completeBranchCode() {
      return completeBranchCode;
    }

    @Override
    public int completeBranchNumber() {
      return completeBranchNumber;
    }

    @Override
    public List<String> branches() {
      return branches;
    }
    
  }

}
