/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.engine.runtime;

import java.util.*;

import group.devtool.workflow.engine.*;
import group.devtool.workflow.engine.definition.ChildWorkFlowNodeDefinition.ChildStartUp;
import group.devtool.workflow.engine.definition.ChildWorkFlowNodeDefinition.ChildWorkFlowConfig;
import group.devtool.workflow.engine.definition.WorkFlowNodeDefinition;
import group.devtool.workflow.engine.definition.WorkFlowNodeDefinition.WorkFlowNodeConfig;

/**
 * 嵌套子流程抽象类，子类通过实现{@code doInitTask}完成任务的初始化
 */
public abstract class ChildWorkFlowNode extends AbstractWorkFlowNode implements WorkFlowNode {

	// not null only init from definition
	private Map<String, String> childNodes = new HashMap<>();

	public ChildWorkFlowNode(String nodeId, WorkFlowNodeDefinition definition, WorkFlowContextImpl context) {
		super(nodeId, definition, context);
	}

	public ChildWorkFlowNode(String nodeId, String nodeCode, Integer version, WorkFlowNodeConfig config, WorkFlowTask... tasks) {
		super(nodeId, nodeCode, version, config, tasks);
	}

	public List<WorkFlowInstance> instances(ChildFactory childFactory) {
		List<WorkFlowInstance> instances = new ArrayList<>();
		for (WorkFlowTask task : getTasks()) {
			String taskId = task.getTaskId();
			instances.add(childFactory.apply(childNodes.get(taskId), taskId));
		}
		return instances;
	}

	@Override
	public boolean done() {
		return Arrays.stream(getTasks()).allMatch(WorkFlowTask::completed);
	}

	@Override
	protected WorkFlowTask[] initTask(WorkFlowNodeDefinition definition, WorkFlowContextImpl context) {
		ChildWorkFlowConfig config = (ChildWorkFlowConfig) getConfig();

		List<WorkFlowTask> result = new ArrayList<>();

		for (ChildStartUp startUp: config.getStartUp()) {
			if (null == startUp.getExpression() || match(startUp.getExpression(), context)) {
				ChildWorkFlowTask[] tasks = doInitTask(startUp.getTaskNumber(), context);
				for (ChildWorkFlowTask task : tasks) {
					result.add(task);
					childNodes.put(task.getTaskId(), startUp.getChildCode());
				}
			}
		}
		return result.toArray(new WorkFlowTask[0]);
	}

	protected abstract boolean match(String expression, WorkFlowContextImpl context);

	protected abstract ChildWorkFlowTask[] doInitTask(int taskNumber, WorkFlowContextImpl context);

	/**
	 * 嵌套子流程实例工厂接口
	 */
	public static interface ChildFactory {

		/**
		 * 嵌套子流程实例化
		 *
		 * @param definitionCode 嵌套子流程定义
		 * @param taskId 子流程节点任务ID
		 * @return 子流程实例
		 * @ 流程实例化异常
		 */
		WorkFlowInstance apply(String definitionCode, String taskId) ;

	}
}
