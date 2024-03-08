package group.devtool.workflow.core;

import group.devtool.workflow.core.exception.WorkFlowException;

import java.util.List;

/**
 * 父流程实例
 */
public abstract class ParentWorkFlowInstance extends AbstractWorkFlowInstance {

  public ParentWorkFlowInstance(String id, WorkFlowDefinition definition) {
    super(id, definition);
  }

  public ParentWorkFlowInstance(String id, WorkFlowDefinition definition, WorkFlowInstanceState state) {
    super(id, definition, state);
  }

  @Override
  public List<WorkFlowNode> initWorkFlowNode(Initialize factory, List<WorkFlowNodeDefinition> nodes,
      WorkFlowContext context) throws WorkFlowException {
    return factory.init(nodes, instanceId(), instanceId(), context);
  }

}
