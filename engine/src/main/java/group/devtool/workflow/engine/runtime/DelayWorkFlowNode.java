/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.engine.runtime;

import java.util.Arrays;

import group.devtool.workflow.engine.WorkFlowContextImpl;
import group.devtool.workflow.engine.WorkFlowDelayTaskScheduler;
import group.devtool.workflow.engine.WorkFlowDelayTaskScheduler.DelayItem;
import group.devtool.workflow.engine.definition.DelayWorkFlowNodeDefinition;
import group.devtool.workflow.engine.definition.WorkFlowNodeDefinition;
import group.devtool.workflow.engine.definition.WorkFlowNodeDefinition.WorkFlowNodeConfig;

/**
 * 延时节点
 */
public abstract class DelayWorkFlowNode extends AbstractWorkFlowNode {

	public DelayWorkFlowNode(String nodeId, WorkFlowNodeDefinition definition, WorkFlowContextImpl context) {
		super(nodeId, definition, context);
	}

	public DelayWorkFlowNode(String nodeId, String nodeCode, Integer version, WorkFlowNodeConfig config, WorkFlowTask... tasks) {
		super(nodeId, nodeCode, version, config, tasks);
	}

	@Override
	public boolean done() {
		return Arrays.stream(getTasks()).allMatch(WorkFlowTask::completed);
	}

	@Override
	protected WorkFlowTask[] initTask(WorkFlowNodeDefinition definition, WorkFlowContextImpl context) {
		DelayWorkFlowNodeDefinition delayDefinition = (DelayWorkFlowNodeDefinition) definition;
		DelayWorkFlowTask task = doInitTask(delayDefinition, context);
		getScheduler().addTask(doInitDelayItem(task));
		return new WorkFlowTask[]{task};
	}

	/**
	 * @return 流程调度器
	 */
	protected abstract WorkFlowDelayTaskScheduler getScheduler();

	/**
	 * 初始化延时任务
	 *
	 * @param task 流程任务实例
	 * @return 延时任务实例
	 */
	protected abstract DelayItem doInitDelayItem(DelayWorkFlowTask task);

	/**
	 * 根据定义初始化任务实例
	 *
	 * @param definition 任务配置
	 * @param context    流程上下文
	 * @return 任务实例
	 * @ 流程异常
	 */
	protected abstract DelayWorkFlowTask doInitTask(DelayWorkFlowNodeDefinition definition, WorkFlowContextImpl context);

}
