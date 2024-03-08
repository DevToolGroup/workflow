package group.devtool.workflow.impl;

import java.util.List;

import group.devtool.workflow.core.AbstractWorkFlowDefinition;
import group.devtool.workflow.core.WorkFlowLinkDefinition;
import group.devtool.workflow.core.WorkFlowNodeDefinition;
import group.devtool.workflow.core.exception.WorkFlowDefinitionException;
import group.devtool.workflow.core.exception.IllegalDefinitionParameter;

/**
 * {@link AbstractWorkFlowDefinition} 默认实现
 */
public class WorkFlowDefinitionImpl extends AbstractWorkFlowDefinition {

  /**
   * 流程定义编码
   */
  private final String code;

  private final String name;

  private final Integer version;

  /**
   * 流程节点定义列表
   */
  private final List<WorkFlowNodeDefinition> nodes;

  /**
   * 流程连线定义列表
   */
  private final List<WorkFlowLinkDefinition> links;

  public WorkFlowDefinitionImpl(String code, String name, Integer version, List<WorkFlowNodeDefinition> nodes,
                                List<WorkFlowLinkDefinition> links) throws WorkFlowDefinitionException {
    if (null == nodes || nodes.size() < 1 || null == links || links.size() < 1) {
      throw new IllegalDefinitionParameter("参数错误，nodes，links不能为空");
    }
    this.code = code;
    this.name = name;
    this.version = version;
    this.nodes = nodes;
    this.links = links;
  }

  @Override
  public String code() {
    return code;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public List<WorkFlowNodeDefinition> getNodes() {
    return nodes;
  }

  public List<WorkFlowLinkDefinition> getLinks() {
    return links;
  }

  @Override
  public Integer version() {
    return version;
  }
}
