package group.devtool.workflow.impl;

import java.util.List;

import group.devtool.workflow.core.exception.WorkFlowException;
import group.devtool.workflow.impl.WorkFlowSchedulerImpl.DelayItemImpl;

/**
 * 流程延时调度存储
 */
public class WorkFlowSchedulerRepository {

  private final WorkFlowConfigurationImpl config;

  public WorkFlowSchedulerRepository() {
    this.config = WorkFlowConfigurationImpl.CONFIG;
  }

  public void addTask(DelayItemImpl item) throws WorkFlowException {
    config.dbTransaction().doInTransaction(() -> {
      WorkFlowMapper mapper = config.mapper();
      mapper.addDelayTask(item);
      return true;
    });
  }

  public List<DelayItemImpl> loadTask() throws WorkFlowException {
    return config.dbTransaction().doInTransaction(() -> {
      WorkFlowMapper mapper = config.mapper();
      return mapper.loadDelayTask(System.currentTimeMillis());
    });
  }

  public void setDelaySuccess(DelayItemImpl item) throws WorkFlowException {
    config.dbTransaction().doInTransaction(() -> {
      WorkFlowMapper mapper = config.mapper();
      return mapper.setDelaySuccess(item.getItemId(), item.getRootInstanceId());
    });
  }
}
