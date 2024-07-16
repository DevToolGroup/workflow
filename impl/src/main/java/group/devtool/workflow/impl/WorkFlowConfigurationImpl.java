/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.impl;

import group.devtool.workflow.engine.*;
import group.devtool.workflow.engine.exception.ConfigException;
import group.devtool.workflow.impl.mapper.WorkFlowMapper;
import group.devtool.workflow.impl.repository.WorkFlowDefinitionRepository;
import group.devtool.workflow.impl.repository.WorkFlowRepository;
import group.devtool.workflow.impl.repository.WorkFlowSchedulerRepository;

import java.util.function.Supplier;

/**
 * {@link WorkFlowConfiguration} 默认实现
 */
public final class WorkFlowConfigurationImpl extends WorkFlowConfiguration {

  public static final WorkFlowConfigurationImpl CONFIG = new WorkFlowConfigurationImpl();

  private WorkFlowDefinitionRepository definitionRepository;

  private WorkFlowIdSupplier supplier;

  private WorkFlowSchedulerRepository schedulerRepository;

  private WorkFlowRepository repository;

  private WorkFlowFactory factory;

  private final int delayTaskParallel = 7;

  private Supplier<WorkFlowMapper> mapperSupplier;

  private WorkFlowConfigurationImpl() {

  }

  public WorkFlowDefinitionRepository definitionRepository() {
    if (null == dbTransaction()) {
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

  public WorkFlowIdSupplier idSupplier() {
    if (null == dbTransaction()) {
      throw new ConfigException("数据库事务管理器未设置");
    }
    return supplier;
  }
  public void setSupplier(WorkFlowIdSupplierImpl supplier) {
    this.supplier = supplier;
  }

  public WorkFlowSchedulerRepository schedulerRepository() {
    if (null == dbTransaction()) {
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
    if (null == dbTransaction()) {
      throw new ConfigException("数据库事务管理器未设置");
    }
    return repository;
  }

  public void setRepository(WorkFlowRepository repository) {
    this.repository = repository;
  }

  public WorkFlowDefinitionService definitionService() {
    if (null == dbTransaction()) {
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
    return mapperSupplier.get();
  }

  public void setMapper(Supplier<WorkFlowMapper> mapper) {
    this.mapperSupplier = mapper;
  }
}
