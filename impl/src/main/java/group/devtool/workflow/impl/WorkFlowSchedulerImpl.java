package group.devtool.workflow.impl;

import java.util.ArrayList;
import java.util.List;

import group.devtool.workflow.core.ThreadWorkFlowScheduler;
import group.devtool.workflow.core.WorkFlowEngine;
import group.devtool.workflow.core.exception.WorkFlowException;

public class WorkFlowSchedulerImpl extends ThreadWorkFlowScheduler {

  private final WorkFlowSchedulerRepository repository;

  public WorkFlowSchedulerImpl() {
    super(WorkFlowConfigurationImpl.CONFIG.delayTaskParallel());
    repository = WorkFlowConfigurationImpl.CONFIG.schedulerRepository();
  }

  @Override
  public void addTask(DelayItem item) throws WorkFlowException {
    repository.addTask((DelayItemImpl) item);
  }

  @Override
  protected List<DelayItem> loadTask() throws WorkFlowException {
    List<DelayItem> items = new ArrayList<>();
    items.addAll(repository.loadTask());
    return items;
  }

  @Override
  protected void delayAfter(DelayItem item, WorkFlowException exception) {
    if (null != exception) {
      return;
    }
    try {
      repository.setDelaySuccess((DelayItemImpl) item);
    } catch (WorkFlowException e) {
      e.printStackTrace();
    }
  }

  /**
   * {@link DelayItem} 默认实现
   */
  public static class DelayItemImpl implements DelayItem {

    private Long delay;

    private String itemId;

    private String taskId;

    private String rootInstanceId;

    public DelayItemImpl() {

    }

    public DelayItemImpl(Long delay, String taskId, String rootInstanceId) {
      this.delay = delay;
      this.taskId = taskId;
      this.rootInstanceId = rootInstanceId;
    }

    public String getTaskId() {
      return taskId;
    }

    public String getRootInstanceId() {
      return rootInstanceId;
    }

    @Override
    public Long getDelay() {
      return delay;
    }

    public String getItemId() {
      return itemId;
    }

    public void setItemId(String itemId) {
      this.itemId = itemId;
    }

    public void setDelay(Long delay) {
      this.delay = delay;
    }

    public void setTaskId(String taskId) {
      this.taskId = taskId;
    }

    public void setRootInstanceId(String rootInstanceId) {
      this.rootInstanceId = rootInstanceId;
    }

    @Override
    public void run() throws WorkFlowException {
      new WorkFlowEngine(WorkFlowConfigurationImpl.CONFIG).run(rootInstanceId, taskId);
    }
  }

}
