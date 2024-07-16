/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.impl.definition;

import group.devtool.workflow.engine.definition.StartWorkFlowNodeDefinition;
import group.devtool.workflow.engine.exception.IllegalWorkFlowDefinition;

/**
 * {@link StartWorkFlowNodeDefinition} 默认实现
 */
public class StartWorkFlowNodeDefinitionImpl extends StartWorkFlowNodeDefinition {

  private final String code;

  private final String name;

  public StartWorkFlowNodeDefinitionImpl(String code, String name) throws IllegalWorkFlowDefinition {
    if (null == code || null == name) {
      throw new IllegalWorkFlowDefinition("节点编码、名称不能为空");
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
  public WorkFlowNodeConfig getConfig() {
    return null;
  }

  @Override
  public String getType() {
    return "START";
  }
}
