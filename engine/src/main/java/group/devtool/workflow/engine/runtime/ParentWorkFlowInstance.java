/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.engine.runtime;

import group.devtool.workflow.engine.WorkFlowContextImpl;
import group.devtool.workflow.engine.common.InstanceState;
import group.devtool.workflow.engine.definition.WorkFlowDefinition;
import group.devtool.workflow.engine.definition.WorkFlowNodeDefinition;

import java.util.List;

/**
 * 父流程实例
 */
public abstract class ParentWorkFlowInstance extends AbstractWorkFlowInstance {

  public ParentWorkFlowInstance(String id, WorkFlowDefinition definition) {
    super(id, definition);
  }

  public ParentWorkFlowInstance(String id, WorkFlowDefinition definition, InstanceState state) {
    super(id, definition, state);
  }

  @Override
  public boolean stopped() {
    return InstanceState.STOP == state;
  }

  @Override
  public void stop() {
    state = InstanceState.STOP;
  }

  @Override
  public List<WorkFlowNode> initWorkFlowNode(InitNode factory, List<WorkFlowNodeDefinition> nodes,
                                             WorkFlowContextImpl context)  {
    return factory.init(nodes, getInstanceId(), getInstanceId(), context);
  }

}
