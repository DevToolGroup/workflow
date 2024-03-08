package group.devtool.workflow.impl;

import group.devtool.workflow.core.ChildWorkFlowNodeDefinition;
import group.devtool.workflow.core.WorkFlowDefinition;
import group.devtool.workflow.core.exception.IllegalDefinitionParameter;

/**
 * {@link ChildWorkFlowNodeDefinition} 默认实现类
 */
public class ChildWorkFlowNodeDefinitionImpl extends ChildWorkFlowNodeDefinition {

  private final String code;

  private final String name;

  // 运行态，该字段值为null
  private final WorkFlowDefinition child;

  private final WorkFlowChildConfig config;

  public ChildWorkFlowNodeDefinitionImpl(String code, String name, WorkFlowChildConfig config, WorkFlowDefinition childDefinition) throws IllegalDefinitionParameter {
    if (null == code || null == name || null == config) {
      throw new IllegalDefinitionParameter("子流程任务节点编码、名称、配置、子流程定义不能为空");
    }
    this.code = code;
    this.name = name;
    this.config = config;
    this.child = childDefinition;
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
  public WorkFlowChildConfig getConfig() {
    return config;
  }

  @Override
  public WorkFlowDefinition getChild() {
    return child;
  }

  public static class MybatisWorkFlowChildConfig implements WorkFlowChildConfig {

    private final int taskNumber;

    private final String childCode;

    public MybatisWorkFlowChildConfig(int taskNumber, String childCode)
        throws IllegalDefinitionParameter {
      if (taskNumber < 1) {
        throw new IllegalDefinitionParameter("");
      }
      this.taskNumber = taskNumber;
      this.childCode = childCode;
    }

    @Override
    public int getTaskNumber() {
      return taskNumber;
    }

    @Override
    public String childCode() {
      return childCode;
    }
  }

  @Override
  public String getType() {
    return "CHILD";
  }

}
