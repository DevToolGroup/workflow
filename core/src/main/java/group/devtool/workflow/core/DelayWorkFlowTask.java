package group.devtool.workflow.core;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

import group.devtool.workflow.core.DelayWorkFlowNodeDefinition.DelayTaskConfig;
import group.devtool.workflow.core.DelayWorkFlowNodeDefinition.JavaDelayTask;
import group.devtool.workflow.core.exception.WorkFlowException;
import group.devtool.workflow.core.exception.InitTaskDelegateException;
import group.devtool.workflow.core.exception.NotFoundWorkFlowTaskDelegate;

/**
 * 延时流程任务
 */
public abstract class DelayWorkFlowTask extends AbstractWorkFlowTask {

  protected DelayTaskConfig config;

  public DelayWorkFlowTask(String taskId, String node, DelayTaskConfig config, String instanceId) {
    super(taskId, node, instanceId);
    this.config = config;
  }

  public DelayWorkFlowTask(String taskId, String node, DelayTaskConfig config, String instanceId,
      WorkFlowTaskState state) {
    super(taskId, node, instanceId, state);
    this.config = config;
  }

  @Override
  protected void doComplete(WorkFlowContext context) throws WorkFlowException {
    Serializable result = doExecute(context);
    if (!config.ignoreResult()) {
      context.add(WorkFlowVariable.bound(config.getReturnVariable(), result, getTaskId(), getNodeCode()));
    }
    doCustomComplete(context);
  }

  protected abstract void doCustomComplete(WorkFlowContext context);

  protected abstract Serializable doExecute(WorkFlowContext context) throws WorkFlowException;

  /**
   * @return 延时任务配置
   */
  protected DelayTaskConfig getConfig() {
    return config;
  }

  /**
   * Java代码实现的延时任务
   */
  public static abstract class DelayJavaWorkFlowTask extends DelayWorkFlowTask {

    public DelayJavaWorkFlowTask(String taskId, String node, DelayTaskConfig config, String instanceId) {
      super(taskId, node, config, instanceId);
    }

    public DelayJavaWorkFlowTask(String taskId, String node, DelayTaskConfig config, String instanceId,
        WorkFlowTaskState state) {
      super(taskId, node, config, instanceId, state);
    }

    @Override
    protected Serializable doExecute(WorkFlowContext context) throws WorkFlowException {
      DelayTaskConfig delayConfig = getConfig();
      JavaDelayTask task = (JavaDelayTask) delayConfig.getTask();

      Class<?> clazz = loadJava(task.getClassName());
      WorkFlowTaskJavaDelegate delegate = instance(clazz);
      return delegate.apply(context);
    }

    private WorkFlowTaskJavaDelegate instance(Class<?> clazz) throws WorkFlowException {
      WorkFlowTaskJavaDelegate task;
      try {
        task = (WorkFlowTaskJavaDelegate) clazz.getDeclaredConstructor().newInstance();
      } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
          | InvocationTargetException | NoSuchMethodException | SecurityException e) {
        throw new InitTaskDelegateException("节点任务配置的Java Class必须提供无参构造器，类名：" + clazz.getName());
      }
      return task;
    }

    private Class<?> loadJava(String className) throws WorkFlowException {
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
}
