package group.devtool.workflow.core;


import group.devtool.workflow.core.exception.WorkFlowDefinitionException;

import java.util.List;

/**
 * 流程定义对象除了包含流程定义的基本信息（编码，版本，开始节点，结束节点）等，
 * 主要对外提供流程流转的能力。
 * 
 */
public interface WorkFlowDefinition {

  /**
   * @return 流程定义编码
   */
  String code();

  /**
   * @return 流程定义名称
   */
  String name();

  /**
   * @return 流程定义版本
   */
  Integer version();

  /**
   * 流程开始节点定义
   * 
   * @throws WorkFlowDefinitionException 流程定义异常
   * @return 流程开始节点定义
   */
  WorkFlowNodeDefinition start() throws WorkFlowDefinitionException;

  /**
   * 流程结束节点定义
   * 
   * @throws WorkFlowDefinitionException 流程定义异常
   * @return 流程结束节点定义
   */
  WorkFlowNodeDefinition end() throws WorkFlowDefinitionException;

  /**
   * 流程流转
   * 
   * @param nodeCode 流程节点编码
   * @param context  流程运行上下文
   * @return 节点定义列表
   */
  List<WorkFlowNodeDefinition> next(String nodeCode, WorkFlowContext context);

}