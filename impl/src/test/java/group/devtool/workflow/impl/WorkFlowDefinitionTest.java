package group.devtool.workflow.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import group.devtool.workflow.core.exception.WorkFlowDefinitionException;
import org.junit.Assert;
import org.junit.Test;

import group.devtool.workflow.core.WorkFlowContext;
import group.devtool.workflow.core.WorkFlowLinkDefinition;
import group.devtool.workflow.core.WorkFlowNodeDefinition;
import group.devtool.workflow.core.WorkFlowVariable;
import group.devtool.workflow.core.UserWorkFlowNodeDefinition.WorkFlowUserConfig;

public class WorkFlowDefinitionTest extends WorkFlowBeforeTest {

  @Test
  public void testDefinitionParameterError() {
    List<WorkFlowNodeDefinition> nodes = new ArrayList<>();
    List<WorkFlowLinkDefinition> links = new ArrayList<>();
    Assert.assertThrows(WorkFlowDefinitionException.class,
        () -> new WorkFlowDefinitionImpl("test", "test", 1, nodes, links));
  }

  @Test
  public void testLinkDefinitionParameterError() {
    Assert.assertThrows(WorkFlowDefinitionException.class,
        () -> new SPELWorkFlowLinkDefinitionImpl(null, null, null));
  }

  @Test
  public void testNodeDefinitionParameterError() {
    Assert.assertThrows(WorkFlowDefinitionException.class, () -> new StartWorkFlowNodeDefinitionImpl(null, null));
  }

  @Test
  public void testDefinitionStartError() throws WorkFlowDefinitionException {
    List<WorkFlowNodeDefinition> nodes = new ArrayList<>();
    List<WorkFlowLinkDefinition> links = new ArrayList<>();

		nodes.add(new EndWorkFlowNodeDefinitionImpl("end", "end"));
		links.add(new SPELWorkFlowLinkDefinitionImpl("start", "end", ""));

		Assert.assertThrows(WorkFlowDefinitionException.class,
        () -> new WorkFlowDefinitionImpl("test", "test", 1, nodes, links).start());

  }

  @Test
  public void testDefinitionNext() throws WorkFlowDefinitionException {
    List<WorkFlowNodeDefinition> nodes = new ArrayList<>();
    List<WorkFlowLinkDefinition> links = new ArrayList<>();

		// 开始
		nodes.add(new StartWorkFlowNodeDefinitionImpl("start", "start"));

		// 用户节点 1
		WorkFlowUserConfig config = new UserWorkFlowNodeDefinitionImpl.WorkFlowUserConfigImpl(
				Arrays.asList("u1", "u2"), 2);
		nodes.add(new UserWorkFlowNodeDefinitionImpl("u1", "u1", config));
		links.add(new SPELWorkFlowLinkDefinitionImpl("start", "u1", "true"));

		WorkFlowUserConfig config2 = new UserWorkFlowNodeDefinitionImpl.WorkFlowUserConfigImpl(
				Arrays.asList("u1", "u2"), 2);
		nodes.add(new UserWorkFlowNodeDefinitionImpl("u2", "u2", config2));
		links.add(new SPELWorkFlowLinkDefinitionImpl("u1", "u2", "#a > 2"));

		// 结束
		nodes.add(new EndWorkFlowNodeDefinitionImpl("end", "end"));
		links.add(new SPELWorkFlowLinkDefinitionImpl("u2", "end", "true"));
		links.add(new SPELWorkFlowLinkDefinitionImpl("u1", "end", "#a < 2"));
		WorkFlowDefinitionImpl definition = new WorkFlowDefinitionImpl("test", "test", 1, nodes, links);

		List<WorkFlowNodeDefinition> next;
		// 开始 -> 用户节点1
		next = definition.next(definition.start().getCode(), new WorkFlowContext("1"));
		Assert.assertEquals("u1", next.get(0).getCode());

		// 用户节点1 -> 结束
		next = definition.next("u1", new WorkFlowContext("1", WorkFlowVariable.suspend("a", 1)));
		Assert.assertEquals("end", next.get(0).getCode());

		// 用户节点1 -> 用户节点2
		next = definition.next("u1", new WorkFlowContext("1", WorkFlowVariable.suspend("a", 3)));
		Assert.assertEquals("u2", next.get(0).getCode());

	}

}
