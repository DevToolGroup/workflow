package group.devtool.workflow.impl;

import java.util.List;

import group.devtool.workflow.core.MergeWorkFlowNodeDefinition;
import group.devtool.workflow.core.exception.IllegalDefinitionParameter;

/**
 * {@link MergeWorkFlowNodeDefinition} 默认实现
 */
public class MergeWorkFlowNodeDefinitionImpl extends MergeWorkFlowNodeDefinition {

  private final String code;

  private final String name;

  private final WorkFlowMergeStrategy config;

  public MergeWorkFlowNodeDefinitionImpl(String code, String name, WorkFlowMergeStrategy config) throws IllegalDefinitionParameter {
    if (null == code || null == name || null == config) {
      throw new IllegalDefinitionParameter("汇聚任务节点编码、名称、配置不能为空");
    }
    this.code = code;
    this.name = name;
    this.config = config;
  }

  @Override
  public String getCode() {
    return code;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public WorkFlowMergeStrategy getConfig() {
    return config;
  }

  public class MergeStrategyImpl implements WorkFlowMergeStrategy {

    private final int taskComplete;

    private final List<String> branches;

    public MergeStrategyImpl(int taskComplete, List<String> branches) {
      this.taskComplete = taskComplete;
      this.branches = branches;
    }

    @Override
    public int completeBranchNumber() {
      return taskComplete;
    }

    @Override
    public List<String> branches() {
      return branches;
    }
  }

  @Override
  public String getType() {
    return "MERGE";
  }

}
