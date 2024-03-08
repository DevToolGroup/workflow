package group.devtool.workflow.core;

import group.devtool.workflow.core.exception.ConfigException;

/**
 * 流程依赖服务配置
 */
public class WorkFlowConfiguration {

  private WorkFlowService service;

  private WorkFlowDefinitionService definitionService;

  private WorkFlowTransactionRetry transaction;

  private WorkFlowScheduler taskScheduler;

  private WorkFlowCallback callback;

  protected WorkFlowConfiguration() {

  }

  /**
   * @return 流程服务
   */
  public WorkFlowService service() {
    return service;
  }

  public void setService(WorkFlowService service) {
    this.service = service;
  }

  /**
   * @return 流程定义服务
   */
  public WorkFlowDefinitionService definitionService() {
    return definitionService;
  }

  public void setDefinitionService(WorkFlowDefinitionService definitionService) {
    this.definitionService = definitionService;
  }

  /**
   * @return 流程事务
   */
  public WorkFlowTransactionRetry transaction() {
    return transaction;
  }

  public void setTransaction(WorkFlowTransactionRetry transaction) {
    this.transaction = transaction;
  }

  /**
   * @return 流程延时任务
   */
  public WorkFlowScheduler taskScheduler() {
    return taskScheduler;
  }

  public void setTaskScheduler(WorkFlowScheduler taskScheduler) {
    if (null != taskScheduler && !taskScheduler.ready()) {
      throw new ConfigException("流程延时任务调度未就绪");
    }
    this.taskScheduler = taskScheduler;
  }

  /**
   * @return 流程回调服务
   */
  public WorkFlowCallback callback() {
    return callback;
  }

  public void setCallback(WorkFlowCallback callback) {
    this.callback = callback;
  }
}
