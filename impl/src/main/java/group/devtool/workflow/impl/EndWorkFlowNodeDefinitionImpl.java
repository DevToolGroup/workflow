package group.devtool.workflow.impl;

import group.devtool.workflow.core.EndWorkFlowNodeDefinition;
import group.devtool.workflow.core.exception.WorkFlowDefinitionException;
import group.devtool.workflow.core.exception.IllegalDefinitionParameter;

/**
 * 流程结束节点定义
 */
public class EndWorkFlowNodeDefinitionImpl extends EndWorkFlowNodeDefinition {

  private final String code;

  private final String name;

  public EndWorkFlowNodeDefinitionImpl(String code, String name) throws WorkFlowDefinitionException {
    if (null == code || null == name) {
      throw new IllegalDefinitionParameter("结束节点编码、名称不能为空");
    }
    this.code = code;
    this.name = name;
  }

  @Override
  public String getCode() {
    return code;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getType() {
    return "END";
  }

  @Override
  public Object getConfig() {
    return null;
  }

}
