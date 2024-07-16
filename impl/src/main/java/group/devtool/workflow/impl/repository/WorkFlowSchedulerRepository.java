/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.impl.repository;

import java.util.List;

import group.devtool.workflow.impl.WorkFlowConfigurationImpl;
import group.devtool.workflow.impl.entity.WorkFlowDelayItemEntity;
import group.devtool.workflow.impl.mapper.WorkFlowMapper;

/**
 * 流程延时调度存储
 */
public class WorkFlowSchedulerRepository {

	private final WorkFlowConfigurationImpl config;

	public WorkFlowSchedulerRepository() {
		this.config = WorkFlowConfigurationImpl.CONFIG;
	}

	public void addTask(WorkFlowDelayItemEntity item) {
		WorkFlowMapper mapper = config.mapper();
		mapper.addDelayTask(item);
	}

	public List<WorkFlowDelayItemEntity> loadTask() {
		WorkFlowMapper mapper = config.mapper();
		return mapper.loadDelayTask(System.currentTimeMillis());
	}

	public void setDelaySuccess(WorkFlowDelayItemEntity item) {
		WorkFlowMapper mapper = config.mapper();
		mapper.setDelaySuccess(item.getItemId(), item.getRootInstanceId());
	}
}
