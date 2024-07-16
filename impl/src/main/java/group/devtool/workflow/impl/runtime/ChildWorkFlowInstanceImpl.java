/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.impl.runtime;

import group.devtool.workflow.engine.common.InstanceState;
import group.devtool.workflow.engine.runtime.ChildWorkFlowInstance;
import group.devtool.workflow.engine.definition.WorkFlowDefinition;

/**
 * {@link ChildWorkFlowInstance} 默认实现类
 */
public class ChildWorkFlowInstanceImpl extends ChildWorkFlowInstance {

  private Long id;


  public ChildWorkFlowInstanceImpl(String instanceId, String parentId, String rootId,
                                   WorkFlowDefinition definition) {
    super(instanceId, parentId, rootId, definition);
  }

  public ChildWorkFlowInstanceImpl(Long id, String instanceId, String parentId, String rootId,
                                   WorkFlowDefinition definition,
                                   InstanceState state) {
    super(instanceId, parentId, rootId, definition, state);
    this.id = id;
  }

  public Long getId() {
    return id;
  }


}
