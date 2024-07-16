/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.impl.runtime;

import group.devtool.workflow.engine.runtime.EndWorkFlowNode;
import group.devtool.workflow.engine.runtime.EndWorkFlowTask;
import group.devtool.workflow.engine.WorkFlowContextImpl;
import group.devtool.workflow.engine.definition.WorkFlowNodeDefinition;
import group.devtool.workflow.engine.runtime.WorkFlowTask;
import group.devtool.workflow.engine.definition.WorkFlowNodeDefinition.WorkFlowNodeConfig;


public class EndWorkFlowNodeImpl extends EndWorkFlowNode {

  private final String instanceId;

  private final String rootInstanceId;

  public EndWorkFlowNodeImpl(String nodeId, WorkFlowNodeDefinition definition,
                             String instanceId,
                             String rootInstanceId,
                             WorkFlowContextImpl context) {
    super(nodeId, definition, context);
    this.instanceId = instanceId;
    this.rootInstanceId = rootInstanceId;
  }

  public EndWorkFlowNodeImpl(String nodeId, String nodeCode, Integer version, WorkFlowNodeConfig config, String instanceId, String rootInstanceId, WorkFlowTask... tasks) {
    super(nodeId, nodeCode, version, config, tasks);
    this.instanceId = instanceId;
    this.rootInstanceId = rootInstanceId;
  }

  @Override
  protected EndWorkFlowTask[] doInitTask(WorkFlowNodeDefinition definition)  {
    return new EndWorkFlowTask[] {
        new EndWorkFlowTaskImpl(getNodeId(), getNodeCode(), instanceId, rootInstanceId)
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
    return "END";
  }

}
