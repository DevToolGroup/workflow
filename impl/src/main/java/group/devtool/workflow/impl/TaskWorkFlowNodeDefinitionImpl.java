package group.devtool.workflow.impl;

import group.devtool.workflow.core.TaskWorkFlowNodeDefinition;
import group.devtool.workflow.core.exception.IllegalDefinitionParameter;

/**
 * {@link TaskWorkFlowNodeDefinition} 默认实现
 */
public class TaskWorkFlowNodeDefinitionImpl extends TaskWorkFlowNodeDefinition {

  private final String code;

  private final String name;

  private final WorkFlowTaskConfig config;

  public TaskWorkFlowNodeDefinitionImpl(String code, String name, WorkFlowTaskConfig config) throws IllegalDefinitionParameter {
    if (null == code || null == name || null == config) {
      throw new IllegalDefinitionParameter("任务任务节点编码、名称、配置不能为空");
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
  public WorkFlowTaskConfig getConfig() {
    return config;
  }

  public static class JavaTaskConfigImpl implements JavaTaskConfig {

    private final boolean ignoreResult;

    private final String returnVariable;

    private final String className;

    public JavaTaskConfigImpl(String className, boolean ignoreResult, String returnVariable) {
      this.ignoreResult = ignoreResult;
      this.returnVariable = returnVariable;
      this.className = className;
    }

    @Override
    public TaskWorker getWorker() {
      return TaskWorker.JAVA;
    }

    @Override
    public boolean ignoreResult() {
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
  }

  @Override
  public String getType() {
    return "TASK";
  }

}
