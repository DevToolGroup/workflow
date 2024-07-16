/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.engine.runtime;


import group.devtool.workflow.engine.WorkFlowContextImpl;
import group.devtool.workflow.engine.definition.UserWorkFlowNodeDefinition;
import group.devtool.workflow.engine.definition.UserWorkFlowNodeDefinition.UserWorkFlowConfig;
import group.devtool.workflow.engine.definition.WorkFlowNodeDefinition;

/**
 * 用户节点
 */
public abstract class UserWorkFlowNode extends AbstractWorkFlowNode {

	public UserWorkFlowNode(String nodeId, WorkFlowNodeDefinition definition, WorkFlowContextImpl context) {
		super(nodeId, definition, context);
	}

	public UserWorkFlowNode(String nodeId, String nodeCode, Integer version, UserWorkFlowConfig config, WorkFlowTask... tasks) {
		super(nodeId, nodeCode, version, config, tasks);
	}

	@Override
	public boolean done() {
		int confirming = 0;
		for (WorkFlowTask task : getTasks()) {
			if (task.completed()) {
				confirming += 1;
			}
		}
		return confirming >= ((UserWorkFlowConfig) getConfig()).getConfirm();
	}

	@Override
	protected WorkFlowTask[] initTask(WorkFlowNodeDefinition definition, WorkFlowContextImpl context) {
		UserWorkFlowNodeDefinition userDefinition = (UserWorkFlowNodeDefinition) definition;
		return doInitTask(userDefinition, context);
	}

	protected abstract UserWorkFlowTask[] doInitTask(UserWorkFlowNodeDefinition definition, WorkFlowContextImpl context);

}
