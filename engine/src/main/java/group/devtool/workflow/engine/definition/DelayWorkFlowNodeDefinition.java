/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.engine.definition;

import group.devtool.workflow.engine.common.TaskWorker;
import group.devtool.workflow.engine.common.TimeUnit;

/**
 * 延时任务节点定义
 */
public abstract class DelayWorkFlowNodeDefinition implements WorkFlowNodeDefinition {

  public abstract DelayWorkFlowConfig getConfig();

  public interface DelayWorkFlowConfig extends WorkFlowNodeConfig {

    Long getTime();

    TimeUnit getUnit();

    TaskWorker getWorker();

    Boolean getIgnoreResult();

    String getReturnVariable();

  }


  public interface JavaDelayWorkFlowConfig extends DelayWorkFlowConfig {

    String getClassName();

  }

  public interface ExpressionDelayWorkFlowConfig extends DelayWorkFlowConfig {

    String getExpression();

  }

  public interface GroovyDelayWorkFlowConfig extends DelayWorkFlowConfig {

    String getScript();

  }


}
