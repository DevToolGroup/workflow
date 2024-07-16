/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.impl.runtime;

import group.devtool.workflow.engine.*;
import group.devtool.workflow.engine.definition.EventWorkFlowNodeDefinition;
import group.devtool.workflow.engine.definition.EventWorkFlowNodeDefinition.EventWorkFlowConfig;
import group.devtool.workflow.engine.definition.WorkFlowNodeDefinition;
import group.devtool.workflow.engine.runtime.EventWorkFlowNode;
import group.devtool.workflow.engine.runtime.EventWorkFlowTask;
import group.devtool.workflow.engine.runtime.WorkFlowTask;
import group.devtool.workflow.impl.runtime.EventWorkFlowTaskImpl.EventWorkFlowTaskConfigImpl;

/**
 * {@link EventWorkFlowNode} 默认实现
 */
public class EventWorkFlowNodeImpl extends EventWorkFlowNode {

  private final String instanceId;

  private final String rootInstanceId;

  public EventWorkFlowNodeImpl(String nodeId, WorkFlowNodeDefinition definition,
                               String instanceId, String rootInstanceId,
                               WorkFlowContextImpl context) {
    super(nodeId, definition, context);
    this.instanceId = instanceId;
    this.rootInstanceId = rootInstanceId;
  }

  public EventWorkFlowNodeImpl(String nodeId, String nodeCode, Integer version,
                               EventWorkFlowConfig config,
                               String instanceId, String rootInstanceId,
															 WorkFlowTask... tasks) {
    super(nodeId, nodeCode, version, config, tasks);
    this.instanceId = instanceId;
    this.rootInstanceId = rootInstanceId;
  }

  @Override
  protected EventWorkFlowTask[] doInitTask(EventWorkFlowNodeDefinition definition, WorkFlowContextImpl context)  {
    EventWorkFlowConfig config = (EventWorkFlowConfig)getConfig();
    EventWorkFlowTaskImpl[] tasks = new EventWorkFlowTaskImpl[config.getEvents().size()];
    for (int i = 0; i < config.getEvents().size(); i++) {
      EventWorkFlowTaskConfigImpl eventTaskConfig = new EventWorkFlowTaskConfigImpl(config.getEvents().get(i));
      tasks[i] = new EventWorkFlowTaskImpl(getNodeId(), getNodeCode(), eventTaskConfig, instanceId, rootInstanceId);
    }
    return tasks;
  }

  @Override
  public String getInstanceId() {
    return instanceId;
  }

  @Override
  public String getRootInstanceId() {
    return rootInstanceId;
  }

  @Override
  public String getNodeClass() {
    return "USER";
  }

}
