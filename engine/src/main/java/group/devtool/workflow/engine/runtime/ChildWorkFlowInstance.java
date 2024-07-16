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
 * 嵌套子流程实例
 */
public abstract class ChildWorkFlowInstance extends AbstractWorkFlowInstance {

  /**
   * 根流程实例ID
   */
  private final String rootInstanceId;

  /**
   * 子流程派生任务ID
   */
  private final String parentId;

  public ChildWorkFlowInstance(String id, String parentId, String rootInstanceId, WorkFlowDefinition definition) {
    this(id, parentId, rootInstanceId, definition, InstanceState.DOING);
  }

  public ChildWorkFlowInstance(String id, String parentId, String rootInstanceId, WorkFlowDefinition definition,
                               InstanceState state) {
    super(id, definition, state);
    this.parentId = parentId;
    this.rootInstanceId = rootInstanceId;
  }

  /**
   * @return 父流程节点任务ID
   */
  public String getParentId() {
    return parentId;
  }

  /**
   * @return 根流程实例ID
   */
  public String getRootInstanceId() {
    return rootInstanceId;
  }

  @Override
  public boolean stopped() {
    return false;
  }

  @Override
  public void stop() {
    throw new UnsupportedOperationException("子流程不支持停止");
  }

  @Override
  public List<WorkFlowNode> initWorkFlowNode(InitNode factory, List<WorkFlowNodeDefinition> nodes,
                                             WorkFlowContextImpl context) {
    return factory.init(nodes, getInstanceId(), rootInstanceId, context);
  }

}
