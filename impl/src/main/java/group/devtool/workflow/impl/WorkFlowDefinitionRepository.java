package group.devtool.workflow.impl;

import java.util.List;

/**
 * 流程定义存储服务
 */
public class WorkFlowDefinitionRepository {

  private final WorkFlowConfigurationImpl config;

  public WorkFlowDefinitionRepository() {
    this.config = WorkFlowConfigurationImpl.CONFIG;
  }

  public void bulkSave(List<WorkFlowDefinitionEntity> entities) {
    WorkFlowMapper mapper = config.mapper();
    mapper.bulkSaveDefinition(entities);
  }

  public List<WorkFlowDefinitionEntity> loadDefinition(String code, Integer version, Boolean recursion) {
    WorkFlowMapper mapper = config.mapper();
    return mapper.loadDefinition(code, version, recursion);
  }

  public WorkFlowDefinitionEntity loadDeployedDefinition(String code) {
    WorkFlowMapper mapper = config.mapper();
    return mapper.loadDeployedDefinition(code);
  }

  public Integer loadDefinitionLatestVersion(String code) {
    WorkFlowMapper mapper = config.mapper();
    return mapper.loadDefinitionLatestVersion(code);
  }

  public List<WorkFlowNodeDefinitionEntity> loadNodeDefinition(String definitionCode, Integer version,
                                                               Boolean recursion) {
    WorkFlowMapper mapper = config.mapper();
    return mapper.loadNodeDefinition(definitionCode, version, recursion);
  }

  public List<WorkFlowLinkDefinitionEntity> loadLinkDefinition(String definitionCode, Integer version,
                                                               Boolean recursion) {
    WorkFlowMapper mapper = config.mapper();
    return mapper.loadLinkDefinition(definitionCode, version, recursion);
  }

  public int changeState(String code, String afterState, String beforeState) {
    WorkFlowMapper mapper = config.mapper();
    return mapper.changeState(code, afterState, beforeState);
  }

  public void bulkSaveNode(List<WorkFlowNodeDefinitionEntity> nodes) {
    WorkFlowMapper mapper = config.mapper();
    mapper.bulkSaveNodeDefinition(nodes);
  }

  public void bulkSaveLink(List<WorkFlowLinkDefinitionEntity> links) {
    WorkFlowMapper mapper = config.mapper();
    mapper.bulkSaveLinkDefinition(links);
  }

}
