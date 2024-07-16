/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.engine.runtime;

import group.devtool.workflow.engine.WorkFlowContextImpl;
import group.devtool.workflow.engine.definition.WorkFlowNodeDefinition;
import group.devtool.workflow.engine.definition.WorkFlowNodeDefinition.WorkFlowNodeConfig;

import java.util.Arrays;

/**
 * 流程启动节点
 */
public abstract class StartWorkFlowNode extends AbstractWorkFlowNode {

	public StartWorkFlowNode(String nodeId, WorkFlowNodeDefinition definition, WorkFlowContextImpl context) {
		super(nodeId, definition, context);
	}

	public StartWorkFlowNode(String nodeId, String nodeCode, Integer version, WorkFlowNodeConfig config, WorkFlowTask... tasks) {
		super(nodeId, nodeCode, version, config, tasks);
	}

	@Override
	public boolean done() {
		return Arrays.stream(getTasks()).allMatch(WorkFlowTask::completed);
	}

	@Override
	public void beforeComplete() {

	}

	@Override
	protected WorkFlowTask[] initTask(WorkFlowNodeDefinition definition, WorkFlowContextImpl context) {
		StartWorkFlowTask[] tasks = doInitTask(definition);
		// 任务状态直接设置为已完成
		for (StartWorkFlowTask task : tasks) {
			task.complete(context);
		}
		return tasks;
	}

	protected abstract StartWorkFlowTask[] doInitTask(WorkFlowNodeDefinition definition);

}
