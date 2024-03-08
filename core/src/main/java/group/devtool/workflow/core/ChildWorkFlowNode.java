package group.devtool.workflow.core;

import group.devtool.workflow.core.exception.WorkFlowException;

import java.util.List;

/**
 * 嵌套子流程节点
 */
public interface ChildWorkFlowNode extends WorkFlowNode {

  /**
   * 嵌套子流程实例初始化
   * 
   * @param childFactory 嵌套子流程实例工厂
   * @return 流程实例列表
   * @throws WorkFlowException 流程实例初始化异常
   */
	List<WorkFlowInstance> instances(ChildFactory childFactory) throws WorkFlowException;

  /**
   * 嵌套子流程实例工厂接口
   */
	interface ChildFactory {

    /**
     * 嵌套子流程实例化
     * 
     * @param definitionCode 嵌套子流程定义
     * @param taskId 子流程节点任务ID
     * @return 子流程实例
     * @throws WorkFlowException 流程实例化异常
     */
		WorkFlowInstance apply(String definitionCode, String taskId) throws WorkFlowException;

  }

}
