/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.impl.entity;

import group.devtool.workflow.engine.WorkFlowEngine;
import group.devtool.workflow.engine.WorkFlowDelayTaskScheduler.DelayItem;
import group.devtool.workflow.impl.WorkFlowConfigurationImpl;

/**
 * {@link DelayItem} 默认实现
 */
public class WorkFlowDelayItemEntity implements DelayItem {

	private Long delay;

	private String itemId;

	private String taskId;

	private String rootInstanceId;

	public WorkFlowDelayItemEntity() {

	}

	public WorkFlowDelayItemEntity(Long delay, String taskId, String rootInstanceId) {
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
	public void run() {
		new WorkFlowEngine(WorkFlowConfigurationImpl.CONFIG).run(rootInstanceId, taskId);
	}
}
