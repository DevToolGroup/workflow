package group.devtool.workflow.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import group.devtool.workflow.core.exception.WorkFlowDefinitionException;
import group.devtool.workflow.core.exception.WorkFlowException;
import org.junit.Assert;
import org.junit.Test;

import group.devtool.workflow.core.UserWorkFlowNodeDefinition.WorkFlowUserConfig;
import group.devtool.workflow.core.UserWorkFlowTask.WorkFlowUserTaskConfig;
import group.devtool.workflow.core.WorkFlowContext;
import group.devtool.workflow.core.WorkFlowDefinition;
import group.devtool.workflow.core.WorkFlowInstance;
import group.devtool.workflow.core.WorkFlowLinkDefinition;
import group.devtool.workflow.core.WorkFlowNodeDefinition;
import group.devtool.workflow.core.WorkFlowTask;
import group.devtool.workflow.core.WorkFlowTaskJavaDelegate;
import group.devtool.workflow.core.WorkFlowVariable;
import group.devtool.workflow.core.WorkFlowTask.WorkFlowTaskState;
import group.devtool.workflow.core.ChildWorkFlowNodeDefinition;
import group.devtool.workflow.impl.ChildWorkFlowNodeDefinitionImpl.MybatisWorkFlowChildConfig;
import group.devtool.workflow.impl.TaskWorkFlowNodeDefinitionImpl.JavaTaskConfigImpl;
import group.devtool.workflow.impl.UserWorkFlowNodeDefinitionImpl.WorkFlowUserConfigImpl;
import group.devtool.workflow.impl.UserWorkFlowTaskImpl.MybatisWorkFlowUserTaskConfig;

public class WorkFlowNodeTest extends WorkFlowBeforeTest {

  @Test
  public void testStart() throws WorkFlowException {
		WorkFlowTask tasks = new StartWorkFlowTaskImpl("start", "start", "start", "start", WorkFlowTaskState.DONE);
		StartWorkFlowNodeImpl node = new StartWorkFlowNodeImpl("start", "start", "start", tasks);
		Assert.assertTrue(node.done());
	}

  @Test
  public void testStartDefinition() throws WorkFlowException {
		WorkFlowDefinition definition = getDefinition();

		WorkFlowContext context = new WorkFlowContext("test");
		context.localVariable(WorkFlowVariable.suspend(WorkFlowVariable.USER, "admin"));
		StartWorkFlowNodeImpl node = new StartWorkFlowNodeImpl(definition.start(), "test", "test", context);
		Assert.assertEquals(1, node.getTasks().length);
		Assert.assertTrue(node.done());
	}

  @Test
  public void testEnd() throws WorkFlowException {
		WorkFlowTask tasks = new EndWorkFlowTaskImpl("end", "end", WorkFlowTaskState.DONE, "end", "end");
		EndWorkFlowNodeImpl node = new EndWorkFlowNodeImpl("end", "end", "end", tasks);
		Assert.assertTrue(node.done());
	}

  @Test
  public void testEndDefinition() throws WorkFlowException {
		WorkFlowDefinitionImpl definition = getDefinition();
		WorkFlowContext context = new WorkFlowContext("test");
		context.localVariable(WorkFlowVariable.suspend(WorkFlowVariable.USER, "admin"));
		EndWorkFlowNodeImpl node = new EndWorkFlowNodeImpl(definition.end(), "test", "test", context);
		Assert.assertEquals(1, node.getTasks().length);
		Assert.assertTrue(node.done());
	}

  @Test
  public void testUser() throws WorkFlowException {
		List<String> member = Arrays.asList("u1", "u2");
		WorkFlowUserTaskConfig u1Config = new MybatisWorkFlowUserTaskConfig("u1", member, 2);
		WorkFlowTask u1 = new UserWorkFlowTaskImpl("u1", "u1", u1Config, "user", "user", WorkFlowTaskState.DONE);

		WorkFlowUserTaskConfig u2Config = new MybatisWorkFlowUserTaskConfig("u2", member, 2);
		WorkFlowTask u2 = new UserWorkFlowTaskImpl("u2", "u2", u2Config, "user", "user", WorkFlowTaskState.DONE);

		UserWorkFlowNodeImpl node = new UserWorkFlowNodeImpl("user", u1Config, "user", "user", u1, u2);
		Assert.assertTrue(node.done());

	}

