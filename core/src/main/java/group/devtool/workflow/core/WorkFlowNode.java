package group.devtool.workflow.core;

import group.devtool.workflow.core.exception.WorkFlowException;

/**
 * 流程节点
 */
public interface WorkFlowNode {

  /**
   * @return 流程节点编码
   */
  String getCode();

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
   * @throws WorkFlowException 流程异常
   */
  boolean done() throws WorkFlowException;

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

  String getNodeClass();

  WorkFlowTask[] getTasks() throws WorkFlowException;


  /**
   * 流程节点状态
   */
  enum WorkFlowNodeState {

    DOING,

    DONE

  }


}
