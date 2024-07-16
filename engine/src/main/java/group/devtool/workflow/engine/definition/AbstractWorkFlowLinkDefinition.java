/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.engine.definition;

import group.devtool.workflow.engine.WorkFlowContextImpl;
import group.devtool.workflow.engine.exception.IllegalWorkFlowDefinition;

/**
 * {@link WorkFlowLinkDefinition} 抽象实现，定义流程连线的基本业务流程
 */
public abstract class AbstractWorkFlowLinkDefinition implements WorkFlowLinkDefinition {

  private final String code;

  private final String source;

  private final String target;

  private final String expression;

  public AbstractWorkFlowLinkDefinition(String code, String source, String target, String expression) throws IllegalWorkFlowDefinition {
    if (null == code || code.trim().isEmpty()) {
      throw new IllegalWorkFlowDefinition("编码不能为空");
    }
    if (null == source || null == target) {
      throw new IllegalWorkFlowDefinition("开始和结束节点不能为空");
    }
    this.code = code;
    this.source = source;
    this.target = target;
    this.expression = expression;
  }

  @Override
  public String getCode() {
    return code;
  }

  @Override
  public String getSource() {
    return source;
  }

  @Override
  public String getTarget() {
    return target;
  }

  @Override
  public boolean match(String sourceCode, WorkFlowContextImpl context) {
    if (!sourceCode.equals(source)) {
      return false;
    }
    if (null == expression) {
      return true;
    }
    return parseExpression(expression, context);
  }

  /**
   * 具体表达式如何解析需要子类根据实际情况实现
   * 
   * @param expression 表达式
   * @param context 流转上下文
   * @return 是否满足条件，true满足，false不满足
   */
  protected abstract boolean parseExpression(String expression, WorkFlowContextImpl context);

}
