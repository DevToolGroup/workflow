/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import group.devtool.workflow.engine.common.JacksonUtils;
import group.devtool.workflow.engine.definition.WorkFlowDefinition;
import group.devtool.workflow.engine.definition.WorkFlowLinkDefinition;
import group.devtool.workflow.engine.definition.WorkFlowNodeDefinition;
import group.devtool.workflow.impl.definition.*;
import group.devtool.workflow.impl.definition.UserWorkFlowNodeDefinitionImpl.UserWorkFlowConfigImpl;
import group.devtool.workflow.impl.definition.TaskWorkFlowNodeDefinitionImpl.JavaTaskWorkFlowConfigImpl;
import group.devtool.workflow.impl.definition.ChildWorkFlowNodeDefinitionImpl.ChildWorkFlowConfigImpl;
import group.devtool.workflow.impl.definition.DelayWorkFlowNodeDefinitionImpl.JavaDelayWorkFlowConfigImpl;
import group.devtool.workflow.impl.entity.WorkFlowDefinitionEntity;
import group.devtool.workflow.impl.entity.WorkFlowLinkDefinitionEntity;
import group.devtool.workflow.impl.entity.WorkFlowNodeDefinitionEntity;

/**
 * 流程定义工厂方法
 */
public class WorkFlowDefinitionFactory {

	public static WorkFlowDefinitionFactory DEFINITION = new WorkFlowDefinitionFactory();

	enum LinkFactory {
		SPEL(SPELWorkFlowLinkDefinitionImpl::new);

		private final LinkFunction init;

		LinkFactory(LinkFunction init) {
			this.init = init;
		}

		public WorkFlowLinkDefinition apply(WorkFlowLinkDefinitionEntity entity) {
			return init.apply(entity);
		}
	}

	enum NodeFactory {
		// 开始节点
		START((code, name, config, objs) -> new StartWorkFlowNodeDefinitionImpl(code, name)),

		// 用户节点
		USER((code, name, config, objs) -> new UserWorkFlowNodeDefinitionImpl(code, name,
						JacksonUtils.deserialize(config, UserWorkFlowConfigImpl.class))),

		// 任务节点
		TASK((code, name, config, objs) -> new TaskWorkFlowNodeDefinitionImpl(code, name,
						JacksonUtils.deserialize(config, JavaTaskWorkFlowConfigImpl.class))),

		// 嵌套子流程节点，运行态下子流程定义为空
		CHILD((code, name, config, objs) -> {
			List<WorkFlowDefinition> ds = new ArrayList<>();
			for (Object obj : objs) {
				ds.add((WorkFlowDefinition) obj);
			}
			return new ChildWorkFlowNodeDefinitionImpl(code, name,
							JacksonUtils.deserialize(config, ChildWorkFlowConfigImpl.class),
							ds);
		}),

		// 延时节点
		DELAY((code, name, config, objs) -> new DelayWorkFlowNodeDefinitionImpl(code, name,
						JacksonUtils.deserialize(config, JavaDelayWorkFlowConfigImpl.class))),

		// 结束节点
		END((code, name, config, objs) -> new EndWorkFlowNodeDefinitionImpl(code, name)),
		;

		private final NodeFunction init;

		NodeFactory(NodeFunction init) {
			this.init = init;
		}

		public WorkFlowNodeDefinition apply(String code, String name, String config, Object... others) {
			return init.apply(code, name, config, others);
		}
	}

	public WorkFlowDefinition factory(WorkFlowDefinitionEntity de, Map<String, List<WorkFlowDefinitionEntity>> nem) {
		return new WorkFlowDefinitionImpl(de.getCode(),
						de.getName(),
						de.getRootCode(),
						de.getVersion(),
						nodeFactory(de.getNodes(), nem),
						linkFactory(de.getLinks()));
	}

	private List<WorkFlowLinkDefinition> linkFactory(List<? extends WorkFlowLinkDefinitionEntity> lde) {
		List<WorkFlowLinkDefinition> links = new ArrayList<>(lde.size());
		for (WorkFlowLinkDefinitionEntity entity : lde) {
			links.add(LinkFactory.valueOf(entity.getParser().toUpperCase()).apply(entity));
		}
		return links;
	}

	private List<WorkFlowNodeDefinition> nodeFactory(List<? extends WorkFlowNodeDefinitionEntity> nde,
																									 Map<String, List<WorkFlowDefinitionEntity>> nem) {
		List<WorkFlowNodeDefinition> nodes = new ArrayList<>(nde.size());
		for (WorkFlowNodeDefinitionEntity entity : nde) {
			List<WorkFlowDefinitionEntity> child = nem.get(entity.getCode());
			if (null == child) {
				nodes.add(NodeFactory.valueOf(entity.getType()).apply(entity.getCode(), entity.getName(), entity.getConfig()));
				continue;
			}
			List<WorkFlowDefinition> childDefinitions = new ArrayList<>();
			for (WorkFlowDefinitionEntity c : child) {
				childDefinitions.add(factory(c, nem));
			}
			nodes.add(NodeFactory.valueOf(entity.getType()).apply(entity.getCode(), entity.getName(), entity.getConfig(),
							childDefinitions.toArray()));
		}
		return nodes;
	}

	public interface NodeFunction {

		WorkFlowNodeDefinition apply(String code, String name, String config, Object... others);

	}

	public interface LinkFunction {

		WorkFlowLinkDefinition apply(WorkFlowLinkDefinitionEntity entity);

	}
}
