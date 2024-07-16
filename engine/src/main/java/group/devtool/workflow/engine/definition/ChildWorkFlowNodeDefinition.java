/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.engine.definition;

import java.io.Serializable;
import java.util.List;

/**
 * 嵌套子流程节点
 */
public abstract class ChildWorkFlowNodeDefinition implements WorkFlowNodeDefinition {

  /**
   * @return 子流程节点配置
   */
  public abstract ChildWorkFlowConfig getConfig();

  public abstract List<WorkFlowDefinition> getChild();

  public interface ChildWorkFlowConfig extends WorkFlowNodeConfig {

    /**
     * @return 子流程启动配置
     */
    List<? extends ChildStartUp> getStartUp();

  }

  public interface ChildStartUp extends Serializable {

    /**
     * @return 子流程编码
     */
    String getChildCode();

    /**
     * @return 子流程多实例数量
     */
    Integer getTaskNumber();

    /**
     * @return 子流程启动条件，满足当前条件则可以启动
     */
    String getExpression();

  }

}
