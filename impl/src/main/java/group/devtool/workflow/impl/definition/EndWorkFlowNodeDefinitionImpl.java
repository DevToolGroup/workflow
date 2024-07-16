/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.impl.definition;

import group.devtool.workflow.engine.definition.EndWorkFlowNodeDefinition;
import group.devtool.workflow.engine.exception.IllegalWorkFlowDefinition;

/**
 * 流程结束节点定义
 */
public class EndWorkFlowNodeDefinitionImpl extends EndWorkFlowNodeDefinition {

  private final String code;

  private final String name;

  public EndWorkFlowNodeDefinitionImpl(String code, String name) throws IllegalWorkFlowDefinition {
    if (null == code || null == name) {
      throw new IllegalWorkFlowDefinition("结束节点编码、名称不能为空");
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
  public WorkFlowNodeConfig getConfig() {
    return null;
  }

}
