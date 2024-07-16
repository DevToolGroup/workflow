/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.impl.entity;

import java.util.List;

public class WorkFlowDefinitionEntity {

  /**
   * 数据库主键
   */
  private Integer id;

  /**
   * 流程定义编码
   */
  private String code;

  /**
   * 流程定义名称
   */
  private String name;

  /**
   * 版本号
   * 
   * 默认版本号：0
   */
  private Integer version;

  /**
   * 状态
   * 
   * N-已卸载
   * Y-已部署
   */
  private String state;

  /**
   * 流程定义关联的节点编码
   * 嵌套子流程场景下，改字段必有值
   */
  private String nodeCode;

  /**
   * 根流程定义编码
   */
  private String rootCode;

  /**
   * 流程节点连线定义
   */
  private List<WorkFlowLinkDefinitionEntity> links;

  /**
   * 流程节点定义
   */
  private List<WorkFlowNodeDefinitionEntity> nodes;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getRootCode() {
    return rootCode;
  }

  public void setRootCode(String rootCode) {
    this.rootCode = rootCode;
  }

  public String getNodeCode() {
    return nodeCode;
  }

  public void setNodeCode(String nodeCode) {
    this.nodeCode = nodeCode;
  }

  public List<WorkFlowNodeDefinitionEntity> getNodes() {
    return nodes;
  }

  public void setNodes(List<WorkFlowNodeDefinitionEntity> nodes) {
    this.nodes = nodes;
  }

  public List<WorkFlowLinkDefinitionEntity> getLinks() {
    return links;
  }

  public void setLinks(List<WorkFlowLinkDefinitionEntity> links) {
    this.links = links;
  }

}
