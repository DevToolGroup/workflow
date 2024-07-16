/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.engine.runtime;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

import group.devtool.workflow.engine.WorkFlowContextImpl;
import group.devtool.workflow.engine.WorkFlowVariable;
import group.devtool.workflow.engine.common.TaskWorker;
import group.devtool.workflow.engine.exception.InitTaskDelegateException;
import group.devtool.workflow.engine.exception.NotFoundWorkFlowTaskDelegate;

/**
 * 抽象任务类，完成Java类的加载以及任务的执行
 */
public abstract class TaskWorkFlowTask extends AbstractWorkFlowTask {

  private final TaskWorkFlowTaskConfig config;

  public TaskWorkFlowTask(String taskId, String nodeId, String nodeCode, TaskWorkFlowTaskConfig config, String instanceId) {
    super(taskId, nodeId, nodeCode, instanceId);
    this.config = config;
  }

  public TaskWorkFlowTask(String id, String nodeId, String nodeCode, TaskWorkFlowTaskConfig config, String instanceId,
      WorkFlowTaskState state) {
    super(id, nodeId, nodeCode, instanceId, state);
    this.config = config;
  }

  @Override
  protected void doComplete(WorkFlowContextImpl context)  {
    String result = doExecute(context);
    if (!config.getIgnoreResult()) {
      context.addRuntimeVariable(WorkFlowVariable.global(config.getReturnVariable(), result));
    }
    doCustomComplete(context, result);
  }

  protected TaskWorkFlowTaskConfig getConfig() {
    return config;
  }

  protected abstract void doCustomComplete(WorkFlowContextImpl context, Serializable result) ;

  protected abstract String doExecute(WorkFlowContextImpl context) ;

  /**
   * 抽象Java流程任务
   */
  public static abstract class JavaWorkFlowTask extends TaskWorkFlowTask {

    public JavaWorkFlowTask(String taskId, String nodeId, String nodeCode, TaskWorkFlowTaskConfig config, String instanceId) {
      super(taskId, nodeId, nodeCode, config, instanceId);
    }

    public JavaWorkFlowTask(String taskId, String nodeId, String nodeCode, TaskWorkFlowTaskConfig config, String instanceId,
        WorkFlowTaskState state) {
      super(taskId, nodeId, nodeCode, config, instanceId, state);
    }

    @Override
    protected String doExecute(WorkFlowContextImpl context)  {
      Class<?> clazz = loadJava();
      WorkFlowTaskJavaDelegate task = instance(clazz);
      return task.apply(context);
    }

    private WorkFlowTaskJavaDelegate instance(Class<?> clazz)  {
      JavaTaskWorkFlowTaskConfig javaConfig = (JavaTaskWorkFlowTaskConfig) getConfig();
      WorkFlowTaskJavaDelegate task;
      try {
        task = (WorkFlowTaskJavaDelegate) clazz.getDeclaredConstructor().newInstance();
      } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
          | InvocationTargetException | NoSuchMethodException | SecurityException e) {
        throw new InitTaskDelegateException("节点任务配置的Java Class必须提供无参构造器，类名：" + javaConfig.getClassName());
      }
      return task;
    }

    private Class<?> loadJava() {
      JavaTaskWorkFlowTaskConfig javaConfig = (JavaTaskWorkFlowTaskConfig) getConfig();
      Class<?> clazz = null;
      try {
        clazz = Class.forName(javaConfig.getClassName());
      } catch (ClassNotFoundException e) {
        throw new NotFoundWorkFlowTaskDelegate("节点任务配置的Java Class Name不存在，类名：" + javaConfig.getClassName());
      }
      if (!WorkFlowTaskJavaDelegate.class.isAssignableFrom(clazz)) {
        throw new NotFoundWorkFlowTaskDelegate("节点任务配置的Java Class必须是JavaTaskDelegate类的子类，类名：" + javaConfig.getClassName());
      }
      return clazz;
    }

  }

  public abstract static class TaskWorkFlowTaskConfig implements WorkFlowTaskConfig {

    private Boolean ignoreResult;

    private TaskWorker worker;

    private String returnVariable;

    public TaskWorkFlowTaskConfig() {

    }

    public TaskWorkFlowTaskConfig(Boolean ignoreResult, TaskWorker worker, String returnVariable) {
      this.ignoreResult = ignoreResult;
      this.worker = worker;
      this.returnVariable = returnVariable;
    }

    public Boolean getIgnoreResult() {
      return ignoreResult;
    }

    public TaskWorker getWorker() {
      return worker;
    }

    public String getReturnVariable() {
      return returnVariable;
    }

  }

  public abstract static class JavaTaskWorkFlowTaskConfig extends TaskWorkFlowTaskConfig {

    private String className;

    public JavaTaskWorkFlowTaskConfig() {
    }

    public JavaTaskWorkFlowTaskConfig(Boolean ignoreResult, String returnVariable, String className) {
      super(ignoreResult, TaskWorker.JAVA, returnVariable);
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
