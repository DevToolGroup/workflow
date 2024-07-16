/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.engine.definition;

import group.devtool.workflow.engine.WorkFlowContextImpl;
import group.devtool.workflow.engine.exception.IllegalWorkFlowDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 流程定义抽象类主要实现流程流转，以及规范流程流转过程中需要子类提供的能力
 */
public abstract class AbstractWorkFlowDefinition implements WorkFlowDefinition {

	/**
	 * @return 流程节点定义
	 */
	protected abstract List<WorkFlowNodeDefinition> getNodes();

	/**
	 * @return 流程节点间连线定义
	 */
	protected abstract List<WorkFlowLinkDefinition> getLinks();

	/**
	 * 流程节点流转，这里规定了流程节点间连线上需要提供条件表达式，帮忙完成后续节点的计算
	 */
	@Override
	public List<WorkFlowNodeDefinition> next(String nodeCode, WorkFlowContextImpl context) {
		List<String> nodeCodes = new ArrayList<>();
		for (WorkFlowLinkDefinition link : getLinks()) {
			if (!link.match(nodeCode, context)) {
				continue;
			}
			nodeCodes.add(link.getTarget());
		}
		if (nodeCodes.size() > 1) {
			throw new IllegalWorkFlowDefinition("发现多个符合条件的节点，当前ji");
		}
		return getNodes().stream().filter(i -> nodeCodes.contains(i.getCode())).collect(Collectors.toList());
	}
}
