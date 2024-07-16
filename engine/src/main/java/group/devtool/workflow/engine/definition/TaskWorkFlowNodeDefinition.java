/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.engine.definition;


import group.devtool.workflow.engine.common.TaskWorker;

/**
 * 类型为任务的流程节点定义
 */
public abstract class TaskWorkFlowNodeDefinition implements WorkFlowNodeDefinition {

  public abstract TaskWorkFlowNodeConfig getConfig();

  public interface TaskWorkFlowNodeConfig extends WorkFlowNodeConfig {

    Boolean getIgnoreResult();

    TaskWorker getWorker();

    String getReturnVariable();

  }

  /**
   * Java任务配置
   */
  public interface JavaTaskWorkFlowConfig extends TaskWorkFlowNodeConfig {

    String getClassName();

  }

  public interface ExpressionTaskConfig extends TaskWorkFlowNodeConfig {

    String getExpression();

  }

  public interface GroovyTaskConfig extends TaskWorkFlowNodeConfig {

    String getScript();

  }


}
