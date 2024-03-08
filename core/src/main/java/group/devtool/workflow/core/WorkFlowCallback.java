package group.devtool.workflow.core;

public interface WorkFlowCallback {

  class EmptyWorkFlowCallback implements WorkFlowCallback {

    @Override
    public void callback(WorkFlowEvent event, WorkFlowContext context) {
      // do nothing
    }

  }

  void callback(WorkFlowEvent event, WorkFlowContext context);

  enum WorkFlowEvent {

    START, // 流程启动事件

    CREATED, // 节点创建事件

    COMPLETE, // 节点完成事件

    END, // 流程或子流程结束事件

    STOP, // 流程停止事件
  }

}
