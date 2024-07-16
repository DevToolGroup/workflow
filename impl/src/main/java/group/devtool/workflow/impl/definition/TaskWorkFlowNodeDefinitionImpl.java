/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.impl.definition;

import group.devtool.workflow.engine.common.TaskWorker;
import group.devtool.workflow.engine.definition.TaskWorkFlowNodeDefinition;
import group.devtool.workflow.engine.exception.IllegalWorkFlowDefinition;

/**
 * {@link TaskWorkFlowNodeDefinition} 默认实现
 */
public class TaskWorkFlowNodeDefinitionImpl extends TaskWorkFlowNodeDefinition {

	private final String code;

	private final String name;

	private final TaskWorkFlowNodeConfig config;

	public TaskWorkFlowNodeDefinitionImpl(String code, String name, TaskWorkFlowNodeConfig config) throws IllegalWorkFlowDefinition {
		if (null == code || null == name || null == config) {
			throw new IllegalWorkFlowDefinition("任务任务节点编码、名称、配置不能为空");
		}
		this.code = code;
		this.name = name;
		this.config = config;
	}

	@Override
	public String getCode() {
		return code;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public TaskWorkFlowNodeConfig getConfig() {
		return config;
	}

	public static class JavaTaskWorkFlowConfigImpl implements JavaTaskWorkFlowConfig {

		private boolean ignoreResult;

		private String returnVariable;

		private String className;

		public JavaTaskWorkFlowConfigImpl() {
		}

		public JavaTaskWorkFlowConfigImpl(String className, boolean ignoreResult, String returnVariable) {
			this.ignoreResult = ignoreResult;
			this.returnVariable = returnVariable;
			this.className = className;
		}

		@Override
		public TaskWorker getWorker() {
			return TaskWorker.JAVA;
		}

		@Override
		public Boolean getIgnoreResult() {
			return ignoreResult;
		}

		@Override
		public String getReturnVariable() {
			return returnVariable;
		}

		@Override
		public String getClassName() {
			return className;
		}

		public void setIgnoreResult(boolean ignoreResult) {
			this.ignoreResult = ignoreResult;
		}

		public void setReturnVariable(String returnVariable) {
			this.returnVariable = returnVariable;
		}

		public void setClassName(String className) {
			this.className = className;
		}
	}

	@Override
	public String getType() {
		return "TASK";
	}

}
