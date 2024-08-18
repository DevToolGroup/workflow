/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import group.devtool.workflow.engine.WorkFlowContext;
import group.devtool.workflow.impl.definition.*;
import group.devtool.workflow.impl.runtime.ParentWorkFlowInstanceImpl;
import org.junit.Assert;
import org.junit.Test;

import group.devtool.workflow.engine.definition.UserWorkFlowNodeDefinition.UserWorkFlowConfig;
import group.devtool.workflow.engine.WorkFlowContextImpl;
import group.devtool.workflow.engine.definition.WorkFlowDefinition;
import group.devtool.workflow.engine.definition.WorkFlowLinkDefinition;
import group.devtool.workflow.engine.runtime.WorkFlowNode;
import group.devtool.workflow.engine.definition.WorkFlowNodeDefinition;
import group.devtool.workflow.engine.WorkFlowVariable;
import group.devtool.workflow.impl.definition.ChildWorkFlowNodeDefinitionImpl.ChildWorkFlowConfigImpl;
import group.devtool.workflow.impl.definition.UserWorkFlowNodeDefinitionImpl.UserWorkFlowConfigImpl;

public class WorkFlowInstanceTest extends InitWorkFlowConfig {

	@Test
	public void testInstance() {

		WorkFlowContextImpl context = new WorkFlowContextImpl("simple_instance_run");
		context.addRuntimeVariable(WorkFlowVariable.global(WorkFlowContext.USER, "admin"));

		WorkFlowDefinition definition = getDefinition();
		ParentWorkFlowInstanceImpl parent = new ParentWorkFlowInstanceImpl("test", definition);

		// 启动
		WorkFlowNode start = parent.start((ndfs, id, rootId, ctx) -> {
			List<WorkFlowNode> nodes = new ArrayList<>();
			for (WorkFlowNodeDefinition ndf : ndfs) {
				nodes.add(dbConfig.service().getNode(ndf, id, rootId, ctx));
			}
			return nodes;
		}, context);

		Assert.assertEquals("start", start.getNodeCode());

		// start -> u1
		List<WorkFlowNode> u1 = parent.next((ndfs, instanceId, rootId, ctx) -> {
			List<WorkFlowNode> ns = new ArrayList<>();
			for (WorkFlowNodeDefinition ndf : ndfs) {
				ns.add(dbConfig.service().getNode(ndf, instanceId, rootId, ctx));
			}
			return ns;
		}, start.getNodeCode(), context);

		Assert.assertEquals(1, u1.size());
		Assert.assertEquals("u1", u1.get(0).getNodeCode());

		List<WorkFlowNode> u2 = parent.next((ndfs, instanceId, rootId, ctx) -> {
			List<WorkFlowNode> ns = new ArrayList<>();
			for (WorkFlowNodeDefinition ndf : ndfs) {
				ns.add(dbConfig.service().getNode(ndf, instanceId, rootId, ctx));
			}
			return ns;
		}, u1.get(0).getNodeCode(), context);
		Assert.assertEquals(0, u2.size());

		context.addRuntimeVariable(WorkFlowVariable.global("a", 3));
		u2 = parent.next((ndfs, instanceId, rootId, ctx) -> {
			List<WorkFlowNode> ns = new ArrayList<>();
			for (WorkFlowNodeDefinition ndf : ndfs) {
				ns.add(dbConfig.service().getNode(ndf, instanceId, rootId, ctx));
			}
			return ns;
		}, u1.get(0).getNodeCode(), context);
		Assert.assertEquals(1, u2.size());
		Assert.assertEquals("u2", u2.get(0).getNodeCode());

		// u1 -> end
		context.addRuntimeVariable(WorkFlowVariable.global("a", 1));
		List<WorkFlowNode> end = parent.next((ndfs, instanceId, rootId, ctx) -> {
			List<WorkFlowNode> ns = new ArrayList<>();
			for (WorkFlowNodeDefinition ndf : ndfs) {
				ns.add(dbConfig.service().getNode(ndf, instanceId, rootId, ctx));
			}
			return ns;
		}, u1.get(0).getNodeCode(), context);
		Assert.assertEquals(0, end.size());
		Assert.assertTrue(parent.done());

	}

