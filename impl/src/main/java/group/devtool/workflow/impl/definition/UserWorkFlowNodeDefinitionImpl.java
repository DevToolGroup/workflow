/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.impl.definition;

import java.util.List;

import group.devtool.workflow.engine.definition.UserWorkFlowNodeDefinition;
import group.devtool.workflow.engine.exception.IllegalWorkFlowDefinition;

/**
 * {@link UserWorkFlowNodeDefinition} 默认实现
 */
public class UserWorkFlowNodeDefinitionImpl extends UserWorkFlowNodeDefinition {

  private final String code;

  private final String name;

  private final UserWorkFlowConfig config;

  public UserWorkFlowNodeDefinitionImpl(String code, String name, UserWorkFlowConfig config) throws IllegalWorkFlowDefinition {
    if (null == code || null == name || null == config) {
      throw new IllegalWorkFlowDefinition("用户任务节点编码、名称、配置不能为空");
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
  public UserWorkFlowConfig getConfig() {
    return config;
  }

  @Override
  public String getType() {
    return "USER";
  }

  /**
   * {@link UserWorkFlowConfig} 默认实现
   */
  public static class UserWorkFlowConfigImpl implements UserWorkFlowConfig {

    private List<String> member;

    private Integer confirm;

    public UserWorkFlowConfigImpl() {

    }

    public UserWorkFlowConfigImpl(List<String> member, int confirm) {
      this.member = member;
      this.confirm = confirm;
    }

    @Override
    public List<String> getMember() {
      return member;
    }

    @Override
    public Integer getConfirm() {
      return confirm;
    }

    public void setMember(List<String> member) {
      this.member = member;
    }

    public void setConfirm(Integer confirm) {
      this.confirm = confirm;
    }
  }

}
