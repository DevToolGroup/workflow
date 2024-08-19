/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.impl.repository;

import group.devtool.workflow.impl.WorkFlowConfigurationImpl;
import group.devtool.workflow.impl.mapper.WorkFlowMapper;
import group.devtool.workflow.impl.entity.WorkFlowDefinitionEntity;
import group.devtool.workflow.impl.entity.WorkFlowLinkDefinitionEntity;
import group.devtool.workflow.impl.entity.WorkFlowNodeDefinitionEntity;

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
    WorkFlowMapper mapper = config.getMapper();
    mapper.bulkSaveDefinition(entities);
  }

  public List<WorkFlowDefinitionEntity> loadDefinition(String code, String rootCode, Integer version, Boolean recursion) {
    WorkFlowMapper mapper = config.getMapper();
    return mapper.loadDefinition(code, rootCode, version, recursion);
  }

  public WorkFlowDefinitionEntity loadDeployedDefinition(String code, String rootCode) {
    WorkFlowMapper mapper = config.getMapper();
    return mapper.loadDeployedDefinition(code, rootCode);
  }

  public Integer loadDefinitionLatestVersion(String code, String rootCode) {
    WorkFlowMapper mapper = config.getMapper();
    return mapper.loadDefinitionLatestVersion(code, rootCode);
  }

  public List<WorkFlowNodeDefinitionEntity> loadNodeDefinition(String definitionCode, String rootDefinitionCode,
                                                               Integer version,
                                                               Boolean recursion) {
    WorkFlowMapper mapper = config.getMapper();
    return mapper.loadNodeDefinition(definitionCode, rootDefinitionCode, version, recursion);
  }

  public List<WorkFlowLinkDefinitionEntity> loadLinkDefinition(String definitionCode, String rootDefinitionCode,
                                                               Integer version,
                                                               Boolean recursion) {
    WorkFlowMapper mapper = config.getMapper();
    return mapper.loadLinkDefinition(definitionCode, rootDefinitionCode, version, recursion);
  }

  public int undeploy(String code, String afterState, String beforeState) {
    WorkFlowMapper mapper = config.getMapper();
    return mapper.changeState(code, afterState, beforeState);
  }

  public void bulkSaveNode(List<WorkFlowNodeDefinitionEntity> nodes) {
    WorkFlowMapper mapper = config.getMapper();
    mapper.bulkSaveNodeDefinition(nodes);
  }

  public void bulkSaveLink(List<WorkFlowLinkDefinitionEntity> links) {
    WorkFlowMapper mapper = config.getMapper();
    mapper.bulkSaveLinkDefinition(links);
  }

}
