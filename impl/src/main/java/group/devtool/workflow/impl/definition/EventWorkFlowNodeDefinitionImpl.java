/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.impl.definition;

import group.devtool.workflow.engine.definition.EventWorkFlowNodeDefinition;
import group.devtool.workflow.engine.exception.IllegalWorkFlowDefinition;

import java.util.List;

/**
 * {@link EventWorkFlowNodeDefinition} 默认实现
 */
public class EventWorkFlowNodeDefinitionImpl extends EventWorkFlowNodeDefinition {

  private final String code;

  private final String name;

  private final EventWorkFlowConfig config;

  public EventWorkFlowNodeDefinitionImpl(String code, String name, EventWorkFlowConfig config) throws IllegalWorkFlowDefinition {
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
  public EventWorkFlowConfig getConfig() {
    return config;
  }

  @Override
  public String getType() {
    return "USER";
  }

  /**
   * {@link EventWorkFlowConfig} 默认实现
   */
  public static class EventWorkFlowConfigImpl implements EventWorkFlowConfig {

    private List<String> events;

    public EventWorkFlowConfigImpl() {

    }

    public EventWorkFlowConfigImpl(List<String> events) {
      this.events = events;
    }

    @Override
    public List<String> getEvents() {
      return events;
    }

    public void setEvents(List<String> events) {
      this.events = events;
    }
  }

}
