/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.impl;

import java.util.ArrayList;
import java.util.List;

import group.devtool.workflow.engine.ThreadWorkFlowScheduler;
import group.devtool.workflow.impl.entity.WorkFlowDelayItemEntity;
import group.devtool.workflow.impl.repository.WorkFlowSchedulerRepository;

public class WorkFlowSchedulerImpl extends ThreadWorkFlowScheduler {

  private final WorkFlowSchedulerRepository repository;

  public WorkFlowSchedulerImpl() {
    super(WorkFlowConfigurationImpl.CONFIG.delayTaskParallel());
    repository = WorkFlowConfigurationImpl.CONFIG.schedulerRepository();
  }

  @Override
  public void addTask(DelayItem item)  {
    repository.addTask((WorkFlowDelayItemEntity) item);
  }

  @Override
  protected List<DelayItem> loadTask()  {
		return new ArrayList<>(repository.loadTask());
  }

  @Override
  protected void delayAfter(DelayItem item) {
		repository.setDelaySuccess((WorkFlowDelayItemEntity) item);
	}

}
