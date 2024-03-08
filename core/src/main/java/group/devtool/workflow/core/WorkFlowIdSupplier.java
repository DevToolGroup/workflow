package group.devtool.workflow.core;

import group.devtool.workflow.core.exception.WorkFlowException;

/**
 * 流程引擎ID生成器
 */
public interface WorkFlowIdSupplier {

  /**
   * @return 实例ID
   * @throws WorkFlowException 流程异常
   */
  String getInstanceId() throws WorkFlowException;

  /**
   * @return 任务ID
   * @throws WorkFlowException 流程异常
   */
  String getTaskId() throws WorkFlowException;

  /**
   * @return 任务ID
   * @throws WorkFlowException 流程异常
   */
  String getTransactionId() throws WorkFlowException;
}
