package group.devtool.workflow.impl;

import group.devtool.workflow.core.*;
import group.devtool.workflow.core.exception.ConfigException;

/**
 * {@link WorkFlowConfiguration} 默认实现
 */
public final class WorkFlowConfigurationImpl extends WorkFlowConfiguration {

  public static final WorkFlowConfigurationImpl CONFIG = new WorkFlowConfigurationImpl();

  private WorkFlowDefinitionRepository definitionRepository;

  private WorkFlowTransaction dbTransaction;

  private WorkFlowIdSupplier supplier;

  private WorkFlowSchedulerRepository schedulerRepository;

  private WorkFlowRepository repository;

  private WorkFlowFactory factory;

  private WorkFlowMapper mapper;

  private final int delayTaskParallel = 7;

  private WorkFlowConfigurationImpl() {

  }

  public WorkFlowDefinitionRepository definitionRepository() {
    if (null == dbTransaction) {
      throw new ConfigException("数据库事务管理器未设置");
    }
    return definitionRepository;
  }

  public void setDefinitionRepository(WorkFlowDefinitionRepository definitionRepository) {
    this.definitionRepository = definitionRepository;
  }

  public WorkFlowDefinitionFactory definitionFactory() {
    return WorkFlowDefinitionFactory.DEFINITION;
  }

  public WorkFlowTransaction dbTransaction() {
    return dbTransaction;
  }

  public void setDbTransaction(WorkFlowTransaction dbTransaction) {
    this.dbTransaction = dbTransaction;
  }

  public WorkFlowIdSupplier idSupplier() {
    if (null == dbTransaction) {
      throw new ConfigException("数据库事务管理器未设置");
    }
    return supplier;
  }
  public void setSupplier(WorkFlowIdSupplierImpl supplier) {
    this.supplier = supplier;
  }

  public WorkFlowSchedulerRepository schedulerRepository() {
    if (null == dbTransaction) {
      throw new ConfigException("数据库事务管理器未设置");
    }
    if (null == schedulerRepository) {
      throw new ConfigException("延时调度服务未设置");
    }
    return schedulerRepository;
  }

  public void setSchedulerRepository(WorkFlowSchedulerRepository schedulerRepository) {
    this.schedulerRepository = schedulerRepository;
  }

  public int delayTaskParallel() {
    return delayTaskParallel;
  }

  public WorkFlowRepository repository() {
    if (null == dbTransaction) {
      throw new ConfigException("数据库事务管理器未设置");
    }
    return repository;
  }

  public void setRepository(WorkFlowRepository repository) {
    this.repository = repository;
  }

  public WorkFlowDefinitionService definitionService() {
    if (null == dbTransaction) {
      throw new ConfigException("数据库事务管理器未设置");
    }
    if (null == definitionRepository) {
      throw new ConfigException("流程定义存储服务未设置");
    }
    return super.definitionService();
  }

  public WorkFlowFactory factory() {
    if (null == supplier) {
      throw new ConfigException("流程ID生成器未设置");
    }
    return factory;
  }

  public void setFactory(WorkFlowFactory factory) {
    this.factory = factory;
  }

  public WorkFlowMapper mapper() {
    return mapper;
  }

  public void setMapper(WorkFlowMapper mapper) {
    this.mapper = mapper;
  }
}
