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

import group.devtool.workflow.engine.exception.IllegalWorkFlowDefinition;
import group.devtool.workflow.impl.definition.*;
import group.devtool.workflow.impl.definition.UserWorkFlowNodeDefinitionImpl.UserWorkFlowConfigImpl;
import org.junit.Assert;
import org.junit.Test;

import group.devtool.workflow.engine.WorkFlowContextImpl;
import group.devtool.workflow.engine.definition.WorkFlowLinkDefinition;
import group.devtool.workflow.engine.definition.WorkFlowNodeDefinition;
import group.devtool.workflow.engine.WorkFlowVariable;
import group.devtool.workflow.engine.definition.UserWorkFlowNodeDefinition.UserWorkFlowConfig;

public class WorkFlowDefinitionTest extends InitWorkFlowConfig {

  @Test
  public void testDefinitionParameterError() {
    List<WorkFlowNodeDefinition> nodes = new ArrayList<>();
    List<WorkFlowLinkDefinition> links = new ArrayList<>();
    Assert.assertThrows(IllegalWorkFlowDefinition.class,
        () -> new WorkFlowDefinitionImpl("test", "test", "test",nodes, links));
  }

  @Test
  public void testLinkDefinitionParameterError() {
    Assert.assertThrows(IllegalWorkFlowDefinition.class,
        () -> new SPELWorkFlowLinkDefinitionImpl(null, null, null, null));
  }

  @Test
  public void testNodeDefinitionParameterError() {
    Assert.assertThrows(IllegalWorkFlowDefinition.class, () -> new StartWorkFlowNodeDefinitionImpl(null, null));
  }


	@Test
	public void testDefinitionNodeUniqueError() {
		List<WorkFlowLinkDefinition> clinks = new ArrayList<>();
		List<WorkFlowNodeDefinition> cnodes = new ArrayList<>();
		cnodes.add(new StartWorkFlowNodeDefinitionImpl("start1", "start"));
		cnodes.add(new EndWorkFlowNodeDefinitionImpl("end1", "end"));
		cnodes.add(new UserWorkFlowNodeDefinitionImpl("user1", "user", new UserWorkFlowConfigImpl(Arrays.asList("u1"), 1)));
		cnodes.add(new UserWorkFlowNodeDefinitionImpl("user1", "user", new UserWorkFlowConfigImpl(Arrays.asList("u1"), 1)));
		clinks.add(new SPELWorkFlowLinkDefinitionImpl("cl1","start1", "end1", ""));

		Assert.assertThrows(IllegalWorkFlowDefinition.class,
						() -> new WorkFlowDefinitionImpl("user", "child", "test", cnodes, clinks));
	}

	@Test
	public void testDefinitionStartError() {
		List<WorkFlowLinkDefinition> clinks = new ArrayList<>();
		List<WorkFlowNodeDefinition> cnodes = new ArrayList<>();
		cnodes.add(new EndWorkFlowNodeDefinitionImpl("end1", "end"));
		cnodes.add(new UserWorkFlowNodeDefinitionImpl("user1", "user", new UserWorkFlowConfigImpl(Arrays.asList("u1"), 1)));
		cnodes.add(new UserWorkFlowNodeDefinitionImpl("user1", "user", new UserWorkFlowConfigImpl(Arrays.asList("u1"), 1)));
		clinks.add(new SPELWorkFlowLinkDefinitionImpl("cl1","start1", "end1", ""));

		Assert.assertThrows(IllegalWorkFlowDefinition.class,
						() -> new WorkFlowDefinitionImpl("user", "child", "test", cnodes, clinks));
	}

	@Test
	public void testDefinitionEndError() {
		List<WorkFlowLinkDefinition> clinks = new ArrayList<>();
		List<WorkFlowNodeDefinition> cnodes = new ArrayList<>();
		cnodes.add(new StartWorkFlowNodeDefinitionImpl("start1", "start"));
		cnodes.add(new UserWorkFlowNodeDefinitionImpl("user1", "user", new UserWorkFlowConfigImpl(Arrays.asList("u1"), 1)));
		cnodes.add(new UserWorkFlowNodeDefinitionImpl("user1", "user", new UserWorkFlowConfigImpl(Arrays.asList("u1"), 1)));
		clinks.add(new SPELWorkFlowLinkDefinitionImpl("cl1","start1", "end1", ""));

		Assert.assertThrows(IllegalWorkFlowDefinition.class,
						() -> new WorkFlowDefinitionImpl("user", "child", "test", cnodes, clinks));
	}

  @Test
  public void testDefinitionNext() {
    List<WorkFlowNodeDefinition> nodes = new ArrayList<>();
    List<WorkFlowLinkDefinition> links = new ArrayList<>();

		// 开始
		nodes.add(new StartWorkFlowNodeDefinitionImpl("start", "start"));

		// 用户节点 1
		UserWorkFlowConfig config = new UserWorkFlowConfigImpl(
				Arrays.asList("u1", "u2"), 2);
		nodes.add(new UserWorkFlowNodeDefinitionImpl("u1", "u1", config));
		links.add(new SPELWorkFlowLinkDefinitionImpl("l1","start", "u1", "true"));

		UserWorkFlowConfig config2 = new UserWorkFlowConfigImpl(
				Arrays.asList("u1", "u2"), 2);
		nodes.add(new UserWorkFlowNodeDefinitionImpl("u2", "u2", config2));
		links.add(new SPELWorkFlowLinkDefinitionImpl("l2","u1", "u2", "#a > 2"));

		// 结束
		nodes.add(new EndWorkFlowNodeDefinitionImpl("end", "end"));
		links.add(new SPELWorkFlowLinkDefinitionImpl("l3","u2", "end", "true"));
		links.add(new SPELWorkFlowLinkDefinitionImpl("l4","u1", "end", "#a < 2"));
		WorkFlowDefinitionImpl definition = new WorkFlowDefinitionImpl("test", "test", "test",  nodes, links);

		List<WorkFlowNodeDefinition> next;
		// 开始 -> 用户节点1
		next = definition.next(definition.getStartNode().getCode(), new WorkFlowContextImpl("workflow_next_test_case"));
		Assert.assertEquals("u1", next.get(0).getCode());

		// 用户节点1 -> 结束
		next = definition.next("u1", new WorkFlowContextImpl("workflow_next_test_case", WorkFlowVariable.global("a", 1)));
		Assert.assertEquals("end", next.get(0).getCode());

		// 用户节点1 -> 用户节点2
		next = definition.next("u1", new WorkFlowContextImpl("workflow_next_test_case", WorkFlowVariable.global("a", 3)));
		Assert.assertEquals("u2", next.get(0).getCode());

	}

}
