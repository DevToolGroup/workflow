/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.engine;

/**
 * 流程回调服务
 */
public interface WorkFlowCallback {

  class EmptyWorkFlowCallback implements WorkFlowCallback {

    @Override
    public void doCallback(WorkFlowEvent event, WorkFlowContext context) {
      // do nothing
    }

  }

  default void callback(WorkFlowEvent event, WorkFlowContext context) {
    try {
      doCallback(event, context);
    } catch (Exception e) {
      // 忽略异常，后续增加日志
    }
  }

  void doCallback(WorkFlowEvent event, WorkFlowContext context);

  enum WorkFlowEvent {

    START, // 流程启动事件

    CREATED, // 节点创建事件

    COMPLETE, // 节点完成事件

    END, // 流程或子流程结束事件

    STOP,  // 流程停止事件

    EXCEPTION,
  }

}
