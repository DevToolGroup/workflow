/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.impl.entity;

/**
 * 流程变量实体
 */
public class WorkFlowVariableEntity {

  private static final String GLOBAL = "global";

  private static final String LOCAL = "local";

  private Long id;

  private String name;

  private String value;

  private String rootInstanceId;

  private String type;

  public WorkFlowVariableEntity() {

  }

  public WorkFlowVariableEntity(String name, String value, String instanceId) {
    this.name = name;
    this.value = value;
    this.rootInstanceId = instanceId;
    this.type = LOCAL;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getRootInstanceId() {
    return rootInstanceId;
  }

  public void setRootInstanceId(String instanceId) {
    this.rootInstanceId = instanceId;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public static boolean isGlobal(WorkFlowVariableEntity entity) {
    return GLOBAL.equals(entity.getType());
  }
}
