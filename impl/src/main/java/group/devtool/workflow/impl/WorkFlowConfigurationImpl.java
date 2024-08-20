/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.impl;

import group.devtool.workflow.engine.*;
import group.devtool.workflow.engine.exception.ConfigurationException;
import group.devtool.workflow.impl.mapper.WorkFlowMapper;
import group.devtool.workflow.impl.repository.WorkFlowDefinitionRepository;
import group.devtool.workflow.impl.repository.WorkFlowOperationRepository;
import group.devtool.workflow.impl.repository.WorkFlowRepository;
import group.devtool.workflow.impl.repository.WorkFlowSchedulerRepository;

import java.util.function.Supplier;

/**
 * {@link WorkFlowConfiguration} 默认实现
 */
public final class WorkFlowConfigurationImpl extends WorkFlowConfiguration {
  private static final String ERROR = "数据库事务管理器未设置";

  public static final WorkFlowConfigurationImpl CONFIG = new WorkFlowConfigurationImpl();

  private WorkFlowDefinitionRepository definitionRepository;

  private WorkFlowSchedulerRepository schedulerRepository;

  private WorkFlowRepository repository;

  private WorkFlowFactory factory;

  private int delayTaskParallel = 7;

  private Supplier<WorkFlowMapper> mapperSupplier;

  private WorkFlowOperationRepository operationRepository;

  private WorkFlowCallbackRepository callbackRepository;

  public WorkFlowDefinitionRepository definitionRepository() {
    if (null == dbTransaction()) {
      throw new ConfigurationException(ERROR);
    }
    return definitionRepository;
  }

  public void setDefinitionRepository(WorkFlowDefinitionRepository definitionRepository) {
    this.definitionRepository = definitionRepository;
  }

  public WorkFlowDefinitionFactory definitionFactory() {
    return WorkFlowDefinitionFactory.DEFINITION;
  }

  public WorkFlowSchedulerRepository schedulerRepository() {
    if (null == dbTransaction()) {
      throw new ConfigurationException(ERROR);
    }
    if (null == schedulerRepository) {
      throw new ConfigurationException("延时调度服务未设置");
    }
    return schedulerRepository;
  }

  public void setSchedulerRepository(WorkFlowSchedulerRepository schedulerRepository) {
    this.schedulerRepository = schedulerRepository;
  }

  public int delayTaskParallel() {
    return delayTaskParallel;
  }

  public void setDelayTaskParallel(int delayTaskParallel) {
    this.delayTaskParallel = delayTaskParallel;
  }

  public WorkFlowRepository repository() {
    if (null == dbTransaction()) {
      throw new ConfigurationException(ERROR);
    }
    return repository;
  }

  public void setRepository(WorkFlowRepository repository) {
    this.repository = repository;
  }

  @Override
  public WorkFlowDefinitionService definitionService() {
    if (null == dbTransaction()) {
      throw new ConfigurationException(ERROR);
    }
    if (null == definitionRepository) {
      throw new ConfigurationException("流程定义存储服务未设置");
    }
    return super.definitionService();
  }

  public WorkFlowFactory factory() {
    if (null == idSupplier()) {
      throw new ConfigurationException("流程ID生成器未设置");
    }
    return factory;
  }

  public void setFactory(WorkFlowFactory factory) {
    this.factory = factory;
  }

  public WorkFlowMapper getMapper() {
    return mapperSupplier.get();
  }

  public void setMapper(Supplier<WorkFlowMapper> mapper) {
    this.mapperSupplier = mapper;
  }

  public WorkFlowOperationRepository operationRepository() {
    return operationRepository;
  }

  public void setOperationRepository(WorkFlowOperationRepository operationRepository) {
    this.operationRepository = operationRepository;
  }

  public WorkFlowCallbackRepository callbackRepository() {
    return callbackRepository;
  }

  public void setCallbackRepository(WorkFlowCallbackRepository callbackRepository) {
    this.callbackRepository = callbackRepository;
  }
}
