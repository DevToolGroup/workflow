package group.devtool.workflow.core;

import group.devtool.workflow.core.exception.WorkFlowException;

import java.util.List;

/**
 * 嵌套子流程实例
 */
public abstract class ChildWorkFlowInstance extends AbstractWorkFlowInstance {

  /**
   * 根流程实例ID
   */
  private final String rootId;

  /**
   * 子流程派生任务ID
   */
  private final String parentId;

  public ChildWorkFlowInstance(String id, String parentId, String rootId, WorkFlowDefinition definition) {
    this(id, parentId, rootId, definition, WorkFlowInstanceState.DOING);
  }

  public ChildWorkFlowInstance(String id, String parentId, String rootId, WorkFlowDefinition definition,
      WorkFlowInstanceState state) {
    super(id, definition, state);
    this.parentId = parentId;
    this.rootId = rootId;
  }

  /**
   * @return 父流程节点任务ID
   */
  public String parentId() {
    return parentId;
  }

  /**
   * @return 根流程实例ID
   */
  public String rootId() {
    return rootId;
  }

  @Override
  public List<WorkFlowNode> initWorkFlowNode(Initialize factory, List<WorkFlowNodeDefinition> nodes,
      WorkFlowContext context) throws WorkFlowException {
    return factory.init(nodes, instanceId(), rootId, context);
  }

}
