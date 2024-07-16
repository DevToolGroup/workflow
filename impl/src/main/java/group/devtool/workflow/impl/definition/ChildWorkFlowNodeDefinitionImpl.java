/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.impl.definition;

import group.devtool.workflow.engine.definition.ChildWorkFlowNodeDefinition;
import group.devtool.workflow.engine.definition.WorkFlowDefinition;
import group.devtool.workflow.engine.exception.IllegalWorkFlowDefinition;

import java.util.List;

/**
 * {@link ChildWorkFlowNodeDefinition} 默认实现类
 */
public class ChildWorkFlowNodeDefinitionImpl extends ChildWorkFlowNodeDefinition {

	private final String code;

	private final String name;

	private final List<WorkFlowDefinition> child;

	private final ChildWorkFlowConfig config;

	public ChildWorkFlowNodeDefinitionImpl(String code, String name,
																				 ChildWorkFlowConfig config, List<WorkFlowDefinition> childDefinition) {
		if (null == code || null == name || null == config || null == childDefinition) {
			throw new IllegalWorkFlowDefinition("子流程任务节点编码、名称、配置、子流程定义不能为空");
		}
		if (childDefinition.stream().map(WorkFlowDefinition::getCode).distinct().count() != childDefinition.size()) {
			throw new IllegalWorkFlowDefinition("子流程定义编码不能重复");
		}
		this.code = code;
		this.name = name;
		this.config = config;
		this.child = childDefinition;
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
	public ChildWorkFlowConfig getConfig() {
		return config;
	}

	@Override
	public List<WorkFlowDefinition> getChild() {
		return child;
	}

	public static class ChildWorkFlowConfigImpl implements ChildWorkFlowConfig {

		private List<ChildStartUpImpl> startUp;

		public ChildWorkFlowConfigImpl() {
		}

		public ChildWorkFlowConfigImpl(List<ChildStartUpImpl> startUps) {
			this.startUp = startUps;
		}

		@Override
		public List<? extends ChildStartUp> getStartUp() {
			return startUp;
		}

		public void setStartUp(List<ChildStartUpImpl> startUp) {
			this.startUp = startUp;
		}
	}

	public static class ChildStartUpImpl implements ChildStartUp {

		private String childCode;

		private Integer taskNumber;

		private String expression;

		public ChildStartUpImpl() {
		}

		public ChildStartUpImpl(String childCode, Integer taskNumber, String expression) {
			this.childCode = childCode;
			this.taskNumber = taskNumber;
			this.expression = expression;
		}

		@Override
		public String getChildCode() {
			return childCode;
		}

		@Override
		public Integer getTaskNumber() {
			return taskNumber;
		}

		@Override
		public String getExpression() {
			return expression;
		}

		public void setChildCode(String childCode) {
			this.childCode = childCode;
		}

		public void setTaskNumber(Integer taskNumber) {
			this.taskNumber = taskNumber;
		}

		public void setExpression(String expression) {
			this.expression = expression;
		}
	}

	@Override
	public String getType() {
		return "CHILD";
	}

}
