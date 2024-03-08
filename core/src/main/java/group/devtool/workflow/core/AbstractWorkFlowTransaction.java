package group.devtool.workflow.core;

import group.devtool.workflow.core.exception.WorkFlowException;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 流程分步式事务，区别于分布式事务，主要目的在于将一次长事务改为多次短事务，
 * 因此，在出现异常的时候，需要基础服务支持短事务回滚
 */
public abstract class AbstractWorkFlowTransaction implements WorkFlowTransactionRetry {

  private static final ThreadLocal<WorkFlowTransactionRetry> current = new ThreadLocal<>();

  private final ThreadLocal<List<WorkFlowTransactionOperation>> operations = new ThreadLocal<>();

  private String rootInstanceId;

  public AbstractWorkFlowTransaction() {

  }

  protected boolean inTransaction() {
    return Optional.ofNullable(current.get()).isPresent();
  }


  @Override
  public void begin() throws WorkFlowException {
    current.set(this);
    operations.set(new CopyOnWriteArrayList<>());
    doBegin();
  }

  protected abstract void doBegin() throws WorkFlowException;


  @Override
  public void addOperate(WorkFlowTransactionOperation operation) {
    if (inTransaction()) {
      operations.get().add(operation);
      this.rootInstanceId = operation.getRootInstanceId();
    }
  }

  public List<WorkFlowTransactionOperation> operations() {
    return operations.get();
  }

  @Override
  public void rollback() throws WorkFlowException {
    if (!inTransaction()) {
      return;
    }
    beforeRollback();
    List<WorkFlowTransactionOperation> actions = operations.get();
    for (int i = actions.size() - 1; i > -1; i--) {
      WorkFlowTransactionOperation action = actions.get(i);
      action.cancel();
    }
    afterRollback();
  }

  protected abstract void afterRollback();

  protected abstract void beforeRollback();

  @Override
  public void close() {
    current.remove();
    operations.remove();
  }

  public String getRootInstanceId() {
    return rootInstanceId;
  }

  public static WorkFlowTransactionRetry current() {
    return AbstractWorkFlowTransaction.current.get();
  }

}
