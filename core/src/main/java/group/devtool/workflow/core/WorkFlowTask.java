package group.devtool.workflow.core;

import group.devtool.workflow.core.exception.WorkFlowException;
import group.devtool.workflow.core.exception.SerializeException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * 流程任务接口
 * 一个流程节点对应一个或多个流程任务
 */
public interface WorkFlowTask {

  /**
   * @return 任务唯一ID
   */
  String getTaskId();

  /**
   * @return 流程节点编码
   */
  String getNodeCode();

  /**
   * @return 流程实例ID
   */
  String getInstanceId();

  /**
   * 变更流程任务状态
   *
   * @param context 流程上下文
   * @throws WorkFlowException 流程异常
   */
  void complete(WorkFlowContext context) throws WorkFlowException;

  /**
   * 判断流程任务是否已完成
   *
   * @return 流程任务已完成返回true，相反，返回false
   */
  boolean completed();

  default byte[] getConfig(Serializable target) throws SerializeException {
    try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
         ObjectOutputStream oo = new ObjectOutputStream(bos)) {
      oo.writeObject(target);
      return bos.toByteArray();
    } catch (IOException e) {
      throw new SerializeException(e.getMessage());
    }
  }

  String getRootInstanceId();

  Long getCompleteTime();

  String getCompleteUser();

  String getTaskClass();

  byte[] getTaskConfig();

  enum WorkFlowTaskState {

    DOING,

    DONE

	}

}
