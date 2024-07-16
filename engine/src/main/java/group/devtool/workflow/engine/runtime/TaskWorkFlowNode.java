/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.engine.runtime;

import group.devtool.workflow.engine.WorkFlowContextImpl;
import group.devtool.workflow.engine.definition.TaskWorkFlowNodeDefinition;
import group.devtool.workflow.engine.definition.WorkFlowNodeDefinition;
import group.devtool.workflow.engine.definition.WorkFlowNodeDefinition.WorkFlowNodeConfig;

import java.util.Arrays;

/**
 * 任务节点
 * 在初始化后直接执行任务
 */
public abstract class TaskWorkFlowNode extends AbstractWorkFlowNode {

	public TaskWorkFlowNode(String nodeId, WorkFlowNodeDefinition definition, WorkFlowContextImpl context) {
		super(nodeId, definition, context);
	}

	public TaskWorkFlowNode(String nodeId, String nodeCode, Integer version, WorkFlowNodeConfig config, WorkFlowTask... tasks) {
		super(nodeId, nodeCode, version, config, tasks);
	}

	@Override
	public boolean done() {
		return Arrays.stream(getTasks()).allMatch(WorkFlowTask::completed);
	}

	@Override
	protected WorkFlowTask[] initTask(WorkFlowNodeDefinition definition, WorkFlowContextImpl context) {
		TaskWorkFlowNodeDefinition taskDefinition = (TaskWorkFlowNodeDefinition) definition;
		TaskWorkFlowTask task = doInitTask(taskDefinition, context);
		task.complete(context);
		return new WorkFlowTask[]{task};
	}

	/**
	 * 根据定义初始化任务实例
	 *
	 * @param definition 任务配置
	 * @param context
	 * @return 任务实例
	 * @
	 */
	protected abstract TaskWorkFlowTask doInitTask(TaskWorkFlowNodeDefinition definition, WorkFlowContextImpl context);

}
