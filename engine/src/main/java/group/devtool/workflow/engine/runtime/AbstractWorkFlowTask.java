/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.engine.runtime;

import group.devtool.workflow.engine.WorkFlowContextImpl;

/**
 * {@link WorkFlowTask} 抽象实现类，用于定义流程任务的基本业务流程
 */
public abstract class AbstractWorkFlowTask implements WorkFlowTask {

  private final String taskId;

  private final String nodeId;

  private final String nodeCode;

  private final String instanceId;

  private WorkFlowTaskState state;

  public AbstractWorkFlowTask(String taskId, String nodeId, String nodeCode, String instanceId) {
    this(taskId, nodeId, nodeCode, instanceId, WorkFlowTaskState.DOING);
  }

  public AbstractWorkFlowTask(String taskId, String nodeId, String nodeCode, String instanceId, WorkFlowTaskState state) {
    this.taskId = taskId;
    this.nodeId = nodeId;
    this.nodeCode = nodeCode;
    this.state = state;
    this.instanceId = instanceId;
  }

  @Override
  public void complete(WorkFlowContextImpl context)  {
    doComplete(context);
    state = WorkFlowTaskState.DONE;
  }

  protected abstract void doComplete(WorkFlowContextImpl context) ;

  @Override
  public String getTaskId() {
    return taskId;
  }

  @Override
  public String getNodeId() {
    return nodeId;
  }

  @Override
  public String getNodeCode() {
    return nodeCode;
  }

  @Override
  public String getInstanceId() {
    return instanceId;
  }

  @Override
  public boolean completed() {
    return WorkFlowTaskState.DONE == state;
  }
}
