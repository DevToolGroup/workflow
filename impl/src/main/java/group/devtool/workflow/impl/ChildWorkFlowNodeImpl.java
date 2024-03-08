package group.devtool.workflow.impl;

import group.devtool.workflow.core.*;
import group.devtool.workflow.core.exception.WorkFlowException;

/**
 * {@link ChildWorkFlowNode} 嵌套子流程节点默认实现类
 */
public class ChildWorkFlowNodeImpl extends AbstractChildWorkFlowNode {

  private final String instanceId;

  private final String rootInstanceId;

  public ChildWorkFlowNodeImpl(WorkFlowNodeDefinition definition, String instanceId, String rootInstanceId,
                               WorkFlowContext context)
      throws WorkFlowException {
    super(definition, context);
    this.rootInstanceId = rootInstanceId;
    this.instanceId = instanceId;
  }

  public ChildWorkFlowNodeImpl(String code, String instanceId, String rootInstanceId, WorkFlowTask... tasks)
      throws WorkFlowException {
    super(code, tasks);
    this.rootInstanceId = rootInstanceId;
    this.instanceId = instanceId;
  }

  @Override
  public String getInstanceId() {
    return instanceId;
  }

  public String getRootInstanceId() {
    return rootInstanceId;
  }

  @Override
  protected ChildWorkFlowTask[] doInitTask(int taskNumber, WorkFlowContext context) throws WorkFlowException {
    ChildWorkFlowTask[] tasks = new ChildWorkFlowTaskImpl[taskNumber];
    for (int i = 0; i < taskNumber; i++) {
      tasks[i] = new ChildWorkFlowTaskImpl(getCode(), instanceId, rootInstanceId);
    }
    return tasks;
  }

  public String getNodeClass() {
    return "CHILD";
  }

}
