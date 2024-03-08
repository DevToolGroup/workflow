package group.devtool.workflow.core;

import group.devtool.workflow.core.exception.WorkFlowException;

/**
 * 流程分步式事务，主要目的在于将一次长事务改为多次短事务，
 * 因此，在出现异常的时候，需要基础服务支持短事务回滚
 */
public interface WorkFlowTransactionRetry {

  String seq();

  /**
   * 开启分步式事务
   * 
   * @return 事务编号
   * @throws WorkFlowException 
   */
	void begin() throws WorkFlowException;

  /**
   * 添加事务操作
   * 
   * @param operation
   */
	void addOperate(WorkFlowTransactionOperation operation);

  /**
   * 事务提交
   * @throws WorkFlowException 
   */
	void commit() throws WorkFlowException;

  /**
   * 事务回滚
   * @throws WorkFlowException 
   */
	void rollback() throws WorkFlowException;

  /**
   * 事务相关资源关闭
   */
	void close();

  interface WorkFlowTransactionOperation {

    Long getId();
    
    String getRootInstanceId();
  
    void cancel() throws WorkFlowException;
  }

  class EmptyWorkFlowTransaction implements WorkFlowTransactionRetry {

    @Override
    public String seq() {
      // do nothing
      return null;
    }

    @Override
    public void begin() {
      // do nothing
    }

    @Override
    public void addOperate(WorkFlowTransactionOperation operation) {
      // do nothing
    }

    @Override
    public void rollback() {
      // do nothing
    }

    @Override
    public void close() {
      // do nothing
    }

    @Override
    public void commit() {
      // do nothing
    }

    
  }

}
