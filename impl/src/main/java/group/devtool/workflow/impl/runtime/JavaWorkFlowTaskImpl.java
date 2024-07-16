/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.impl.runtime;

import java.io.Serializable;

import group.devtool.workflow.engine.WorkFlowContextImpl;
import group.devtool.workflow.engine.definition.TaskWorkFlowNodeDefinition.JavaTaskWorkFlowConfig;
import group.devtool.workflow.engine.runtime.TaskWorkFlowTask.JavaWorkFlowTask;
import group.devtool.workflow.engine.exception.SerializeException;
import group.devtool.workflow.impl.WorkFlowConfigurationImpl;

/**
 * {@link JavaWorkFlowTask} 默认实现
 */
public class JavaWorkFlowTaskImpl extends JavaWorkFlowTask {

	private final String rootInstanceId;

	private Long completeTime;

	private final String byteConfig;

	public JavaWorkFlowTaskImpl(String nodeId, String nodeCode, JavaTaskWorkFlowTaskConfig config, String instanceId, String rootInstanceId) {
		super(WorkFlowConfigurationImpl.CONFIG.idSupplier().getTaskId(), nodeId, nodeCode, config, instanceId);
		this.rootInstanceId = rootInstanceId;
		this.byteConfig = getConfig(config);
	}

	public JavaWorkFlowTaskImpl(String taskId, String nodeId, String nodeCode, JavaTaskWorkFlowTaskConfig config,
															String instanceId, String rootInstanceId, WorkFlowTaskState state) throws SerializeException {
		super(taskId, nodeId, nodeCode, config, instanceId, state);
		this.rootInstanceId = rootInstanceId;
		this.byteConfig = getConfig(config);
	}

	public String getRootInstanceId() {
		return rootInstanceId;
	}

	@Override
	public void doCustomComplete(WorkFlowContextImpl context, Serializable result) {
		// do nothing
		completeTime = System.currentTimeMillis();
	}

	public String getTaskClass() {
		return "JAVA";
	}

	public String getCompleteUser() {
		return null;
	}

	public Long getCompleteTime() {
		return completeTime;
	}

	public String getTaskConfig() {
		return byteConfig;
	}

	public static class JavaTaskWorkFlowTaskConfigImpl extends JavaTaskWorkFlowTaskConfig {

		public JavaTaskWorkFlowTaskConfigImpl() {

		}

		public JavaTaskWorkFlowTaskConfigImpl(JavaTaskWorkFlowConfig jc) {
			super(jc.getIgnoreResult(), jc.getReturnVariable(), jc.getClassName());
		}

		public JavaTaskWorkFlowTaskConfigImpl(String className, Boolean ignoreResult, String returnVariable) {
			super(ignoreResult, returnVariable, className);
		}
	}

}
