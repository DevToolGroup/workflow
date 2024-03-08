package group.devtool.workflow.impl;

import group.devtool.workflow.core.WorkFlowIdSupplier;
import group.devtool.workflow.core.exception.WorkFlowException;

import java.util.UUID;

/**
 * 基于UUID的流程ID默认实现
 */
public class WorkFlowIdSupplierImpl implements WorkFlowIdSupplier {

  private final WorkFlowConfigurationImpl config;

  public WorkFlowIdSupplierImpl() {
    this.config = WorkFlowConfigurationImpl.CONFIG;
  }

  @Override
  public String getInstanceId() throws WorkFlowException {
    return config.dbTransaction().doInTransaction(() -> {
      return UUID.randomUUID().toString();
    });
  }

  @Override
  public String getTaskId() throws WorkFlowException {
    return config.dbTransaction().doInTransaction(() -> {
      return UUID.randomUUID().toString();
    });
  }

  @Override
  public String getTransactionId() throws WorkFlowException {
    return config.dbTransaction().doInTransaction(() -> {
      return UUID.randomUUID().toString();
    });
  }

}
