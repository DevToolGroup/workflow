package group.devtool.workflow.core.exception;

/**
 * 流程节点不存在异常
 */
public class NotFoundWorkFlowNode extends WorkFlowException {

  public NotFoundWorkFlowNode(String nodeCode, String instanceId) {
    super(String.format("流程节点不存在，节点编码：%s，实例ID：%s", nodeCode, instanceId));
  }

}