	private WorkFlowDefinitionImpl getDefinition() {
		List<WorkFlowNodeDefinition> nodes = new ArrayList<>();
		List<WorkFlowLinkDefinition> links = new ArrayList<>();
		// 开始
		start(nodes);

		// 用户节点 1
		u1(nodes, links);

		// 用户节点 2
		u2(nodes, links);

		// 子流程节点
		child(nodes, links);

		// 结束
		end(nodes, links);

		return new WorkFlowDefinitionImpl("simple_instance_definition", "simple_instance_definition", "simple_instance_definition", nodes, links);
	}

	private void end(List<WorkFlowNodeDefinition> nodes, List<WorkFlowLinkDefinition> links) {
		nodes.add(new EndWorkFlowNodeDefinitionImpl("end", "end"));
		links.add(new SPELWorkFlowLinkDefinitionImpl("l4", "child_def", "end", "#a > 3"));
		links.add(new SPELWorkFlowLinkDefinitionImpl("l5", "u1", "end", "#a < 2"));
	}

	private void child(List<WorkFlowNodeDefinition> nodes, List<WorkFlowLinkDefinition> links) {
		List<WorkFlowLinkDefinition> clinks = new ArrayList<>();
		List<WorkFlowNodeDefinition> cnodes = new ArrayList<>();

		cnodes.add(new StartWorkFlowNodeDefinitionImpl("child_start", "child_start"));
		cnodes.add(new UserWorkFlowNodeDefinitionImpl("child_u1", "child_u1", new UserWorkFlowConfigImpl(Arrays.asList("child_u1"), 1)));
		cnodes.add(new EndWorkFlowNodeDefinitionImpl("child_end", "child_end"));

		clinks.add(new SPELWorkFlowLinkDefinitionImpl("cl1", "child_start", "child_u1", "true"));
		clinks.add(new SPELWorkFlowLinkDefinitionImpl("cl2", "child_u1", "child_end", "true"));
		WorkFlowDefinitionImpl childDef = new WorkFlowDefinitionImpl("child_def", "child_def", "simple_instance_definition", cnodes, clinks);
		ChildWorkFlowConfigImpl childConfig = new ChildWorkFlowConfigImpl(Arrays.asList(new ChildWorkFlowNodeDefinitionImpl.ChildStartUpImpl(childDef.getCode(), 1, null)));
		ChildWorkFlowNodeDefinitionImpl child = new ChildWorkFlowNodeDefinitionImpl("child", "child", childConfig,
						Arrays.asList(childDef));
		nodes.add(child);
		links.add(new SPELWorkFlowLinkDefinitionImpl("l3", "u2", "child", "true"));
	}

	private void u2(List<WorkFlowNodeDefinition> nodes, List<WorkFlowLinkDefinition> links) {
		UserWorkFlowConfig config2 = new UserWorkFlowConfigImpl(
						Arrays.asList("u1", "u2"), 2);
		nodes.add(new UserWorkFlowNodeDefinitionImpl("u2", "u2", config2));
		links.add(new SPELWorkFlowLinkDefinitionImpl("l2", "u1", "u2", "#a > 2"));
	}

	private void u1(List<WorkFlowNodeDefinition> nodes, List<WorkFlowLinkDefinition> links) {
		UserWorkFlowConfig config = new UserWorkFlowConfigImpl(
						Arrays.asList("u1", "u2"), 2);
		nodes.add(new UserWorkFlowNodeDefinitionImpl("u1", "u1", config));
		links.add(new SPELWorkFlowLinkDefinitionImpl("l1", "start", "u1", "true"));
	}

	private void start(List<WorkFlowNodeDefinition> nodes) {
		nodes.add(new StartWorkFlowNodeDefinitionImpl("start", "start"));
	}
}
