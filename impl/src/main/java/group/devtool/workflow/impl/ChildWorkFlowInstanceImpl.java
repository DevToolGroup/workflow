package group.devtool.workflow.impl;

import group.devtool.workflow.core.ChildWorkFlowInstance;
import group.devtool.workflow.core.WorkFlowDefinition;

/**
 * {@link ChildWorkFlowInstance} 默认实现类
 */
public class ChildWorkFlowInstanceImpl extends ChildWorkFlowInstance {

  private Long id;

  private final String definitionCode;

  private final Integer definitionVersion;

  public ChildWorkFlowInstanceImpl(String instanceId, String parentId, String rootId,
                                   WorkFlowDefinition definition) {
    super(instanceId, parentId, rootId, definition);
    this.definitionCode = definition.code();
    this.definitionVersion = definition.version();
  }

  public ChildWorkFlowInstanceImpl(Long id, String instanceId, String parentId, String rootId,
                                   WorkFlowDefinition definition,
                                   WorkFlowInstanceState state) {
    super(instanceId, parentId, rootId, definition, state);
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
