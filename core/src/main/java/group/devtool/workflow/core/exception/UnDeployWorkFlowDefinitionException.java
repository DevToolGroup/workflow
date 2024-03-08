package group.devtool.workflow.core.exception;

/**
 * 流程定义卸载异常
 */
public class UnDeployWorkFlowDefinitionException extends WorkFlowException {

  private static final String FORMAT = "流程定义卸载异常，编码: %s，原因: %s";

  public UnDeployWorkFlowDefinitionException(String code, String reason) {
    super(String.format(FORMAT, code, reason));
  }

}
