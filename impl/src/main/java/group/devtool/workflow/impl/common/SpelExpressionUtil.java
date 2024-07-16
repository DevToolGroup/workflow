/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.impl.common;

import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Map;

public class SpelExpressionUtil {

	public static boolean getValue(String expression, Map<String, Object> variables) {
		SpelExpressionParser parser = new SpelExpressionParser();
		Expression exp = parser.parseExpression(expression);
		StandardEvaluationContext context = new StandardEvaluationContext();

		// 这里用局部变量覆盖全局变量
		context.setVariables(variables);

		try {
			Boolean result = exp.getValue(context, Boolean.class);
			return Boolean.TRUE.equals(result);
		} catch (EvaluationException e) {
			return false;
		}
	}
}
