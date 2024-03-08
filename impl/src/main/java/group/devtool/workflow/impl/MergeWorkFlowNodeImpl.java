package group.devtool.workflow.impl;

import group.devtool.workflow.core.MergeWorkFlowNode;
import group.devtool.workflow.core.MergeWorkFlowTask;
import group.devtool.workflow.core.WorkFlowContext;
import group.devtool.workflow.core.WorkFlowNodeDefinition;
import group.devtool.workflow.core.WorkFlowTask;
import group.devtool.workflow.core.MergeWorkFlowNodeDefinition.WorkFlowMergeStrategy;
import group.devtool.workflow.core.exception.WorkFlowException;

/**
 * {@link MergeWorkFlowNode} 默认实现
 */
public class MergeWorkFlowNodeImpl extends MergeWorkFlowNode {

  private final String instanceId;

  private final String rootInstanceId;

  public MergeWorkFlowNodeImpl(WorkFlowNodeDefinition definition, String instanceId, String rootInstanceId,
                               WorkFlowContext context) throws WorkFlowException {
    super(definition, context);
    this.instanceId = instanceId;
    this.rootInstanceId = rootInstanceId;
  }

  public MergeWorkFlowNodeImpl(String code, WorkFlowMergeStrategy config, String instanceId, String rootInstanceId,
                               WorkFlowTask[] tasks) throws WorkFlowException {
    super(code, config, tasks);
    this.instanceId = instanceId;
    this.rootInstanceId = rootInstanceId;
  }

  @Override
  protected MergeWorkFlowTask doInitTask(WorkFlowNodeDefinition definition, WorkFlowContext context) throws WorkFlowException {
    return new MergeWorkFlowTaskImpl(getCode(), instanceId, rootInstanceId);
  }

  @Override
  public String getInstanceId() {
    return instanceId;
  }

  public String getRootInstanceId() {
    return rootInstanceId;
  }

  public String getNodeClass() {
    return "MERGE";
  }

}
