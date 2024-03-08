package group.devtool.workflow.impl;

import java.util.HashMap;
import java.util.Map;

import group.devtool.workflow.core.exception.WorkFlowDefinitionException;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import group.devtool.workflow.core.AbstractWorkFlowLinkDefinition;
import group.devtool.workflow.core.WorkFlowContext;

/**
 * 基于Spring El Expression实现的连接线定义
 */
public class SPELWorkFlowLinkDefinitionImpl extends AbstractWorkFlowLinkDefinition {

  private final String source;

  private final String expressionString;

  public SPELWorkFlowLinkDefinitionImpl(String source, String target, String expression) throws WorkFlowDefinitionException {
    super(source, target, expression);
    this.source = source;
    this.expressionString = expression;
  }

  public SPELWorkFlowLinkDefinitionImpl(WorkFlowLinkDefinitionEntity entity) throws WorkFlowDefinitionException {
    super(entity.getSource(), entity.getTarget(), entity.getExpression());
    this.source = entity.getSource();
    this.expressionString = entity.getExpression();
  }

  @Override
  protected boolean parseExpression(String expression, WorkFlowContext context) {
    SpelExpressionParser parser = new SpelExpressionParser();
    Expression exp = parser.parseExpression(expression);
    StandardEvaluationContext variables = new StandardEvaluationContext();

    Map<String, Object> vm = new HashMap<>();
    vm.putAll(context.instanceVariables());
    vm.putAll(context.nodeVariables().getOrDefault(source, new HashMap<String, Object>()));
    vm.putAll(context.localVariables());
    variables.setVariables(vm);
    
    Boolean result = exp.getValue(variables, Boolean.class);
    return Boolean.TRUE.equals(result);
  }

  public String getParser() {
    return "SPEL";
  }

  public String getSource() {
    return source;
  }

  public String getExpression() {
    return expressionString;
  }
}
