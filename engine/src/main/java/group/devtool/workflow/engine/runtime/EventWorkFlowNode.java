/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.engine.runtime;


import group.devtool.workflow.engine.WorkFlowContextImpl;
import group.devtool.workflow.engine.definition.EventWorkFlowNodeDefinition;
import group.devtool.workflow.engine.definition.EventWorkFlowNodeDefinition.EventWorkFlowConfig;
import group.devtool.workflow.engine.definition.WorkFlowNodeDefinition;

/**
 * 用户节点
 */
public abstract class EventWorkFlowNode extends AbstractWorkFlowNode {

	public EventWorkFlowNode(String nodeId, WorkFlowNodeDefinition definition, WorkFlowContextImpl context) {
		super(nodeId, definition, context);
	}

	public EventWorkFlowNode(String nodeId, String nodeCode, Integer version, EventWorkFlowConfig config, WorkFlowTask... tasks) {
		super(nodeId, nodeCode, version, config, tasks);
	}

	@Override
	public boolean done() {
		for (WorkFlowTask task : getTasks()) {
			if (task.completed()) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected WorkFlowTask[] initTask(WorkFlowNodeDefinition definition, WorkFlowContextImpl context) {
		EventWorkFlowNodeDefinition eventDefinition = (EventWorkFlowNodeDefinition) definition;
		return doInitTask(eventDefinition, context);
	}

	protected abstract EventWorkFlowTask[] doInitTask(EventWorkFlowNodeDefinition definition, WorkFlowContextImpl context);

}
