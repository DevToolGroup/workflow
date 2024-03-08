package group.devtool.workflow.core;

import java.io.Serializable;

/**
 * 嵌套子流程节点
 */
public abstract class ChildWorkFlowNodeDefinition implements WorkFlowNodeDefinition {

  public abstract WorkFlowChildConfig getConfig();

  public abstract WorkFlowDefinition getChild();

  public interface WorkFlowChildConfig extends Serializable {

    int getTaskNumber();

    String childCode();

  }

}
