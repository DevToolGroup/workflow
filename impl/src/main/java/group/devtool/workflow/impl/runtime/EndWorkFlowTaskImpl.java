/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.impl.runtime;

import group.devtool.workflow.engine.runtime.EndWorkFlowTask;
import group.devtool.workflow.engine.WorkFlowContextImpl;
import group.devtool.workflow.impl.WorkFlowConfigurationImpl;

/**
 * {@link EndWorkFlowTask} 默认实现
 */
public class EndWorkFlowTaskImpl extends EndWorkFlowTask {

  private Long completeTime;

  private final String rootInstanceId;

  public EndWorkFlowTaskImpl(String nodeId, String nodeCode, String instanceId, String rootInstanceId)  {
    super(WorkFlowConfigurationImpl.CONFIG.idSupplier().getTaskId(), nodeId, nodeCode, instanceId);
    this.rootInstanceId = rootInstanceId;
  }

  public EndWorkFlowTaskImpl(String taskId, String nodeId, String nodeCode,
                             WorkFlowTaskState taskState, String instanceId,
                             String rootInstanceId) {
    super(taskId, nodeId, nodeCode, instanceId, taskState);
    this.rootInstanceId = rootInstanceId;
  }

  @Override
  protected void doComplete(WorkFlowContextImpl context)  {
    completeTime = System.currentTimeMillis();
  }

  public String getRootInstanceId() {
    return rootInstanceId;
  }

  public String getTaskClass() {
    return "END";
  }

  public String getCompleteUser() {
    return null;
  }

  public Long getCompleteTime() {
    return completeTime;
  }

  public String getTaskConfig() {
    return null;
  }
}
