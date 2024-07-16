/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.impl.runtime;

import group.devtool.workflow.engine.runtime.UserWorkFlowNode;
import group.devtool.workflow.engine.definition.UserWorkFlowNodeDefinition;
import group.devtool.workflow.engine.runtime.UserWorkFlowTask;
import group.devtool.workflow.engine.WorkFlowContextImpl;
import group.devtool.workflow.engine.definition.WorkFlowNodeDefinition;
import group.devtool.workflow.engine.runtime.WorkFlowTask;
import group.devtool.workflow.impl.runtime.UserWorkFlowTaskImpl.UserWorkFlowTaskConfigImpl;
import group.devtool.workflow.engine.definition.UserWorkFlowNodeDefinition.UserWorkFlowConfig;

/**
 * {@link UserWorkFlowNode} 默认实现
 */
public class UserWorkFlowNodeImpl extends UserWorkFlowNode {

  private final String instanceId;

  private final String rootInstanceId;

  public UserWorkFlowNodeImpl(String nodeId, WorkFlowNodeDefinition definition, String instanceId, String rootInstanceId,
                              WorkFlowContextImpl context) {
    super(nodeId, definition, context);
    this.instanceId = instanceId;
    this.rootInstanceId = rootInstanceId;
  }

  public UserWorkFlowNodeImpl(String nodeId, String nodeCode, Integer version,
                              UserWorkFlowConfig config,
                              String instanceId, String rootInstanceId,
                              WorkFlowTask... tasks) {
    super(nodeId, nodeCode, version, config, tasks);
    this.instanceId = instanceId;
    this.rootInstanceId = rootInstanceId;
  }

  @Override
  protected UserWorkFlowTask[] doInitTask(UserWorkFlowNodeDefinition definition, WorkFlowContextImpl context)  {
    UserWorkFlowConfig config = ((UserWorkFlowConfig) getConfig());
    UserWorkFlowTaskImpl[] tasks = new UserWorkFlowTaskImpl[config.getMember().size()];
    for (int i = 0; i < config.getMember().size(); i++) {
      UserWorkFlowTaskConfigImpl userTaskConfig = new UserWorkFlowTaskConfigImpl(config.getMember().get(i));
      tasks[i] = new UserWorkFlowTaskImpl(getNodeId(), getNodeCode(), userTaskConfig, instanceId, rootInstanceId);
    }
    return tasks;
  }

  @Override
  public String getInstanceId() {
    return instanceId;
  }

  @Override
  public String getRootInstanceId() {
    return rootInstanceId;
  }

  @Override
  public String getNodeClass() {
    return "USER";
  }

}
