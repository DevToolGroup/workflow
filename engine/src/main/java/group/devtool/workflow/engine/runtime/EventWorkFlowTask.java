/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.engine.runtime;

import group.devtool.workflow.engine.WorkFlowContextImpl;

/**
 * 用户任务
 */
public abstract class EventWorkFlowTask extends AbstractWorkFlowTask {

  private final EventWorkFlowTaskConfig config;

  public EventWorkFlowTask(String taskId, String nodeId, String nodeCode, EventWorkFlowTaskConfig config, String instanceId) {
    super(taskId, nodeId, nodeCode, instanceId);
    this.config = config;
  }

  public EventWorkFlowTask(String taskId, String nodeId, String nodeCode, EventWorkFlowTaskConfig config, String instanceId, WorkFlowTaskState state) {
    super(taskId, nodeId, nodeCode, instanceId, state);
    this.config = config;
  }

  public EventWorkFlowTaskConfig getEventConfig() {
    return config;
  }

  @Override
  protected void doComplete(WorkFlowContextImpl context)  {
    doCustomComplete(context);
  }

  protected abstract void doCustomComplete(WorkFlowContextImpl context) ;


  public static abstract class EventWorkFlowTaskConfig implements WorkFlowTaskConfig {

    public abstract String getWaiting();

  }

}
