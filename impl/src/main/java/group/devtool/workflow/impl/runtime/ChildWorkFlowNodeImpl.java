/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.impl.runtime;

import group.devtool.workflow.engine.*;
import group.devtool.workflow.engine.definition.WorkFlowNodeDefinition;
import group.devtool.workflow.engine.definition.WorkFlowNodeDefinition.WorkFlowNodeConfig;
import group.devtool.workflow.engine.runtime.ChildWorkFlowNode;
import group.devtool.workflow.engine.runtime.ChildWorkFlowTask;
import group.devtool.workflow.engine.runtime.WorkFlowTask;
import group.devtool.workflow.impl.common.SpelExpressionUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link ChildWorkFlowNode} 嵌套子流程节点默认实现类
 */
public class ChildWorkFlowNodeImpl extends ChildWorkFlowNode {

  private final String instanceId;

  private final String rootInstanceId;

  public ChildWorkFlowNodeImpl(String nodeId, WorkFlowNodeDefinition definition,
                               String instanceId, String rootInstanceId,
                               WorkFlowContextImpl context) {
    super(nodeId, definition, context);
    this.rootInstanceId = rootInstanceId;
    this.instanceId = instanceId;
  }

  public ChildWorkFlowNodeImpl(String nodeId, String nodeCode, Integer version,
                               WorkFlowNodeConfig config,
                               String instanceId, String rootInstanceId,
                               WorkFlowTask... tasks) {
    super(nodeId, nodeCode, version, config, tasks);
    this.rootInstanceId = rootInstanceId;
    this.instanceId = instanceId;
  }

  @Override
  public String getInstanceId() {
    return instanceId;
  }

  public String getRootInstanceId() {
    return rootInstanceId;
  }

  @Override
  protected boolean match(String expression, WorkFlowContextImpl context) {
		Map<String, Object> variables = new HashMap<>(context.getVariableMap());
    return SpelExpressionUtil.getValue(expression, variables);
  }

  @Override
  protected ChildWorkFlowTask[] doInitTask(int taskNumber, WorkFlowContextImpl context)  {
    ChildWorkFlowTask[] tasks = new ChildWorkFlowTaskImpl[taskNumber];
    for (int i = 0; i < taskNumber; i++) {
      tasks[i] = new ChildWorkFlowTaskImpl(getNodeId(), getNodeCode(), instanceId, rootInstanceId);
    }
    return tasks;
  }

  public String getNodeClass() {
    return "CHILD";
  }

}
