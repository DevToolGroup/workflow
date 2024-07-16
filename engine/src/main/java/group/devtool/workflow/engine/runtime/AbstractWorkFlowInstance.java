/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.engine.runtime;

import group.devtool.workflow.engine.WorkFlowContextImpl;
import group.devtool.workflow.engine.common.InstanceState;
import group.devtool.workflow.engine.definition.EndWorkFlowNodeDefinition;
import group.devtool.workflow.engine.definition.WorkFlowDefinition;
import group.devtool.workflow.engine.definition.WorkFlowNodeDefinition;
import group.devtool.workflow.engine.exception.NextWorkFlowException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 流程实例
 */
public abstract class AbstractWorkFlowInstance implements WorkFlowInstance {

	/**
	 * 流程实例ID
	 */
	private final String id;

	/**
	 * 流程状态
	 */
	protected InstanceState state;

	/**
	 * 流程定义
	 */
	private final WorkFlowDefinition definition;

	public AbstractWorkFlowInstance(String id, WorkFlowDefinition definition) {
		this(id, definition, InstanceState.DOING);
	}

	public AbstractWorkFlowInstance(String id, WorkFlowDefinition definition, InstanceState state) {
		this.id = id;
		this.definition = definition;
		this.state = state;
	}

	@Override
	public String getInstanceId() {
		return id;
	}

	@Override
	public boolean done() {
		return InstanceState.DONE == state;
	}

	@Override
	public WorkFlowNode start(InitNode factory, WorkFlowContextImpl context) {
		List<WorkFlowNodeDefinition> codes = Collections.singletonList(definition.getStartNode());
		List<WorkFlowNode> nodes = initWorkFlowNode(factory, codes, context);
		state = InstanceState.DOING;
		return nodes.get(0);
	}

	public abstract List<WorkFlowNode> initWorkFlowNode(InitNode factory, List<WorkFlowNodeDefinition> nodes,
																											WorkFlowContextImpl context);

	@Override
	public List<WorkFlowNode> next(InitNode factory, String nodeCode, WorkFlowContextImpl context) {
		List<WorkFlowNodeDefinition> nodes = definition.next(nodeCode, context);
		if (nodes.isEmpty()) {
			throw new NextWorkFlowException("流转的目标节点为空");
		}
		if (isEnd(nodes)) {
			state = InstanceState.DONE;
			return new ArrayList<>();
		}

		return initWorkFlowNode(factory, nodes, context);
	}

	private boolean isEnd(List<WorkFlowNodeDefinition> nodes) {
		boolean isEnd = nodes.stream().anyMatch(i -> i instanceof EndWorkFlowNodeDefinition);
		if (isEnd && nodes.size() == 1) {
			return true;
		} else if (isEnd) {
			throw new NextWorkFlowException("在当前条件下，流程已流转至结束节点，但是存在满足当前条件的其他节点");
		} else {
			return false;
		}
	}

	@Override
	public String getDefinitionCode() {
		return definition.getCode();
	}

	@Override
	public Integer getDefinitionVersion() {
		return definition.getVersion();
	}

	@Override
	public String getRootDefinitionCode() {
		return definition.getRootCode();
	}

}
