package group.devtool.workflow.core;

import group.devtool.workflow.core.exception.WorkFlowDefinitionException;
import group.devtool.workflow.core.exception.IllegalDefinitionParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 流程定义抽象类主要实现流程流转，以及规范流程流转过程中需要子类提供的能力
 */
public abstract class AbstractWorkFlowDefinition implements WorkFlowDefinition {

  /**
   * @return 流程节点定义
   */
  protected abstract List<WorkFlowNodeDefinition> getNodes();

  @Override
  public WorkFlowNodeDefinition start() throws WorkFlowDefinitionException {
    return get(getNodes(), StartWorkFlowNodeDefinition.class);
  }

  @Override
  public WorkFlowNodeDefinition end() throws WorkFlowDefinitionException {
    return get(getNodes(), EndWorkFlowNodeDefinition.class);
  }

  /**
   * @return 流程节点间连线定义
   */
  protected abstract List<WorkFlowLinkDefinition> getLinks();

  /**
   * 流程节点流转，这里规定了流程节点间连线上需要提供条件表达式，帮忙完成后续节点的计算
   */
  @Override
  public List<WorkFlowNodeDefinition> next(String nodeCode, WorkFlowContext context) {
    List<String> nodeCodes = new ArrayList<>();
    for (WorkFlowLinkDefinition link : getLinks()) {
      if (link.from(nodeCode) && link.match(context)) {
        nodeCodes.add(link.getTarget());
      }
    }
    return getNodes().stream().filter(i -> nodeCodes.contains(i.getCode())).collect(Collectors.toList());
  }

  private WorkFlowNodeDefinition get(List<WorkFlowNodeDefinition> nodes, Class<? extends WorkFlowNodeDefinition> clazz)
      throws WorkFlowDefinitionException {
    WorkFlowNodeDefinition special = null;
    for (WorkFlowNodeDefinition node : nodes) {
      if (!(clazz.isInstance(node))) {
        continue;
      }
      if (null != special) {
        throw new IllegalDefinitionParameter("流程定义中必须存在有且仅有一个开始节点，一个结束节点");
      }
      special = node;

    }
    if (null == special) {
      throw new IllegalDefinitionParameter("流程定义中必须存在有且仅有一个开始节点，一个结束节点");
    }
    return special;
  }
}
