/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.engine.runtime;

public abstract class ChildWorkFlowTask extends AbstractWorkFlowTask {

  public ChildWorkFlowTask(String taskId, String nodeId, String nodeCode, String instanceId) {
    super(taskId, nodeId, nodeCode, instanceId);
  }

  public ChildWorkFlowTask(String taskId, String nodeId, String nodeCode, String instanceId, WorkFlowTaskState state) {
    super(taskId, nodeId, nodeCode, instanceId, state);
  }
}
