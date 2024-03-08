package group.devtool.workflow.impl;

import java.util.List;

import group.devtool.workflow.core.UserWorkFlowNodeDefinition;
import group.devtool.workflow.core.exception.IllegalDefinitionParameter;

/**
 * {@link UserWorkFlowNodeDefinition} 默认实现
 */
public class UserWorkFlowNodeDefinitionImpl extends UserWorkFlowNodeDefinition {

  private final String code;

  private final String name;

  private final WorkFlowUserConfig config;

  public UserWorkFlowNodeDefinitionImpl(String code, String name, WorkFlowUserConfig config) throws IllegalDefinitionParameter {
    if (null == code || null == name || null == config) {
      throw new IllegalDefinitionParameter("用户任务节点编码、名称、配置不能为空");
    }
    this.code = code;
    this.name = name;
    this.config = config;
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
  public WorkFlowUserConfig getConfig() {
    return config;
  }

  @Override
  public String getType() {
    return "USER";
  }

  /**
   * {@link WorkFlowUserConfig} 默认实现
   */
  public static class WorkFlowUserConfigImpl implements WorkFlowUserConfig {

    private final List<String> member;

    private final Integer confirm;

    public WorkFlowUserConfigImpl(List<String> member, int confirm) {
      this.member = member;
      this.confirm = confirm;
    }

    @Override
    public List<String> member() {
      return member;
    }

    @Override
    public Integer confirm() {
      return confirm;
    }

  }

}
