/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.engine;

import group.devtool.workflow.engine.exception.ConfigException;

/**
 * 流程依赖服务配置
 */
public class WorkFlowConfiguration {

  private WorkFlowTransaction dbTransaction;

  private WorkFlowService service;

  private WorkFlowDefinitionService definitionService;

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

  /**
   * @return 事务控制
   */
  public WorkFlowTransaction dbTransaction() {
    return dbTransaction;
  }

  public void setDBTransaction(WorkFlowTransaction dbTransaction) {
    this.dbTransaction = dbTransaction;
  }

}