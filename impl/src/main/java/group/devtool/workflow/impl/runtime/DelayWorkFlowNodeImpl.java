/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.impl.runtime;

import group.devtool.workflow.engine.common.TaskWorker;
import group.devtool.workflow.engine.runtime.DelayWorkFlowNode;
import group.devtool.workflow.engine.definition.DelayWorkFlowNodeDefinition;
import group.devtool.workflow.engine.runtime.DelayWorkFlowTask;
import group.devtool.workflow.engine.runtime.DelayWorkFlowTask.DelayWorkFlowTaskConfig;
import group.devtool.workflow.engine.WorkFlowContextImpl;
import group.devtool.workflow.engine.definition.WorkFlowNodeDefinition;
import group.devtool.workflow.engine.WorkFlowScheduler;
import group.devtool.workflow.engine.WorkFlowScheduler.DelayItem;
import group.devtool.workflow.engine.runtime.WorkFlowTask;
import group.devtool.workflow.engine.exception.NotSupportWorkFlowTaskClass;
import group.devtool.workflow.impl.WorkFlowConfigurationImpl;
import group.devtool.workflow.engine.definition.DelayWorkFlowNodeDefinition.DelayWorkFlowConfig;
import group.devtool.workflow.engine.definition.WorkFlowNodeDefinition.WorkFlowNodeConfig;
import group.devtool.workflow.impl.entity.WorkFlowDelayItemEntity;

/**
 * {@link DelayWorkFlowNode} 默认实现类
 */
public class DelayWorkFlowNodeImpl extends DelayWorkFlowNode {

	private final String instanceId;

	private final String rootInstanceId;

	public DelayWorkFlowNodeImpl(String nodeId, WorkFlowNodeDefinition definition, String instanceId, String rootInstanceId,
															 WorkFlowContextImpl context) {
		super(nodeId, definition, context);
		this.instanceId = instanceId;
		this.rootInstanceId = rootInstanceId;
	}

	public DelayWorkFlowNodeImpl(String nodeId, String nodeCode, Integer version, WorkFlowNodeConfig config, String instanceId, String rootInstanceId, WorkFlowTask... tasks) {
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
	protected DelayWorkFlowTask doInitTask(DelayWorkFlowNodeDefinition definition, WorkFlowContextImpl context) {
		DelayWorkFlowConfig config = (DelayWorkFlowConfig) getConfig();
		if (TaskWorker.JAVA == config.getWorker()) {
			DelayWorkFlowTaskConfig taskConfig = new JavaDelayWorkFlowTaskImpl.JavaDelayWorkFlowTaskConfigImpl(config);
			return new JavaDelayWorkFlowTaskImpl(getNodeId(), getNodeCode(), taskConfig, instanceId, rootInstanceId);
		}
		throw new NotSupportWorkFlowTaskClass("任务节点执行器类型暂不支持，执行器类型：" + config.getWorker().name());
	}

	public String getNodeClass() {
		return "DELAY";
	}

	@Override
	protected WorkFlowScheduler getScheduler() {
		return WorkFlowConfigurationImpl.CONFIG.taskScheduler();
	}

	@Override
	protected DelayItem doInitDelayItem(DelayWorkFlowTask task) {
		JavaDelayWorkFlowTaskImpl myTask = (JavaDelayWorkFlowTaskImpl) task;
		return new WorkFlowDelayItemEntity(myTask.delay(), myTask.getTaskId(), myTask.getRootInstanceId());
	}
}
