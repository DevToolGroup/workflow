package group.devtool.workflow.impl;

import group.devtool.workflow.core.StartWorkFlowNode;
import group.devtool.workflow.core.StartWorkFlowTask;
import group.devtool.workflow.core.WorkFlowContext;
import group.devtool.workflow.core.WorkFlowNodeDefinition;
import group.devtool.workflow.core.WorkFlowTask;
import group.devtool.workflow.core.exception.WorkFlowException;

/**
 * {@link StartWorkFlowNode} 默认实现
 */
public class StartWorkFlowNodeImpl extends StartWorkFlowNode {

  private final String instanceId;

  private final String rootInstanceId;

  public StartWorkFlowNodeImpl(WorkFlowNodeDefinition definition, String instanceId, String rootInstanceId,
                               WorkFlowContext context) throws WorkFlowException {
    super(definition, context);
    this.instanceId = instanceId;
    this.rootInstanceId = rootInstanceId;
  }

  public StartWorkFlowNodeImpl(String code, String instanceId, String rootInstanceId, WorkFlowTask... tasks)
      throws WorkFlowException {
    super(code, tasks);
    this.instanceId = instanceId;
    this.rootInstanceId = rootInstanceId;
  }

  @Override
  protected StartWorkFlowTask[] doInitTask(WorkFlowNodeDefinition definition) throws WorkFlowException {
    return new StartWorkFlowTask[] {
        new StartWorkFlowTaskImpl(getCode(), getInstanceId(), getRootInstanceId())
    };
  }

  @Override
  public String getInstanceId() {
    return instanceId;
  }

  public String getRootInstanceId() {
    return rootInstanceId;
  }

  public String getNodeClass() {
    return "START";
  }

}
