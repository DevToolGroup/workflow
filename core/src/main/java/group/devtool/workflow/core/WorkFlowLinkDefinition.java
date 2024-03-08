package group.devtool.workflow.core;

/**
 * 流程节点间连线定义
 */
public interface WorkFlowLinkDefinition {

  /**
   * 连接线目标节点
   * 
   * @return 目标节点编码
   */
	String getTarget();

  /**
   * 判断连接线是否从指定参数处开始
   * 
   * @param source 起始节点编码
   * @return true: 从直接节点开始，false: 相反
   */
	boolean from(String source);

  /**
   * 根据流程上下文判断当前连接线条件是否满足
   * 
   * @param context 流程上下文
   * @return true: 满足, false: 相反
   */
	boolean match(WorkFlowContext context);

}
