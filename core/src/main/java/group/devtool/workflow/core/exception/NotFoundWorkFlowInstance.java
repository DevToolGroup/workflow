package group.devtool.workflow.core.exception;

/**
 * 流程实例不存在异常
 */
public class NotFoundWorkFlowInstance extends WorkFlowException {

  public NotFoundWorkFlowInstance(String instanceId, String rootInstanceId) {
    super(String.format("流程实例不存在，流程实例ID：%s，根流程实例ID：%s", instanceId, rootInstanceId));
  }

}
