/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.engine.runtime;

import group.devtool.workflow.engine.common.JacksonUtils;
import group.devtool.workflow.engine.definition.WorkFlowNodeDefinition.WorkFlowNodeConfig;

/**
 * 流程节点
 */
public interface WorkFlowNode {

  /**
   * @return 流程节点ID
   */
  String getNodeId();

  /**
   * @return 流程节点编码
   */
  String getNodeCode();

  String getNodeClass();

  /**
   * 获取流程节点对应的流程任务。
   *
   * @apiNote 流程节点任务采用延时加载的方式，因此，如果流程任务未初始化，那么该方法会初始化流程任务
   *
   * @return 流程节点对应的任务
   */
  WorkFlowTask[] getTasks();

  /**
   * @return 流程实例ID
   */
  String getInstanceId();

  /**
   * @return 根流程实例ID
   */
  String getRootInstanceId();

  /**
   * 判断流程节点是否已完成
   *
   * @return 流程节点已完成返回true，相反，返回false
   */
  boolean done();

  /**
   * 节点后置处理
   */
  default void afterComplete() {

  }

  /**
   * 节点前置处理
   */
  default void beforeComplete() {
    
  }

  WorkFlowNodeConfig getConfig();

  default String getConfigText() {
    return JacksonUtils.serialize(getConfig());
  }

  Integer getVersion();

  /**
   * 流程节点状态
   */
  enum WorkFlowNodeState {

    DOING,

    DONE

  }


}
