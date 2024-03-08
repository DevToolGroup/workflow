package group.devtool.workflow.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import group.devtool.workflow.core.exception.WorkFlowDefinitionException;
import group.devtool.workflow.core.exception.WorkFlowException;
import org.junit.Assert;
import org.junit.Test;

import group.devtool.workflow.core.UserWorkFlowNodeDefinition.WorkFlowUserConfig;
import group.devtool.workflow.core.WorkFlowContext;
import group.devtool.workflow.core.WorkFlowDefinition;
import group.devtool.workflow.core.WorkFlowLinkDefinition;
import group.devtool.workflow.core.WorkFlowNode;
import group.devtool.workflow.core.WorkFlowNodeDefinition;
import group.devtool.workflow.core.WorkFlowVariable;
import group.devtool.workflow.impl.ChildWorkFlowNodeDefinitionImpl.MybatisWorkFlowChildConfig;
import group.devtool.workflow.impl.UserWorkFlowNodeDefinitionImpl.WorkFlowUserConfigImpl;

public class WorkFlowInstanceTest extends WorkFlowBeforeTest {

  @Test
  public void testInstance() throws WorkFlowException {

    WorkFlowContext context = new WorkFlowContext("test");
    context.localVariable(WorkFlowVariable.suspend(WorkFlowVariable.USER, "admin"));

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

		Assert.assertEquals("start", start.getCode());
		// start -> u1
		List<WorkFlowNode> u1 = parent.next((ndfs, instanceId, rootId, ctx) -> {
			List<WorkFlowNode> ns = new ArrayList<>();
			for (WorkFlowNodeDefinition ndf : ndfs) {
				ns.add(dbConfig.service().getNode(ndf, instanceId, rootId, ctx));
			}
			return ns;
		}, start.getCode(), context);

		Assert.assertEquals(1, u1.size());
		Assert.assertEquals("u1", u1.get(0).getCode());

		List<WorkFlowNode> u2 = parent.next((ndfs, instanceId, rootId, ctx) -> {
			List<WorkFlowNode> ns = new ArrayList<>();
			for (WorkFlowNodeDefinition ndf : ndfs) {
				ns.add(dbConfig.service().getNode(ndf, instanceId, rootId, ctx));
			}
			return ns;
		}, u1.get(0).getCode(), context);
		Assert.assertEquals(0, u2.size());

		context.localVariable(WorkFlowVariable.suspend("a", 3));
		u2 = parent.next((ndfs, instanceId, rootId, ctx) -> {
			List<WorkFlowNode> ns = new ArrayList<>();
			for (WorkFlowNodeDefinition ndf : ndfs) {
				ns.add(dbConfig.service().getNode(ndf, instanceId, rootId, ctx));
			}
			return ns;
		}, u1.get(0).getCode(), context);
		Assert.assertEquals(1, u2.size());
		Assert.assertEquals("u2", u2.get(0).getCode());

		// u1 -> end
		context.localVariable(WorkFlowVariable.suspend("a", 1));
		List<WorkFlowNode> end = parent.next((ndfs, instanceId, rootId, ctx) -> {
			List<WorkFlowNode> ns = new ArrayList<>();
			for (WorkFlowNodeDefinition ndf : ndfs) {
				ns.add(dbConfig.service().getNode(ndf, instanceId, rootId, ctx));
			}
			return ns;
		}, u1.get(0).getCode(), context);
		Assert.assertEquals(0, end.size());
		Assert.assertTrue(parent.done());

	}

  private WorkFlowDefinitionImpl getDefinition()
      throws WorkFlowDefinitionException {
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

    WorkFlowDefinitionImpl definition = new WorkFlowDefinitionImpl("test", "test", 1, nodes, links);
    return definition;
  }

  private void end(List<WorkFlowNodeDefinition> nodes, List<WorkFlowLinkDefinition> links)
      throws WorkFlowDefinitionException {
    nodes.add(new EndWorkFlowNodeDefinitionImpl("end", "end"));
    links.add(new SPELWorkFlowLinkDefinitionImpl("cc", "end", "#a > 3"));
    links.add(new SPELWorkFlowLinkDefinitionImpl("u1", "end", "#a < 2"));
  }

  private void child(List<WorkFlowNodeDefinition> nodes, List<WorkFlowLinkDefinition> links)
      throws WorkFlowDefinitionException {
    List<WorkFlowLinkDefinition> clinks = new ArrayList<>();
    List<WorkFlowNodeDefinition> cnodes = new ArrayList<>();

    cnodes.add(new StartWorkFlowNodeDefinitionImpl("start1", "start1"));
    cnodes.add(
        new UserWorkFlowNodeDefinitionImpl("cu1", "cu1", new WorkFlowUserConfigImpl(List.of("cu1"), 1)));
    cnodes.add(new EndWorkFlowNodeDefinitionImpl("end1", "end1"));

    clinks.add(new SPELWorkFlowLinkDefinitionImpl("start1", "cu1", "true"));
    clinks.add(new SPELWorkFlowLinkDefinitionImpl("cu1", "end1", "true"));
    WorkFlowDefinitionImpl childDef = new WorkFlowDefinitionImpl("cc", "cc", 1, cnodes, clinks);
    MybatisWorkFlowChildConfig config3 = new MybatisWorkFlowChildConfig(1, childDef.code());
    ChildWorkFlowNodeDefinitionImpl child = new ChildWorkFlowNodeDefinitionImpl("child", "child", config3,
        childDef);
    nodes.add(child);
    links.add(new SPELWorkFlowLinkDefinitionImpl("u2", "child", "true"));
  }

  private void u2(List<WorkFlowNodeDefinition> nodes, List<WorkFlowLinkDefinition> links)
      throws WorkFlowDefinitionException {
    WorkFlowUserConfig config2 = new WorkFlowUserConfigImpl(
        Arrays.asList("u1", "u2"), 2);
    nodes.add(new UserWorkFlowNodeDefinitionImpl("u2", "u2", config2));
    links.add(new SPELWorkFlowLinkDefinitionImpl("u1", "u2", "#a > 2"));
  }

  private void u1(List<WorkFlowNodeDefinition> nodes, List<WorkFlowLinkDefinition> links)
      throws WorkFlowDefinitionException {
    WorkFlowUserConfig config = new WorkFlowUserConfigImpl(
        Arrays.asList("u1", "u2"), 2);
    nodes.add(new UserWorkFlowNodeDefinitionImpl("u1", "u1", config));
    links.add(new SPELWorkFlowLinkDefinitionImpl("start", "u1", "true"));
  }

  private void start(List<WorkFlowNodeDefinition> nodes) throws WorkFlowDefinitionException {
    nodes.add(new StartWorkFlowNodeDefinitionImpl("start", "start"));
  }

  public byte[] serialize(String string) {
    try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos)) {
      oos.writeObject(string);
      return bos.toByteArray();
    } catch (Exception e) {
      return null;
    }
  }

  public Object deserialize(byte[] config) {
    try (ByteArrayInputStream bis = new ByteArrayInputStream(config);
        ObjectInputStream ois = new ObjectInputStream(bis)) {
      return ois.readObject();
    } catch (Exception e) {
      return null;
    }
  }
}
