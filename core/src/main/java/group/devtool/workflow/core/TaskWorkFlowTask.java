package group.devtool.workflow.core;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

import group.devtool.workflow.core.TaskWorkFlowNodeDefinition.JavaTaskConfig;
import group.devtool.workflow.core.TaskWorkFlowNodeDefinition.WorkFlowTaskConfig;
import group.devtool.workflow.core.exception.WorkFlowException;
import group.devtool.workflow.core.exception.InitTaskDelegateException;
import group.devtool.workflow.core.exception.NotFoundWorkFlowTaskDelegate;

/**
 * 抽象任务类，完成Java类的加载以及任务的执行
 */
public abstract class TaskWorkFlowTask extends AbstractWorkFlowTask {

  private final WorkFlowTaskConfig config;

  public TaskWorkFlowTask(String taskId, String node, WorkFlowTaskConfig config, String instanceId) {
    super(taskId, node, instanceId);
    this.config = config;
  }

  public TaskWorkFlowTask(String id, String node, WorkFlowTaskConfig config, String instanceId,
      WorkFlowTaskState state) {
    super(id, node, instanceId, state);
    this.config = config;
  }

  @Override
  protected void doComplete(WorkFlowContext context) throws WorkFlowException {
    Serializable result = doExecute(context);
    if (!config.ignoreResult()) {
      context.add(WorkFlowVariable.bound(config.getReturnVariable(), result, getTaskId(), getNodeCode()));
    }
    doCustomComplete(context, result);
  }

  protected WorkFlowTaskConfig getConfig() {
    return config;
  }

  protected abstract void doCustomComplete(WorkFlowContext context, Serializable result) throws WorkFlowException;

  protected abstract Serializable doExecute(WorkFlowContext context) throws WorkFlowException;

  /**
   * 抽象Java流程任务
   */
  public static abstract class JavaWorkFlowTask extends TaskWorkFlowTask {

    public JavaWorkFlowTask(String taskId, String node, WorkFlowTaskConfig config, String instanceId) {
      super(taskId, node, config, instanceId);
    }

    public JavaWorkFlowTask(String taskId, String node, WorkFlowTaskConfig config, String instanceId,
        WorkFlowTaskState state) {
      super(taskId, node, config, instanceId, state);
    }

    @Override
    protected Serializable doExecute(WorkFlowContext context) throws WorkFlowException {
      Class<?> clazz = loadJava();
      WorkFlowTaskJavaDelegate task = instance(clazz);
      return task.apply(context);
    }

    private WorkFlowTaskJavaDelegate instance(Class<?> clazz) throws WorkFlowException {
      JavaTaskConfig javaConfig = (JavaTaskConfig) getConfig();
      WorkFlowTaskJavaDelegate task;
      try {
        task = (WorkFlowTaskJavaDelegate) clazz.getDeclaredConstructor().newInstance();
      } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
          | InvocationTargetException | NoSuchMethodException | SecurityException e) {
        throw new InitTaskDelegateException("节点任务配置的Java Class必须提供无参构造器，类名：" + javaConfig.getClassName());
      }
      return task;
    }

    private Class<?> loadJava() throws NotFoundWorkFlowTaskDelegate {
      JavaTaskConfig javaConfig = (JavaTaskConfig) getConfig();
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
}
