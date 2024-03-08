package group.devtool.workflow.impl;

import group.devtool.workflow.core.EndWorkFlowNode;
import group.devtool.workflow.core.EndWorkFlowTask;
import group.devtool.workflow.core.WorkFlowContext;
import group.devtool.workflow.core.WorkFlowNodeDefinition;
import group.devtool.workflow.core.WorkFlowTask;
import group.devtool.workflow.core.exception.WorkFlowException;

public class EndWorkFlowNodeImpl extends EndWorkFlowNode {

  private final String instanceId;

  private final String rootInstanceId;

  public EndWorkFlowNodeImpl(WorkFlowNodeDefinition definition, String instanceId, String rootInstanceId,
                             WorkFlowContext context)
      throws WorkFlowException {
    super(definition, context);
    this.instanceId = instanceId;
    this.rootInstanceId = rootInstanceId;
  }

  public EndWorkFlowNodeImpl(String code, String instanceId, String rootInstanceId, WorkFlowTask... tasks)
      throws WorkFlowException {
    super(code, tasks);
    this.instanceId = instanceId;
    this.rootInstanceId = rootInstanceId;
  }

  @Override
  protected EndWorkFlowTask[] doInitTask(WorkFlowNodeDefinition definition) throws WorkFlowException {
    return new EndWorkFlowTask[] {
        new EndWorkFlowTaskImpl(getCode(), instanceId, rootInstanceId)
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
    return "END";
  }

}
