/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.impl.runtime;

import group.devtool.workflow.engine.WorkFlowContextImpl;
import group.devtool.workflow.engine.definition.DelayWorkFlowNodeDefinition.JavaDelayWorkFlowConfig;
import group.devtool.workflow.engine.definition.DelayWorkFlowNodeDefinition.DelayWorkFlowConfig;
import group.devtool.workflow.engine.exception.SerializeException;
import group.devtool.workflow.engine.runtime.DelayWorkFlowTask.JavaDelayWorkFlowTask;
import group.devtool.workflow.impl.WorkFlowConfigurationImpl;

/**
 * {@link JavaDelayWorkFlowTask} 默认实现类
 */
public class JavaDelayWorkFlowTaskImpl extends JavaDelayWorkFlowTask {

  private final String rootInstanceId;

  private Long completeTime;

  private final String byteConfig;

  public JavaDelayWorkFlowTaskImpl(String nodeId, String nodeCode, DelayWorkFlowTaskConfig config, String instanceId, String rootInstanceId)  {
    super(WorkFlowConfigurationImpl.CONFIG.idSupplier().getTaskId(), nodeId, nodeCode, config, instanceId);
    this.rootInstanceId = rootInstanceId;
    this.byteConfig = getConfig(config);
  }

  public JavaDelayWorkFlowTaskImpl(String taskId, String nodeId, String nodeCode,
                                   DelayWorkFlowTaskConfig config, String instanceId, String rootInstanceId,
                                   WorkFlowTaskState state) throws SerializeException {
    super(taskId, nodeId, nodeCode, config, instanceId, state);
    this.rootInstanceId = rootInstanceId;
    this.byteConfig = getConfig(config);
  }

  public Long delay() {
    return getConfig().getTime() * getConfig().getUnit().getMills();
  }



  @Override
  public void doCustomComplete(WorkFlowContextImpl context) {
    // do nothing
    completeTime = System.currentTimeMillis();
  }

  public String getTaskClass() {
    return "JAVA";
  }

  public String getCompleteUser() {
    return null;
  }

  @Override
  public String getRootInstanceId() {
    return rootInstanceId;
  }

  public Long getCompleteTime() {
    return completeTime;
  }

  public String getTaskConfig() {
    return byteConfig;
  }

  public static class JavaDelayWorkFlowTaskConfigImpl extends JavaDelayWorkFlowTaskConfig {

    public JavaDelayWorkFlowTaskConfigImpl(DelayWorkFlowConfig config) {
      super(config.getIgnoreResult(), config.getReturnVariable(), config.getTime(), config.getUnit(), ((JavaDelayWorkFlowConfig) config).getClassName());
    }
  }
}
