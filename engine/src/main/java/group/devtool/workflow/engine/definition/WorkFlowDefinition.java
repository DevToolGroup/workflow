/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.engine.definition;


import group.devtool.workflow.engine.WorkFlowContextImpl;

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
  String getCode();

  /**
   * @return 流程定义名称
   */
  String getName();

  /**
   * @return 流程定义版本
   */
  Integer getVersion();

  /**
   * @return 根流程定义版本
   */
  String getRootCode();

  /**
   * 流程开始节点定义
   * 
   * @return 流程开始节点定义
   */
  WorkFlowNodeDefinition getStartNode();

  /**
   * 流程结束节点定义
   * 
   * @return 流程结束节点定义
   */
  WorkFlowNodeDefinition getEndNode();

  /**
   * 流程流转
   * 
   * @param nodeCode 流程节点编码
   * @param context  流程运行上下文
   * @return 节点定义列表
   */
  List<WorkFlowNodeDefinition> next(String nodeCode, WorkFlowContextImpl context);

}
