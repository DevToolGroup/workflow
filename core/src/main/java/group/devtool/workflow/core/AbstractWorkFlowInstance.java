package group.devtool.workflow.core;

import group.devtool.workflow.core.exception.InitNodeException;
import group.devtool.workflow.core.exception.NextWorkFlowException;
import group.devtool.workflow.core.exception.WorkFlowDefinitionException;
import group.devtool.workflow.core.exception.WorkFlowException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 流程实例
 */
public abstract class AbstractWorkFlowInstance implements WorkFlowInstance {

  /**
   * 流程实例ID
   */
  private final String id;

  /**
   * 流程状态
   */
  private WorkFlowInstanceState state;

  /**
   * 流程定义
   */
  private final WorkFlowDefinition definition;

  public AbstractWorkFlowInstance(String id, WorkFlowDefinition definition) {
    this(id, definition, WorkFlowInstanceState.DOING);
  }

  public AbstractWorkFlowInstance(String id, WorkFlowDefinition definition, WorkFlowInstanceState state) {
    this.id = id;
    this.definition = definition;
    this.state = state;
  }

  @Override
  public String instanceId() {
    return id;
  }

  @Override
  public boolean done() {
    return WorkFlowInstanceState.DONE == state;
  }

  @Override
  public boolean stopped() {
    return WorkFlowInstanceState.STOP == state;
  }

  @Override
  public WorkFlowNode start(Initialize factory, WorkFlowContext context) throws WorkFlowException {
    try {
      List<WorkFlowNodeDefinition> codes = Collections.singletonList(definition.start());
      List<WorkFlowNode> nodes = initWorkFlowNode(factory, codes, context);
      state = WorkFlowInstanceState.DOING;
      return nodes.get(0);
    } catch (WorkFlowDefinitionException e) {
      throw new InitNodeException(e.getMessage());
    }
  }

  public abstract List<WorkFlowNode> initWorkFlowNode(Initialize factory, List<WorkFlowNodeDefinition> nodes,
      WorkFlowContext context) throws WorkFlowException;

  @Override
  public List<WorkFlowNode> next(Initialize factory, String nodeCode, WorkFlowContext context)
      throws WorkFlowException {
    List<WorkFlowNodeDefinition> nodes = definition.next(nodeCode, context);
    if (nodes.isEmpty()) {
      throw new NextWorkFlowException("流转的目标节点为空");
    }
    if (isEnd(nodes)) {
      state = WorkFlowInstanceState.DONE;
      return new ArrayList<>();
    }

    return initWorkFlowNode(factory, nodes, context);
  }

  private boolean isEnd(List<WorkFlowNodeDefinition> nodes) throws NextWorkFlowException {
    boolean isEnd = nodes.stream().anyMatch(i -> i instanceof EndWorkFlowNodeDefinition);
    if (isEnd && nodes.size() == 1) {
      return true;
    } else if (isEnd) {
      throw new NextWorkFlowException("在当前条件下，流程已流转至结束节点，但是存在满足当前条件的其他节点");
    } else {
      return false;
    }
  }

  public void stop() {
    state = WorkFlowInstanceState.STOP;
  }
}
