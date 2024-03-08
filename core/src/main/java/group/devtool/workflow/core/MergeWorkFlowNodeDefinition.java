package group.devtool.workflow.core;


import java.io.Serializable;
import java.util.List;

/**
 * 流程分支合并节点定义
 */
public abstract class MergeWorkFlowNodeDefinition implements WorkFlowNodeDefinition {

  public abstract WorkFlowMergeStrategy getConfig();

  public interface WorkFlowMergeStrategy extends Serializable {

    /**
     * 当到达合并节点的分支数量 大于等于 完成分支数量，说明节点已完成需要继续往下流转
     * 
     * @return 完成分支数量
     */
		int completeBranchNumber();

    /**
     *
     * @return 合并节点上游节点的编码集合
     */
		List<String> branches();

  }
}
