/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.engine.exception;

/**
 * 流程定义不存在
 */
public class NotFoundWorkFlowDefinition extends WorkFlowRuntimeException {

  public NotFoundWorkFlowDefinition(String code, Integer version) {
    super(String.format("流程定义不存在，编码：%s，版本：%s", code, version));
  }

  public NotFoundWorkFlowDefinition(String message) {
    super(message);
  }

}
