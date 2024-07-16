/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.impl.entity;

/**
 * 流程实例实体
 */
public class WorkFlowInstanceEntity {

  private Long id;

  private String scope;

  private String state;

  private String instanceId;

  private String definitionCode;

  private String rootDefinitionCode;

  private int definitionVersion;

  private String parentTaskId;

  private String rootInstanceId;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String id) {
    this.instanceId = id;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getDefinitionCode() {
    return definitionCode;
  }

  public void setDefinitionCode(String definitionCode) {
    this.definitionCode = definitionCode;
  }

  public int getDefinitionVersion() {
    return definitionVersion;
  }

  public void setDefinitionVersion(int definitionVersion) {
    this.definitionVersion = definitionVersion;
  }

  public String getRootInstanceId() {
    return rootInstanceId;
  }

  public String getParentTaskId() {
    return parentTaskId;
  }

  public void setParentTaskId(String parentTaskId) {
    this.parentTaskId = parentTaskId;
  }

  public void setRootInstanceId(String rootId) {
    this.rootInstanceId = rootId;
  }

  public String getScope() {
    return scope;
  }

  public void setScope(String scope) {
    this.scope = scope;
  }

  public String getRootDefinitionCode() {
    return rootDefinitionCode;
  }

  public void setRootDefinitionCode(String rootDefinitionCode) {
    this.rootDefinitionCode = rootDefinitionCode;
  }
}
