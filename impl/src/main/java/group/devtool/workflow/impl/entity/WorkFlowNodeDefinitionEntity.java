/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.impl.entity;

public class WorkFlowNodeDefinitionEntity {

  /**
   * 数据库主键
   */
  private Long id;

  /**
   * 关联流程定义的流程编码
   */
  private String definitionCode;

  /**
   * 关联的根流程定义编码
   */
  private String rootDefinitionCode;

  /**
   * 版本号
   * 
   * 默认版本号：0
   */
  private Integer version;

  /**
   * 节点编码
   */
  private String code;

  /**
   * 节点名称
   */
  private String name;

  /**
   * 节点类型
   */
  private String type;

  /**
   * 节点配置
   */
  private String config;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getRootDefinitionCode() {
    return rootDefinitionCode;
  }

  public void setRootDefinitionCode(String rootDefinitionCode) {
    this.rootDefinitionCode = rootDefinitionCode;
  }


  public String getDefinitionCode() {
    return definitionCode;
  }

  public void setDefinitionCode(String parentCode) {
    this.definitionCode = parentCode;
  }

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
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

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getConfig() {
    return config;
  }

  public void setConfig(String config) {
    this.config = config;
  }

}
