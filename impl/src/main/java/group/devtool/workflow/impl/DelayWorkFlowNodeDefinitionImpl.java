package group.devtool.workflow.impl;

import group.devtool.workflow.core.DelayWorkFlowNodeDefinition;
import group.devtool.workflow.core.exception.WorkFlowDefinitionException;
import group.devtool.workflow.core.exception.IllegalDefinitionParameter;
import group.devtool.workflow.core.exception.WorkFlowException;

/**
 * {@link DelayWorkFlowNodeDefinition} 默认实现
 */
public class DelayWorkFlowNodeDefinitionImpl extends DelayWorkFlowNodeDefinition {

  private final String code;

  private final String name;

  private final DelayTaskConfig config;

  public DelayWorkFlowNodeDefinitionImpl(String code, String name, DelayTaskConfig config) throws WorkFlowException {
    if (null == code || null == name || null == config) {
      throw new IllegalDefinitionParameter("延时任务务节点编码、名称、配置不能为空");
    }
    this.code = code;
    this.name = name;
    this.config = config;
    if (!support(config.getTask())) {
      throw new WorkFlowDefinitionException(
          "节点定义配置类型不匹配，预期类型：JavaScheduleTask、GroovyScheduleTask、ExpressionScheduleTask");
    }
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
  public DelayTaskConfig getConfig() {
    return config;
  }

  /**
   * 暂时仅支持Java
   *
   * @param task
   * @return
   */
  private static boolean support(WorkFlowDelayTask task) {
    return task instanceof JavaDelayTask || task instanceof GroovyDelayTask || task instanceof ExpressionDelayTask;
  }

  public class WorkFlowDelayConfigImpl implements DelayTaskConfig {

    private final Integer time;

    private final DelayUnit unit;

    private final boolean ignoreResult;

    private final String returnVariable;

    private final WorkFlowDelayTask task;

    public WorkFlowDelayConfigImpl(Integer time, DelayUnit unit, boolean ignoreResult,
                                   String returnVariable, WorkFlowDelayTask task) {
      this.time = time;
      this.unit = unit;
      this.ignoreResult = ignoreResult;
      this.returnVariable = returnVariable;
      this.task = task;
    }

    @Override
    public WorkFlowDelayTask getTask() {
      return task;
    }

    @Override
    public Integer getTime() {
      return time;
    }

    @Override
    public DelayUnit getUnit() {
      return unit;
    }

    @Override
    public boolean ignoreResult() {
      return ignoreResult;
    }


    @Override
    public String getReturnVariable() {
      return returnVariable;
    }
  }

  public class JavaDelayTaskImpl implements JavaDelayTask {

    private final String returnVariable;

    private final String className;

    public JavaDelayTaskImpl(String returnVariable, String className) {
      this.returnVariable = returnVariable;
      this.className = className;
    }

    @Override
    public TaskWorker getWorker() {
      return TaskWorker.JAVA;
    }

    @Override
    public String getReturnVariable() {
      return returnVariable;
    }

    @Override
    public String getClassName() {
      return className;
    }
  }

  @Override
  public String getType() {
    return "DELAY";
  }

}
