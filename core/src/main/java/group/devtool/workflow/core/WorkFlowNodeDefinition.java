package group.devtool.workflow.core;


/**
 * 流程节点定义
 */
public interface WorkFlowNodeDefinition {

  /**
   * @return 节点编码
   */
	String getCode();

  /**
   * @return 节点名称
   */
	String getName();

	/**
	 * @return 节点类型
	 */
	String getType();

	/**
	 * @return 节点配置
	 */
	Object getConfig();

}
