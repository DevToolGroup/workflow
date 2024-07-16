/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.engine.runtime;

import java.lang.reflect.InvocationTargetException;

import group.devtool.workflow.engine.WorkFlowContextImpl;
import group.devtool.workflow.engine.WorkFlowVariable;
import group.devtool.workflow.engine.common.TimeUnit;
import group.devtool.workflow.engine.exception.InitTaskDelegateException;
import group.devtool.workflow.engine.exception.NotFoundWorkFlowTaskDelegate;

/**
 * 延时流程任务
 */
public abstract class DelayWorkFlowTask extends AbstractWorkFlowTask {

	protected DelayWorkFlowTaskConfig config;

	public DelayWorkFlowTask(String taskId, String nodeId, String nodeCode, DelayWorkFlowTaskConfig config, String instanceId) {
		super(taskId, nodeId, nodeCode, instanceId);
		this.config = config;
	}

	public DelayWorkFlowTask(String taskId, String nodeId, String nodeCode, DelayWorkFlowTaskConfig config, String instanceId,
													 WorkFlowTaskState state) {
		super(taskId, nodeId, nodeCode, instanceId, state);
		this.config = config;
	}

	@Override
	protected void doComplete(WorkFlowContextImpl context) {
		String result = doExecute(context);
		if (!config.getIgnoreResult()) {
			context.addRuntimeVariable(WorkFlowVariable.global(config.getReturnVariable(), result));
		}
		doCustomComplete(context);
	}

	protected abstract void doCustomComplete(WorkFlowContextImpl context);

	protected abstract String doExecute(WorkFlowContextImpl context);

	/**
	 * @return 延时任务配置
	 */
	protected DelayWorkFlowTaskConfig getConfig() {
		return config;
	}

	/**
	 * Java代码实现的延时任务
	 */
	public static abstract class JavaDelayWorkFlowTask extends DelayWorkFlowTask {

		public JavaDelayWorkFlowTask(String taskId, String nodeId, String nodeCode, DelayWorkFlowTaskConfig config, String instanceId) {
			super(taskId, nodeId, nodeCode, config, instanceId);
		}

		public JavaDelayWorkFlowTask(String taskId, String nodeId, String nodeCode, DelayWorkFlowTaskConfig config, String instanceId,
																 WorkFlowTaskState state) {
			super(taskId, nodeId, nodeCode, config, instanceId, state);
		}

		@Override
		protected String doExecute(WorkFlowContextImpl context) {
			DelayWorkFlowTaskConfig delayConfig = getConfig();
			JavaDelayWorkFlowTaskConfig task = (JavaDelayWorkFlowTaskConfig) delayConfig;

			Class<?> clazz = loadJava(task.getClassName());
			WorkFlowTaskJavaDelegate delegate = instance(clazz);
			return delegate.apply(context);
		}

		private WorkFlowTaskJavaDelegate instance(Class<?> clazz) {
			WorkFlowTaskJavaDelegate task;
			try {
				task = (WorkFlowTaskJavaDelegate) clazz.getDeclaredConstructor().newInstance();
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
							 | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				throw new InitTaskDelegateException("节点任务配置的Java Class必须提供无参构造器，类名：" + clazz.getName());
			}
			return task;
		}

		private Class<?> loadJava(String className) {
			Class<?> clazz = null;
			try {
				clazz = Class.forName(className);
			} catch (ClassNotFoundException e) {
				throw new NotFoundWorkFlowTaskDelegate("节点任务配置的Java Class Name不存在，类名：" + className);
			}
			if (!WorkFlowTaskJavaDelegate.class.isAssignableFrom(clazz)) {
				throw new NotFoundWorkFlowTaskDelegate("节点任务配置的Java Class必须是JavaTaskDelegate类的子类，类名：" + className);
			}
			return clazz;
		}

	}

	public static abstract class DelayWorkFlowTaskConfig implements WorkFlowTaskConfig {

		private Boolean ignoreResult;

		private String returnVariable;

		private Long time;

		private TimeUnit unit;

		public DelayWorkFlowTaskConfig() {

		}

		public DelayWorkFlowTaskConfig(Boolean ignoreResult, String returnVariable, Long time, TimeUnit unit) {
			this.ignoreResult = ignoreResult;
			this.returnVariable = returnVariable;
			this.time = time;
			this.unit = unit;
		}

		public Boolean getIgnoreResult() {
			return ignoreResult;
		}

		public void setIgnoreResult(Boolean ignoreResult) {
			this.ignoreResult = ignoreResult;
		}

		public String getReturnVariable() {
			return returnVariable;
		}

		public void setReturnVariable(String returnVariable) {
			this.returnVariable = returnVariable;
		}

		public Long getTime() {
			return time;
		}

		public void setTime(Long time) {
			this.time = time;
		}

		public TimeUnit getUnit() {
			return unit;
		}

		public void setUnit(TimeUnit unit) {
			this.unit = unit;
		}
	}

	public static abstract class JavaDelayWorkFlowTaskConfig extends DelayWorkFlowTaskConfig {

		private String className;

		public JavaDelayWorkFlowTaskConfig(Boolean ignoreResult, String returnVariable, Long time, TimeUnit unit, String className) {
			super(ignoreResult, returnVariable, time, unit);
			this.className = className;
		}

		public String getClassName() {
			return className;
		}

		public void setClassName(String className) {
			this.className = className;
		}
	}

}
