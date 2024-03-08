package group.devtool.workflow.impl;

import group.devtool.workflow.core.StartWorkFlowNodeDefinition;
import group.devtool.workflow.core.exception.WorkFlowDefinitionException;
import group.devtool.workflow.core.exception.IllegalDefinitionParameter;

/**
 * {@link StartWorkFlowNodeDefinition} 默认实现
 */
public class StartWorkFlowNodeDefinitionImpl extends StartWorkFlowNodeDefinition {

  private final String code;

  private final String name;

  public StartWorkFlowNodeDefinitionImpl(String code, String name) throws WorkFlowDefinitionException {
    if (null == code || null == name) {
      throw new IllegalDefinitionParameter("节点编码、名称不能为空");
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
  public Object getConfig() {
    return null;
  }

  @Override
  public String getType() {
    return "START";
  }
}
