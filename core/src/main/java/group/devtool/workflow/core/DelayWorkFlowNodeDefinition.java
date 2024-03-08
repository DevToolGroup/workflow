package group.devtool.workflow.core;

import java.io.Serializable;

/**
 * 延时任务节点定义
 */
public abstract class DelayWorkFlowNodeDefinition implements WorkFlowNodeDefinition {

  public abstract DelayTaskConfig getConfig();

  public interface DelayTaskConfig extends Serializable {

    Integer getTime();

    DelayUnit getUnit();

    WorkFlowDelayTask getTask();

    boolean ignoreResult();

    String getReturnVariable();

  }

  public enum DelayUnit {

    SECONDS(1000L),

    MINUTES(60 * 1000L),

    HOURS(60 * 60 * 1000L),

    DAYS(24 * 60 * 60 * 1000L),

    ;

    private final Long mills;

    DelayUnit(Long mills) {
      this.mills = mills;
    }

    public Long getMills() {
      return mills;
    }
  }

  public interface WorkFlowDelayTask extends Serializable {

    TaskWorker getWorker();

    String getReturnVariable();

  }

  public interface JavaDelayTask extends WorkFlowDelayTask {

    String getClassName();

  }

  public interface ExpressionDelayTask extends WorkFlowDelayTask {

    String getExpression();

  }

  public interface GroovyDelayTask extends WorkFlowDelayTask {

    String getScript();

  }

  public enum TaskWorker {

    JAVA,
  }

}
