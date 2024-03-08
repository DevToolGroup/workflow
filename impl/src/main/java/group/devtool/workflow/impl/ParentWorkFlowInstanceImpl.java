package group.devtool.workflow.impl;

import group.devtool.workflow.core.ParentWorkFlowInstance;
import group.devtool.workflow.core.WorkFlowDefinition;

/**
 * {@link ParentWorkFlowInstance} 默认实现
 */
public class ParentWorkFlowInstanceImpl extends ParentWorkFlowInstance {

  private Long id;

  private final String definitionCode;

  private final Integer definitionVersion;

  public ParentWorkFlowInstanceImpl(String instanceId, WorkFlowDefinition definition) {
    super(instanceId, definition);
    this.definitionCode = definition.code();
    this.definitionVersion = definition.version();
  }

  public ParentWorkFlowInstanceImpl(Long id, String instanceId, WorkFlowDefinition definition,
                                    WorkFlowInstanceState state) {
    super(instanceId, definition, state);
    this.id = id;
    this.definitionCode = definition.code();
    this.definitionVersion = definition.version();
  }

  public Long getId() {
    return id;
  }

  public String getDefinitionCode() {
    return definitionCode;
  }

  public Integer getDefinitionVersion() {
    return definitionVersion;
  }

}
