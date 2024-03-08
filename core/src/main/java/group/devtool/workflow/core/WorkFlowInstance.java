package group.devtool.workflow.core;

import group.devtool.workflow.core.exception.WorkFlowException;

import java.util.List;

/**
 * 流程实例
 */
public interface WorkFlowInstance {

  Long getId();

  String instanceId();

  boolean done();

  boolean stopped();

  WorkFlowNode start(Initialize initialize, WorkFlowContext context)
      throws WorkFlowException;

  List<WorkFlowNode> next(Initialize initialize, String nodeCode, WorkFlowContext context)
      throws WorkFlowException;

  void stop();

  String getDefinitionCode();

  Integer getDefinitionVersion();


  enum WorkFlowInstanceState {
    DOING,
    DONE,
    STOP,
	}

  interface Initialize {

    List<WorkFlowNode> init(List<WorkFlowNodeDefinition> definitions,
        String instanceId,
        String rootInstanceId,
        WorkFlowContext context) throws WorkFlowException, WorkFlowException;

  }
}
