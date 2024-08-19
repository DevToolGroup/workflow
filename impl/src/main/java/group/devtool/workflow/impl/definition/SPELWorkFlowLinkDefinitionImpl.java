/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.impl.definition;

import group.devtool.workflow.impl.common.SpelExpressionUtil;
import group.devtool.workflow.impl.entity.WorkFlowLinkDefinitionEntity;

import group.devtool.workflow.engine.definition.AbstractWorkFlowLinkDefinition;
import group.devtool.workflow.engine.WorkFlowContextImpl;

import java.util.HashMap;
import java.util.Map;

/**
 * 基于Spring El Expression实现的连接线定义
 */
public class SPELWorkFlowLinkDefinitionImpl extends AbstractWorkFlowLinkDefinition {

    private final String source;

    private final String expressionString;

    public SPELWorkFlowLinkDefinitionImpl(String code, String source, String target) {
        this(code, source, target, null);
    }

    public SPELWorkFlowLinkDefinitionImpl(String code, String source, String target, String expression) {
        super(code, source, target, expression);
        this.source = source;
        this.expressionString = expression;
    }

    public SPELWorkFlowLinkDefinitionImpl(WorkFlowLinkDefinitionEntity entity) {
        super(entity.getCode(), entity.getSource(), entity.getTarget(), entity.getExpression());
        this.source = entity.getSource();
        this.expressionString = entity.getExpression();
    }

    @Override
    protected boolean parseExpression(String expression, WorkFlowContextImpl context) {
        Map<String, Object> variables = new HashMap<>(context.getVariableMap());
        return SpelExpressionUtil.getValue(expression, variables);
    }

    @Override
    public String getSource() {
        return source;
    }

    public String getParser() {
        return "SPEL";
    }

    public String getExpression() {
        return expressionString;
    }
}
