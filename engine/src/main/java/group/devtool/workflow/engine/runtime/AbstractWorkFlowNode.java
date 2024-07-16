/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.engine.runtime;

import group.devtool.workflow.engine.WorkFlowContextImpl;
import group.devtool.workflow.engine.definition.WorkFlowNodeDefinition;
import group.devtool.workflow.engine.definition.WorkFlowNodeDefinition.WorkFlowNodeConfig;

/**
 * {@link WorkFlowNode} 抽象实现，定义流程节点的基本业务逻辑。
 */
public abstract class AbstractWorkFlowNode implements WorkFlowNode {

  private final String nodeId;

  private WorkFlowTask[] tasks;

  private final String nodeCode;

  private TaskSupplier taskSupplier;

  private final Integer version;

  private final WorkFlowNodeConfig config;

  public AbstractWorkFlowNode(String nodeId, WorkFlowNodeDefinition definition, WorkFlowContextImpl context) {
    this.nodeId = nodeId;
    this.nodeCode = definition.getCode();
    this.taskSupplier = () -> initTask(definition, context);
    this.config = definition.getConfig();
    this.version = 0;
  }

  public AbstractWorkFlowNode(String nodeId, String nodeCode, Integer version, WorkFlowNodeConfig config, WorkFlowTask... tasks) {
    this.nodeId = nodeId;
    this.nodeCode = nodeCode;
    this.tasks = tasks;
    this.config = config;
    this.version = version;
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
  public WorkFlowTask[] getTasks() {
    if (null != taskSupplier) {
      tasks = taskSupplier.get();
      taskSupplier = null;
    }
    return tasks;
  }

  /**
   * 初始化流程任务
   *
   * @param definition 流程节点定义
   * @param context 流程上下文
   * @return 流程任务数组
   */
  protected abstract WorkFlowTask[] initTask(WorkFlowNodeDefinition definition, WorkFlowContextImpl context);

  @Override
  public WorkFlowNodeConfig getConfig() {
    return config;
  }

  @Override
  public Integer getVersion() {
    return version;
  }

  /**
   * 流程任务初始化接口
   */
  public interface TaskSupplier {
  
    WorkFlowTask[] get() ;

  }
}
