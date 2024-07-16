/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.impl.runtime;

import group.devtool.workflow.engine.WorkFlowContext;
import group.devtool.workflow.engine.runtime.StartWorkFlowTask;
import group.devtool.workflow.engine.WorkFlowContextImpl;
import group.devtool.workflow.engine.exception.NotFoundWorkFlowVariable;
import group.devtool.workflow.impl.WorkFlowConfigurationImpl;

/**
 * {@link StartWorkFlowTask} 默认实现
 */
public class StartWorkFlowTaskImpl extends StartWorkFlowTask {

	private String account;

	private final String rootInstanceId;

	private Long completeTime;

	public StartWorkFlowTaskImpl(String nodeId, String nodeCode, String instanceId, String rootInstanceId) {
		super(WorkFlowConfigurationImpl.CONFIG.idSupplier().getTaskId(), nodeId, nodeCode, instanceId);
		this.rootInstanceId = rootInstanceId;
	}

	public StartWorkFlowTaskImpl(String taskId, String nodeId, String nodeCode,
															 String instanceId, String rootInstanceId,
															 WorkFlowTaskState taskState) {
		super(taskId, nodeId, nodeCode, instanceId, taskState);
		this.rootInstanceId = rootInstanceId;
	}

	@Override
	protected void doComplete(WorkFlowContextImpl context) {
		account = (String) context.lookup(WorkFlowContext.USER);
		if (null == account) {
			throw new NotFoundWorkFlowVariable("开始节点缺少操作用户参数");
		}
		completeTime = System.currentTimeMillis();
	}

	public String getRootInstanceId() {
		return rootInstanceId;
	}

	public String getTaskClass() {
		return "START";
	}

	public String getCompleteUser() {
		return account;
	}

	public Long getCompleteTime() {
		return completeTime;
	}

	public String getTaskConfig() {
		return null;
	}

}
