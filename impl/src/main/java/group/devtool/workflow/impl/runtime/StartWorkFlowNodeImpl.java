/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.impl.runtime;

import group.devtool.workflow.engine.runtime.StartWorkFlowNode;
import group.devtool.workflow.engine.runtime.StartWorkFlowTask;
import group.devtool.workflow.engine.WorkFlowContextImpl;
import group.devtool.workflow.engine.definition.WorkFlowNodeDefinition;
import group.devtool.workflow.engine.runtime.WorkFlowTask;
import group.devtool.workflow.engine.definition.WorkFlowNodeDefinition.WorkFlowNodeConfig;

/**
 * {@link StartWorkFlowNode} 默认实现
 */
public class StartWorkFlowNodeImpl extends StartWorkFlowNode {

  private final String instanceId;

  private final String rootInstanceId;

  public StartWorkFlowNodeImpl(String nodeId, WorkFlowNodeDefinition definition, String instanceId, String rootInstanceId,
                               WorkFlowContextImpl context) {
    super(nodeId, definition, context);
    this.instanceId = instanceId;
    this.rootInstanceId = rootInstanceId;
  }

  public StartWorkFlowNodeImpl(String nodeId, String nodeCode, Integer version, WorkFlowNodeConfig config, String instanceId, String rootInstanceId, WorkFlowTask... tasks) {
    super(nodeId, nodeCode, version, config, tasks);
    this.instanceId = instanceId;
    this.rootInstanceId = rootInstanceId;
  }

  @Override
  protected StartWorkFlowTask[] doInitTask(WorkFlowNodeDefinition definition)  {
    return new StartWorkFlowTask[] {
        new StartWorkFlowTaskImpl(getNodeId(), getNodeCode(), getInstanceId(), getRootInstanceId())
    };
  }

  @Override
  public String getInstanceId() {
    return instanceId;
  }

  public String getRootInstanceId() {
    return rootInstanceId;
  }

  public String getNodeClass() {
    return "START";
  }

}
