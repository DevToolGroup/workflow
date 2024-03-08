package group.devtool.workflow.core;


import java.io.Serializable;

/**
 * 类型为任务的流程节点定义
 */
public abstract class TaskWorkFlowNodeDefinition implements WorkFlowNodeDefinition {

  public abstract WorkFlowTaskConfig getConfig();

  public interface WorkFlowTaskConfig extends Serializable {

    boolean ignoreResult();

    TaskWorker getWorker();

    String getReturnVariable();

  }

  /**
   * Java任务配置
   */
  public interface JavaTaskConfig extends WorkFlowTaskConfig {

    String getClassName();

  }

  public interface ExpressionTaskConfig extends WorkFlowTaskConfig {

    String getExpression();

  }

  public interface GroovyTaskConfig extends WorkFlowTaskConfig {

    String getScript();

  }

  public enum TaskWorker {

    JAVA,
    GROOVY,
    SPEL
  }
}
