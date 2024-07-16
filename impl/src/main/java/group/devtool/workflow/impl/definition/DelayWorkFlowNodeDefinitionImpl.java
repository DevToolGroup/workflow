/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.impl.definition;

import group.devtool.workflow.engine.common.TaskWorker;
import group.devtool.workflow.engine.common.TimeUnit;
import group.devtool.workflow.engine.definition.DelayWorkFlowNodeDefinition;
import group.devtool.workflow.engine.exception.IllegalWorkFlowDefinition;

/**
 * {@link DelayWorkFlowNodeDefinition} 默认实现
 */
public class DelayWorkFlowNodeDefinitionImpl extends DelayWorkFlowNodeDefinition {

  private final String code;

  private final String name;

  private final DelayWorkFlowConfig config;

  public DelayWorkFlowNodeDefinitionImpl(String code, String name, DelayWorkFlowConfig config) throws IllegalWorkFlowDefinition {
    if (null == code || null == name || null == config) {
      throw new IllegalWorkFlowDefinition("延时任务务节点编码、名称、配置不能为空");
    }
    this.code = code;
    this.name = name;
    this.config = config;
    if (!support(config)) {
      throw new IllegalWorkFlowDefinition(
          "节点定义配置类型不匹配，预期类型：JavaScheduleTask、GroovyScheduleTask、ExpressionScheduleTask");
    }
  }

  @Override
  public String getCode() {
    return code;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public DelayWorkFlowConfig getConfig() {
    return config;
  }

  /**
   * 暂时仅支持Java
   *
   * @param config 延时配置
   * @return 如果是支持的任务类型，返回true，相反返回false
   */
  private static boolean support(DelayWorkFlowConfig config) {
    return config instanceof JavaDelayWorkFlowConfig;
  }

  public class JavaDelayWorkFlowConfigImpl implements JavaDelayWorkFlowConfig {

    private Long time;

    private TimeUnit unit;

    private Boolean ignoreResult;

    private String returnVariable;

    private String className;

    public JavaDelayWorkFlowConfigImpl() {

    }

    public JavaDelayWorkFlowConfigImpl(Long time, TimeUnit unit, boolean ignoreResult, String returnVariable, String className) {
      this.time = time;
      this.unit = unit;
      this.ignoreResult = ignoreResult;
      this.returnVariable = returnVariable;
      this.className = className;
    }


    @Override
    public Long getTime() {
      return time;
    }

    @Override
    public TimeUnit getUnit() {
      return unit;
    }

    @Override
    public TaskWorker getWorker() {
      return TaskWorker.JAVA;
    }

    @Override
    public Boolean getIgnoreResult() {
      return ignoreResult;
    }


    @Override
    public String getReturnVariable() {
      return returnVariable;
    }

    public void setTime(Long time) {
      this.time = time;
    }

    public void setUnit(TimeUnit unit) {
      this.unit = unit;
    }

    public void setIgnoreResult(boolean ignoreResult) {
      this.ignoreResult = ignoreResult;
    }

    public void setReturnVariable(String returnVariable) {
      this.returnVariable = returnVariable;
    }

    @Override
    public String getClassName() {
      return className;
    }
  }

  @Override
  public String getType() {
    return "DELAY";
  }

}
