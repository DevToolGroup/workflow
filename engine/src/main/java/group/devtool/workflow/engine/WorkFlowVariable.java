/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.engine;

import java.io.Serializable;

/**
 * 流程变量
 */
public class WorkFlowVariable {

	private final String name;

	private final Serializable value;

	protected WorkFlowVariable(String name, Serializable value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public Serializable getValue() {
		return value;
	}

	/**
	 * 构造全局变量
	 *
	 * @param name  变量名
	 * @param value 变量值
	 * @return 全局变量
	 */
	public static GlobalWorkFlowVariable global(String name, Serializable value) {
		return new GlobalWorkFlowVariable(name, value);
	}

	public static class GlobalWorkFlowVariable extends WorkFlowVariable {

		protected GlobalWorkFlowVariable(String name, Serializable value) {
			super(name, value);
		}

	}

}
