package group.devtool.workflow.core;

import group.devtool.workflow.core.exception.WorkFlowException;

import java.io.Closeable;


public interface WorkFlowScheduler extends Closeable {

  boolean ready();

  void addTask(DelayItem item) throws WorkFlowException;

  /**
   * 延时事项接口
   */
  interface DelayItem {

    /**
     * @return 延时时间
     */
    Long getDelay() ;

    /**
     * @throws WorkFlowException
     */
    void run() throws WorkFlowException;
  }

}
