package group.devtool.workflow.core;


import java.io.Serializable;
import java.util.List;

/**
 * 触发式流程节点定义
 */
public abstract class UserWorkFlowNodeDefinition implements WorkFlowNodeDefinition {

  public abstract WorkFlowUserConfig getConfig();

  /**
   * 用户配置
   */
  public interface WorkFlowUserConfig extends Serializable {

    List<String> member();

    Integer confirm();

  }

}
