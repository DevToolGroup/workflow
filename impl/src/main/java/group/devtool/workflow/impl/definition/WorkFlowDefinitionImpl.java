/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.impl.definition;

import java.util.*;

import group.devtool.workflow.engine.definition.*;
import group.devtool.workflow.engine.exception.IllegalWorkFlowDefinition;

/**
 * {@link AbstractWorkFlowDefinition} 默认实现
 */
public class WorkFlowDefinitionImpl extends AbstractWorkFlowDefinition {

	/**
	 * 流程定义编码
	 */
	private final String code;

	private final String name;

	private final Integer version;

	private final String rootCode;

	/**
	 * 流程节点定义列表
	 */
	private final List<WorkFlowNodeDefinition> nodes;

	/**
	 * 流程连线定义列表
	 */
	private final List<WorkFlowLinkDefinition> links;

	private WorkFlowNodeDefinition start;

	private WorkFlowNodeDefinition end;

	public WorkFlowDefinitionImpl(String code, String name, String rootCode,
																List<WorkFlowNodeDefinition> nodes,
																List<WorkFlowLinkDefinition> links) {
		this(code, name, rootCode,1, nodes, links);
	}

	public WorkFlowDefinitionImpl(String code, String name, String rootCode, Integer version,
																List<WorkFlowNodeDefinition> nodes,
																List<WorkFlowLinkDefinition> links) {
		validate(code, nodes, links);
		this.code = code;
		this.name = name;
		this.version = version;
		this.rootCode = rootCode;
		this.nodes = nodes;
		this.links = links;

	}

	private void validate(String code, List<WorkFlowNodeDefinition> nodes, List<WorkFlowLinkDefinition> links) {
		if (null == nodes || nodes.isEmpty() || null == links || links.isEmpty()) {
			throw new IllegalWorkFlowDefinition("参数错误，nodes，links不能为空");
		}
		// 校验开始/结束节点
		for (WorkFlowNodeDefinition node : nodes) {
			if (node instanceof StartWorkFlowNodeDefinition) {
				if (null == start) {
					this.start = node;
				} else {
					throw new IllegalWorkFlowDefinition("仅支持一个开始节点，节点编码：" + node.getCode());
				}
			} else if (node instanceof EndWorkFlowNodeDefinition) {
				if (null == end) {
					this.end = node;
				} else {
					throw new IllegalWorkFlowDefinition("仅支持一个结束节点");
				}
			}
		}
		if (null == start || null == end) {
			throw new IllegalWorkFlowDefinition("开始节点/结束节点不能为空");
		}
		validCodeUnique(code, nodes, new HashSet<>());
	}

	private boolean validCodeUnique(String definitionCode, List<WorkFlowNodeDefinition> nodes, Set<String> codes) throws IllegalWorkFlowDefinition {
		if (codes.contains(definitionCode)) {
			throw new IllegalWorkFlowDefinition("编码已存在" + definitionCode);
		}
		codes.add(definitionCode);
		for (WorkFlowNodeDefinition node : nodes) {
			if (codes.contains(node.getCode())) {
				throw new IllegalWorkFlowDefinition("节点编码已存在" + node.getCode());
			}
			codes.add(node.getCode());
			if (node instanceof ChildWorkFlowNodeDefinition) {
				ChildWorkFlowNodeDefinition child = (ChildWorkFlowNodeDefinitionImpl) node;
				List<WorkFlowDefinition> childDefinitions = child.getChild();
				for (WorkFlowDefinition definition: childDefinitions) {
					WorkFlowDefinitionImpl childDefinitionImpl = (WorkFlowDefinitionImpl)definition;
					validCodeUnique(childDefinitionImpl.getCode(), childDefinitionImpl.getNodes(), new HashSet<>());
				}
			}
		}
		return true;
	}

	@Override
	public String getCode() {
		return code;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getRootCode() {
		return rootCode;
	}

	@Override
	public List<WorkFlowNodeDefinition> getNodes() {
		return nodes;
	}

	public List<WorkFlowLinkDefinition> getLinks() {
		return links;
	}

	@Override
	public Integer getVersion() {
		return version;
	}

	@Override
	public WorkFlowNodeDefinition getStartNode() {
		return start;
	}

	@Override
	public WorkFlowNodeDefinition getEndNode() {
		return end;
	}
}
