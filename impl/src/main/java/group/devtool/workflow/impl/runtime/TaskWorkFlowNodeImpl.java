/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.impl.runtime;

import group.devtool.workflow.engine.common.TaskWorker;
import group.devtool.workflow.engine.definition.TaskWorkFlowNodeDefinition;
import group.devtool.workflow.engine.WorkFlowContextImpl;
import group.devtool.workflow.engine.definition.WorkFlowNodeDefinition;
import group.devtool.workflow.engine.definition.TaskWorkFlowNodeDefinition.JavaTaskWorkFlowConfig;
import group.devtool.workflow.engine.definition.TaskWorkFlowNodeDefinition.TaskWorkFlowNodeConfig;
import group.devtool.workflow.engine.exception.NotSupportWorkFlowTaskClass;
import group.devtool.workflow.engine.definition.WorkFlowNodeDefinition.WorkFlowNodeConfig;
import group.devtool.workflow.impl.runtime.JavaWorkFlowTaskImpl.JavaTaskWorkFlowTaskConfigImpl;
import group.devtool.workflow.engine.runtime.TaskWorkFlowTask;
import group.devtool.workflow.engine.runtime.TaskWorkFlowNode;
import group.devtool.workflow.engine.runtime.WorkFlowTask;

/**
 * 任务节点
 */
public class TaskWorkFlowNodeImpl extends TaskWorkFlowNode {

	private final String instanceId;

	private final String rootInstanceId;

	public TaskWorkFlowNodeImpl(String nodeId, WorkFlowNodeDefinition definition, String instanceId, String rootInstanceId,
															WorkFlowContextImpl context) {
		super(nodeId, definition, context);
		this.instanceId = instanceId;
		this.rootInstanceId = rootInstanceId;
	}

	public TaskWorkFlowNodeImpl(String nodeId, String nodeCode, Integer version, WorkFlowNodeConfig config, String instanceId, String rootInstanceId, WorkFlowTask... tasks) {
		super(nodeId, nodeCode, version, config, tasks);
		this.instanceId = instanceId;
		this.rootInstanceId = rootInstanceId;
	}

	@Override
	public String getInstanceId() {
		return instanceId;
	}

	public String getRootInstanceId() {
		return rootInstanceId;
	}

	@Override
	protected TaskWorkFlowTask doInitTask(TaskWorkFlowNodeDefinition definition, WorkFlowContextImpl context) {
		TaskWorkFlowNodeConfig config = (TaskWorkFlowNodeConfig)getConfig();
		if (TaskWorker.JAVA == config.getWorker()) {
			JavaTaskWorkFlowConfig jc = (JavaTaskWorkFlowConfig) config;
			return new JavaWorkFlowTaskImpl(getNodeId(), getNodeCode(), new JavaTaskWorkFlowTaskConfigImpl(jc), instanceId, rootInstanceId);
		}
		throw new NotSupportWorkFlowTaskClass("任务节点执行器类型暂不支持，执行器类型：" + config.getWorker().name());
	}

	public String getNodeClass() {
		return "TASK";
	}

}
