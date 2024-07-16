/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.engine.definition;


import group.devtool.workflow.engine.common.JacksonUtils;

import java.io.Serializable;

/**
 * 流程节点定义
 */
public interface WorkFlowNodeDefinition {

  /**
   * @return 节点编码
   */
	String getCode();

  /**
   * @return 节点名称
   */
	String getName();

	/**
	 * @return 节点类型
	 */
	String getType();

	/**
	 * @return 节点配置
	 */
	WorkFlowNodeConfig getConfig();


	default String getConfig(WorkFlowNodeConfig config) {
		return JacksonUtils.serialize(config);
	}

	public interface WorkFlowNodeConfig extends Serializable {

	}

}
