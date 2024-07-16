/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.engine.exception;

/**
 * 流程节点不存在异常
 */
public class NotFoundWorkFlowNode extends WorkFlowRuntimeException {

  public NotFoundWorkFlowNode(String nodeId, String instanceId) {
    super(String.format("流程节点不存在，节点ID：%s，实例ID：%s", nodeId, instanceId));
  }

}
