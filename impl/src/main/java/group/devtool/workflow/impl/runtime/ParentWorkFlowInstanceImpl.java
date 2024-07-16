/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.impl.runtime;

import group.devtool.workflow.engine.common.InstanceState;
import group.devtool.workflow.engine.runtime.ParentWorkFlowInstance;
import group.devtool.workflow.engine.definition.WorkFlowDefinition;

/**
 * {@link ParentWorkFlowInstance} 默认实现
 */
public class ParentWorkFlowInstanceImpl extends ParentWorkFlowInstance {

	private Long id;

	public ParentWorkFlowInstanceImpl(String instanceId, WorkFlowDefinition definition) {
		super(instanceId, definition);
	}

	public ParentWorkFlowInstanceImpl(Long id, String instanceId, WorkFlowDefinition definition,
																		InstanceState state) {
		super(instanceId, definition, state);
		this.id = id;
	}

	public Long getId() {
		return id;
	}

}