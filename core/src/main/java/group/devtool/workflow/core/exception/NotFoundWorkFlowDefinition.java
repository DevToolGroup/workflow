package group.devtool.workflow.core.exception;

/**
 * 流程定义不存在
 */
public class NotFoundWorkFlowDefinition extends WorkFlowDefinitionException {

  public NotFoundWorkFlowDefinition(String code, Integer version) {
    super(String.format("流程定义不存在，编码：%s，版本：%s", code, version));
  }

}
