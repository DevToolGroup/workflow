package group.devtool.workflow.core;

import group.devtool.workflow.core.exception.WorkFlowDefinitionException;
import group.devtool.workflow.core.exception.IllegalDefinitionParameter;

/**
 * {@link WorkFlowLinkDefinition} 抽象实现，定义流程连线的基本业务流程
 */
public abstract class AbstractWorkFlowLinkDefinition implements WorkFlowLinkDefinition {

  private final String source;

  private final String target;

  private final String expression;

  public AbstractWorkFlowLinkDefinition(String source, String target, String expression) throws WorkFlowDefinitionException {
    if (null == expression) {
      throw new IllegalDefinitionParameter("表达式不能为空");
    }
    if (null == source || null == target) {
      throw new IllegalDefinitionParameter("开始和结束节点不能为空");
    }
    this.source = source;
    this.target = target;
    this.expression = expression;
  }

  @Override
  public boolean from(String source) {
    return this.source.equals(source);
  }

  @Override
  public String getTarget() {
    return target;
  }

  @Override
  public boolean match(WorkFlowContext context) {
    return parseExpression(expression, context);
  }

  /**
   * 具体表达式如何解析需要子类根据实际情况实现
   * 
   * @param expression 表达式
   * @param context 流转上下文
   * @return 是否满足条件，true满足，false不满足
   */
  protected abstract boolean parseExpression(String expression, WorkFlowContext context);

}
