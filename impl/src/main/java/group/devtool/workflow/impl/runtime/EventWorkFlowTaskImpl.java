/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.impl.runtime;

import group.devtool.workflow.engine.exception.SerializeException;
import group.devtool.workflow.engine.runtime.EventWorkFlowTask;
import group.devtool.workflow.engine.WorkFlowContextImpl;
import group.devtool.workflow.impl.WorkFlowConfigurationImpl;

/**
 * {@link EventWorkFlowTask} 默认实现
 */
public class EventWorkFlowTaskImpl extends EventWorkFlowTask {

	private final String rootInstanceId;

	private final String byteConfig;

	private long completeTime;

	public EventWorkFlowTaskImpl(String nodeId, String nodeCode, EventWorkFlowTaskConfig config, String instanceId, String rootInstanceId) {
		super(WorkFlowConfigurationImpl.CONFIG.idSupplier().getTaskId(), nodeId, nodeCode, config, instanceId);
		this.rootInstanceId = rootInstanceId;
		this.byteConfig = getConfig(config);
	}

	public EventWorkFlowTaskImpl(String taskId, String nodeId, String nodeCode, EventWorkFlowTaskConfig config, String instanceId,
															 String rootInstanceId, WorkFlowTaskState taskState) throws SerializeException {
		super(taskId, nodeId, nodeCode, config, instanceId, taskState);
		this.rootInstanceId = rootInstanceId;
		this.byteConfig = getConfig(config);
	}

	@Override
	protected void doCustomComplete(WorkFlowContextImpl context) {
		completeTime = System.currentTimeMillis();
	}

	@Override
	public String getRootInstanceId() {
		return rootInstanceId;
	}

	@Override
	public String getTaskClass() {
		return "EVENT";
	}

	@Override
	public String getCompleteUser() {
		return null;
	}

	@Override
	public Long getCompleteTime() {
		return completeTime;
	}

	public static class EventWorkFlowTaskConfigImpl extends EventWorkFlowTaskConfig {

		private String waiting;

		public EventWorkFlowTaskConfigImpl() {
		}

		public EventWorkFlowTaskConfigImpl(String waiting) {
			this.waiting = waiting;
		}

		@Override
		public String getWaiting() {
			return waiting;
		}


		public void setWaiting(String waiting) {
			this.waiting = waiting;
		}

	}

	@Override
	public String getTaskConfig() {
		return byteConfig;
	}
}
