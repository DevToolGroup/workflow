package group.devtool.workflow.core;

import group.devtool.workflow.core.WorkFlowTransactionRetry.WorkFlowTransactionOperation;
import group.devtool.workflow.core.exception.WorkFlowException;

/**
 * {@link WorkFlowService} 默认实现
 */
public abstract class AbstractWorkFlowService implements WorkFlowService {

  private WorkFlowTransactionRetry current() {
    return AbstractWorkFlowTransaction.current();
  }

  @Override
  public void save(WorkFlowContext context) throws WorkFlowException {
    current().addOperate(doSave(context));
  }

  protected abstract WorkFlowTransactionOperation doSave(WorkFlowContext context) throws WorkFlowException;

  @Override
  public void save(WorkFlowInstance instance, String rootInstanceId) throws WorkFlowException {
    current().addOperate(doSave(instance, rootInstanceId));
  }

  protected abstract WorkFlowTransactionOperation doSave(WorkFlowInstance instance, String rootInstanceId) throws WorkFlowException;

  @Override
  public void save(WorkFlowNode node, String rootInstanceId) throws WorkFlowException {
    current().addOperate(doSave(node, rootInstanceId));
  }

  protected abstract WorkFlowTransactionOperation doSave(WorkFlowNode node, String rootInstanceId) throws WorkFlowException;

  @Override
  public void changeNodeComplete(WorkFlowNode node) throws WorkFlowException {
    current().addOperate(doChangeNodeComplete(node));
  }

  protected abstract WorkFlowTransactionOperation doChangeNodeComplete(WorkFlowNode node) throws WorkFlowException;

  @Override
  public void changeTaskComplete(WorkFlowTask task) throws WorkFlowException {
    current().addOperate(doChangeTaskComplete(task));
  }

  protected abstract WorkFlowTransactionOperation doChangeTaskComplete(WorkFlowTask task) throws WorkFlowException;
  
}