  @Test
  public void testUserDefinition() throws WorkFlowException {
		WorkFlowDefinitionImpl definition = getDefinition();

		WorkFlowContext context = new WorkFlowContext("test");
		List<WorkFlowNodeDefinition> userDefinition = definition.next("start", context);
		context.localVariable(WorkFlowVariable.suspend(WorkFlowVariable.USER, "admin"));
		UserWorkFlowNodeImpl node = new UserWorkFlowNodeImpl(userDefinition.get(0), "test", "test", context);
		Assert.assertEquals(2, node.getTasks().length);
		WorkFlowTask[] tasks = node.getTasks();

		Assert.assertTrue(tasks[0] instanceof UserWorkFlowTaskImpl);
		UserWorkFlowTaskImpl task = (UserWorkFlowTaskImpl) tasks[0];
		Object userConfig = deserialize(task.getTaskConfig());
		Assert.assertTrue(userConfig instanceof MybatisWorkFlowUserTaskConfig);

		MybatisWorkFlowUserTaskConfig userTaskConfig = (MybatisWorkFlowUserTaskConfig) userConfig;
		Assert.assertArrayEquals(Arrays.asList("u1", "u2").toArray(), userTaskConfig.member().toArray());
		Assert.assertTrue(userTaskConfig.member().contains(userTaskConfig.pendingUser()));

		Assert.assertFalse(node.done());

		for (WorkFlowTask item : node.getTasks()) {
			MybatisWorkFlowUserTaskConfig itemConfig = (MybatisWorkFlowUserTaskConfig) deserialize(
					((UserWorkFlowTaskImpl) item).getTaskConfig());
			context.localVariable(WorkFlowVariable.suspend(WorkFlowVariable.USER, itemConfig.pendingUser()));
			item.complete(context);
		}
		Assert.assertTrue(node.done());

	}

  @Test
  public void testChild() throws WorkFlowException {
		WorkFlowTask task = new ChildWorkFlowTaskImpl("childTask", "child", "child", "child", WorkFlowTaskState.DONE);
		ChildWorkFlowNodeImpl child = new ChildWorkFlowNodeImpl("child", "child", "child", task);
		Assert.assertTrue(child.done());
	}

  @Test
  public void testChildDefinition() throws WorkFlowException {
		WorkFlowDefinitionImpl definition = getDefinition();
		WorkFlowContext context = new WorkFlowContext("test");
		List<WorkFlowNodeDefinition> nodes = definition.next("u2", context);

		ChildWorkFlowNodeImpl child = null;
		child = new ChildWorkFlowNodeImpl(nodes.get(0), "tc", "test", context);
		Assert.assertEquals(1, child.getTasks().length);
		WorkFlowTask task = child.getTasks()[0];

		List<WorkFlowInstance> childInstances = child.instances((definitionCode, taskId) -> {
			return dbConfig.factory().childFactory(((ChildWorkFlowNodeDefinition) nodes.get(0)).getChild(), taskId, "test");
		});
		Assert.assertEquals(1, childInstances.size());

		ChildWorkFlowInstanceImpl childInstance = (ChildWorkFlowInstanceImpl) childInstances.get(0);
		Assert.assertEquals(task.getTaskId(), childInstance.parentId());
		Assert.assertEquals("test", childInstance.rootId());
		Assert.assertEquals("cc", childInstance.getDefinitionCode());

	}

  @Test
  public void testTask() throws WorkFlowException {
    String className = "group.devtool.workflow.impl.WorkFlowTaskTest$HelloWorldTask";
    JavaTaskConfigImpl config = new JavaTaskConfigImpl(className, false, "h");
    WorkFlowTask tasks;
		tasks = new JavaWorkFlowTaskImpl("java", "task", config, "task", "task", WorkFlowTaskState.DONE);
		TaskWorkFlowNodeImpl node = new TaskWorkFlowNodeImpl("task", "task", "task", tasks);
		Assert.assertTrue(node.done());
	}

  @Test
  public void testTaskDefinition() throws WorkFlowException {
    JavaTaskConfigImpl config = new JavaTaskConfigImpl(
        "group.devtool.workflow.impl.WorkFlowNodeTest$HelloWorldTask", false, "h");
    WorkFlowContext context = new WorkFlowContext("test");
		Assert.assertNull(context.lookup("h"));
		TaskWorkFlowNodeDefinitionImpl taskDefinition = new TaskWorkFlowNodeDefinitionImpl("hello", "hello",
				config);

		TaskWorkFlowNodeImpl node = new TaskWorkFlowNodeImpl(taskDefinition, "test", "test", context);
		Assert.assertTrue(node.done());

		Assert.assertEquals("hello world", ((Map<String, String>) context.lookup("h")).get("output"));
	}

  public static class HelloWorldTask implements WorkFlowTaskJavaDelegate {

    @Override
    public Serializable apply(WorkFlowContext context) {
      System.out.println("call hello world java task");
      HashMap<String, String> result = new HashMap<>();
      result.put("output", "hello world");
      return result;
    }

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
