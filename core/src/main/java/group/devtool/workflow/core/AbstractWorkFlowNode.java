package group.devtool.workflow.core;

import group.devtool.workflow.core.exception.WorkFlowException;

/**
 * {@link WorkFlowNode} 抽象实现，定义流程节点的基本业务逻辑。
 */
public abstract class AbstractWorkFlowNode implements WorkFlowNode {

  private WorkFlowTask[] tasks;

  private final String code;

  private TaskSupplier taskSupplier;

  public AbstractWorkFlowNode(WorkFlowNodeDefinition definition, WorkFlowContext context) throws WorkFlowException {
    this.code = definition.getCode();
    this.taskSupplier = () -> initTask(definition, context);
  }

  public AbstractWorkFlowNode(String code, WorkFlowTask... tasks) throws WorkFlowException {
    this.code = code;
    this.tasks = tasks;
  }

  @Override
  public String getCode() {
    return code;
  }

  @Override
  public WorkFlowTask[] getTasks() throws WorkFlowException {
    if (null != taskSupplier) {
      tasks = taskSupplier.get();
      taskSupplier = null;
    }
    return tasks;
  }

  /**
   * 初始化流程任务
   *
   * @param definition 流程节点定义
   * @param context 流程上下文
   * @return 流程任务数组
   * @throws WorkFlowException 流程异常
   */
  protected abstract WorkFlowTask[] initTask(WorkFlowNodeDefinition definition, WorkFlowContext context)
      throws WorkFlowException;

  /**
   * 流程任务初始化接口
   */
  public interface TaskSupplier {
  
    WorkFlowTask[] get() throws WorkFlowException;

  }
}
