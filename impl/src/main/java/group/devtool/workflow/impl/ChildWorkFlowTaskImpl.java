package group.devtool.workflow.impl;

import group.devtool.workflow.core.ChildWorkFlowTask;
import group.devtool.workflow.core.WorkFlowContext;
import group.devtool.workflow.core.exception.WorkFlowException;
import group.devtool.workflow.core.exception.SerializeException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * {@link ChildWorkFlowTask} 默认实现类
 */
public class ChildWorkFlowTaskImpl extends ChildWorkFlowTask {

  private final String rootInstanceId;

  private Long completeTime;

  public ChildWorkFlowTaskImpl(String node, String instanceId, String rootInstanceId) throws WorkFlowException {
    super(WorkFlowConfigurationImpl.CONFIG.idSupplier().getTaskId(), node, instanceId);
    this.rootInstanceId = rootInstanceId;
  }

  public ChildWorkFlowTaskImpl(String taskId, String node, String instanceId, String rootInstanceId,
                               WorkFlowTaskState state) {
    super(taskId, node, instanceId, state);
    this.rootInstanceId = rootInstanceId;
  }

  @Override
  protected void doComplete(WorkFlowContext context) {
    // do nothing
    completeTime = System.currentTimeMillis();
  }


  public String getRootInstanceId() {
    return rootInstanceId;
  }

  public String getTaskClass() {
    return "CHILD";
  }

  public String getCompleteUser() {
    return null;
  }

  public Long getCompleteTime() {
    return completeTime;
  }

  public byte[] getTaskConfig() {
    return null;
  }

  public byte[] getConfig(Serializable target) throws SerializeException {
    try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
         ObjectOutputStream oo = new ObjectOutputStream(bos)) {
      oo.writeObject(target);
      return bos.toByteArray();
    } catch (IOException e) {
      throw new SerializeException(e.getMessage());
    }
  }
}
