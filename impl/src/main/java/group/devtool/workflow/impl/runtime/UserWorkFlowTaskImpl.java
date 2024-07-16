/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.impl.runtime;

import group.devtool.workflow.engine.WorkFlowContext;
import group.devtool.workflow.engine.runtime.UserWorkFlowTask;
import group.devtool.workflow.engine.WorkFlowContextImpl;
import group.devtool.workflow.impl.WorkFlowConfigurationImpl;

/**
 * {@link UserWorkFlowTask} 默认实现
 */
public class UserWorkFlowTaskImpl extends UserWorkFlowTask {

	private Long completeTime;

	private final String rootInstanceId;

	private String completeUser;

	private final String byteConfig;

	public UserWorkFlowTaskImpl(String nodeId, String nodeCode, UserWorkFlowTaskConfig config,
															String instanceId, String rootInstanceId) {
		super(WorkFlowConfigurationImpl.CONFIG.idSupplier().getTaskId(), nodeId, nodeCode, config, instanceId);
		this.rootInstanceId = rootInstanceId;
		this.byteConfig = getConfig(config);
	}

	public UserWorkFlowTaskImpl(String taskId, String nodeId, String nodeCode, UserWorkFlowTaskConfig config,
															String instanceId, String rootInstanceId,
															WorkFlowTaskState taskState) {
		super(taskId, nodeId, nodeCode, config, instanceId, taskState);
		this.rootInstanceId = rootInstanceId;
		this.byteConfig = getConfig(config);
	}

	@Override
	protected void doCustomComplete(WorkFlowContextImpl context) {
		completeTime = System.currentTimeMillis();
		completeUser = (String) context.lookup(WorkFlowContext.USER);
	}

	@Override
	public String getRootInstanceId() {
		return rootInstanceId;
	}

	@Override
	public String getTaskClass() {
		return "USER";
	}

	@Override
	public String getCompleteUser() {
		return completeUser;
	}

	@Override
	public Long getCompleteTime() {
		return completeTime;
	}

	public static class UserWorkFlowTaskConfigImpl extends UserWorkFlowTaskConfig {

		private String pendingUser;

		public UserWorkFlowTaskConfigImpl() {
		}

		public UserWorkFlowTaskConfigImpl(String pendingUser) {
			this.pendingUser = pendingUser;
		}

		@Override
		public String getPendingUser() {
			return pendingUser;
		}

		public void setPendingUser(String pendingUser) {
			this.pendingUser = pendingUser;
		}
	}

	@Override
	public String getTaskConfig() {
		return byteConfig;
	}
}
