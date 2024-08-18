/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.impl;

import java.util.*;

import group.devtool.workflow.engine.common.JacksonUtils;
import group.devtool.workflow.engine.WorkFlowContext;
import group.devtool.workflow.engine.definition.*;
import group.devtool.workflow.impl.definition.*;
import group.devtool.workflow.impl.runtime.*;
import org.junit.Assert;
import org.junit.Test;

import group.devtool.workflow.engine.definition.UserWorkFlowNodeDefinition.UserWorkFlowConfig;
import group.devtool.workflow.engine.runtime.UserWorkFlowTask.UserWorkFlowTaskConfig;
import group.devtool.workflow.engine.WorkFlowContextImpl;
import group.devtool.workflow.engine.runtime.WorkFlowInstance;
import group.devtool.workflow.engine.runtime.WorkFlowTask;
import group.devtool.workflow.engine.runtime.WorkFlowTaskJavaDelegate;
import group.devtool.workflow.engine.WorkFlowVariable;
import group.devtool.workflow.engine.runtime.WorkFlowTask.WorkFlowTaskState;
import group.devtool.workflow.impl.definition.ChildWorkFlowNodeDefinitionImpl.ChildWorkFlowConfigImpl;
import group.devtool.workflow.impl.definition.ChildWorkFlowNodeDefinitionImpl.ChildStartUpImpl;
import group.devtool.workflow.impl.definition.TaskWorkFlowNodeDefinitionImpl.JavaTaskWorkFlowConfigImpl;
import group.devtool.workflow.impl.definition.UserWorkFlowNodeDefinitionImpl.UserWorkFlowConfigImpl;
import group.devtool.workflow.impl.runtime.UserWorkFlowTaskImpl.UserWorkFlowTaskConfigImpl;

public class WorkFlowNodeTest extends InitWorkFlowConfig {

	@Test
	public void testStart() {
		WorkFlowTask task = new StartWorkFlowTaskImpl("start", "start", "start", "start", "start", WorkFlowTaskState.DONE);
		StartWorkFlowNodeImpl node = new StartWorkFlowNodeImpl("start", "start", Integer.valueOf(1), null, "start", "start", task);
		Assert.assertTrue(node.done());
	}

	@Test
	public void testStartDefinition() {
		WorkFlowDefinition definition = getDefinition("test");

		WorkFlowContextImpl context = new WorkFlowContextImpl("test");
		context.addRuntimeVariable(WorkFlowVariable.global(WorkFlowContext.USER, "admin"));
		StartWorkFlowNodeImpl node = new StartWorkFlowNodeImpl(dbConfig.idSupplier().getNodeId(), definition.getStartNode(), "test", "test", context);
		Assert.assertEquals(1, node.getTasks().length);
		Assert.assertTrue(node.done());
	}

	@Test
	public void testEnd() {
		WorkFlowTask tasks = new EndWorkFlowTaskImpl("end", "end", "end", WorkFlowTaskState.DONE, "end", "end");
		EndWorkFlowNodeImpl node = new EndWorkFlowNodeImpl("end", "end", Integer.valueOf(1), null, "end", "end", tasks);
		Assert.assertTrue(node.done());
	}

	@Test
	public void testEndDefinition() {
		WorkFlowDefinitionImpl definition = getDefinition("test");
		WorkFlowContextImpl context = new WorkFlowContextImpl("test");
		context.addRuntimeVariable(WorkFlowVariable.global(WorkFlowContext.USER, "admin"));
		EndWorkFlowNodeImpl node = new EndWorkFlowNodeImpl(dbConfig.idSupplier().getNodeId(), definition.getEndNode(), "test", "test", context);
		Assert.assertEquals(1, node.getTasks().length);
		Assert.assertTrue(node.done());
	}

	@Test
	public void testUser() {
		UserWorkFlowTaskConfig u1Config = new UserWorkFlowTaskConfigImpl("u1");
		WorkFlowTask u1 = new UserWorkFlowTaskImpl("u1", "u1", "u1", u1Config, "user", "user", WorkFlowTaskState.DONE);

		UserWorkFlowTaskConfig u2Config = new UserWorkFlowTaskConfigImpl("u2");
		WorkFlowTask u2 = new UserWorkFlowTaskImpl("u2", "u2", "u2", u2Config, "user", "user", WorkFlowTaskState.DONE);

		UserWorkFlowConfig u1NodeConfig = new UserWorkFlowConfigImpl(Arrays.asList("u1", "u2"), 1);
		UserWorkFlowNodeImpl node = new UserWorkFlowNodeImpl("user", "user", Integer.valueOf(1), u1NodeConfig, "user", "user", u1, u2);
		Assert.assertTrue(node.done());
	}

	@Test
	public void testUserDefinition() {
		WorkFlowDefinitionImpl definition = getDefinition("test");

		WorkFlowContextImpl context = new WorkFlowContextImpl("test");
		List<WorkFlowNodeDefinition> userDefinition = definition.next("start", context);
		context.addRuntimeVariable(WorkFlowVariable.global(WorkFlowContext.USER, "admin"));
		UserWorkFlowNodeImpl node = new UserWorkFlowNodeImpl(dbConfig.idSupplier().getNodeId(), userDefinition.get(0), "test", "test", context);
		Assert.assertEquals(2, node.getTasks().length);
		WorkFlowTask[] tasks = node.getTasks();

		Assert.assertTrue(tasks[0] instanceof UserWorkFlowTaskImpl);

		Assert.assertFalse(node.done());

		for (WorkFlowTask item : node.getTasks()) {
			UserWorkFlowTaskConfigImpl itemConfig = JacksonUtils.deserialize(item.getTaskConfig(), UserWorkFlowTaskConfigImpl.class);
			context.addRuntimeVariable(WorkFlowVariable.global(WorkFlowContext.USER, itemConfig.getPendingUser()));
			item.complete(context);
		}

		Assert.assertTrue(node.done());

	}

	@Test
	public void testChild() {
		WorkFlowTask task = new ChildWorkFlowTaskImpl("childTask", "child", "child", "child", "child", WorkFlowTaskState.DONE);
		ChildWorkFlowNodeImpl child = new ChildWorkFlowNodeImpl("child", "child", 1, null, "child", "child", task);
		Assert.assertTrue(child.done());
	}

	@Test
	public void testChildDefinition() {
		WorkFlowDefinitionImpl definition = getDefinition("test");
		WorkFlowContextImpl context = new WorkFlowContextImpl("test");
		List<WorkFlowNodeDefinition> nodes = definition.next("u2", context);

		ChildWorkFlowNodeImpl child = new ChildWorkFlowNodeImpl(dbConfig.idSupplier().getNodeId(), nodes.get(0), "tc", "test", context);
		Assert.assertEquals(1, child.getTasks().length);
		WorkFlowTask task = child.getTasks()[0];

		List<WorkFlowInstance> childInstances = child.instances((definitionCode, taskId) -> {
			return dbConfig.factory().childFactory(((ChildWorkFlowNodeDefinition) nodes.get(0)).getChild().get(0), taskId, "test");
		});
		Assert.assertEquals(1, childInstances.size());

		ChildWorkFlowInstanceImpl childInstance = (ChildWorkFlowInstanceImpl) childInstances.get(0);
		Assert.assertEquals(task.getTaskId(), childInstance.getParentId());
		Assert.assertEquals("test", childInstance.getRootInstanceId());
		Assert.assertEquals("cc", childInstance.getDefinitionCode());
	}

	@Test
	public void testMultiChildDefinition() {
		List<ChildStartUpImpl> startup = new ArrayList<>();
		startup.add(new ChildStartUpImpl("test1", 1, null));
		startup.add(new ChildStartUpImpl("test2", 1, "#a > 2"));
		startup.add(new ChildStartUpImpl("test3", 1, "#a < 2"));
		ChildWorkFlowConfigImpl config = new ChildWorkFlowConfigImpl(startup);
		List<WorkFlowDefinition> definitions = new ArrayList<>();
		definitions.add(getDefinition("test1"));
		definitions.add(getDefinition("test2"));
		definitions.add(getDefinition("test3"));
		ChildWorkFlowNodeDefinitionImpl childDef = new ChildWorkFlowNodeDefinitionImpl("multi", "multi", config, definitions);

		WorkFlowContextImpl context = new WorkFlowContextImpl("instance", WorkFlowVariable.global("a", 3));
		ChildWorkFlowNodeImpl child = new ChildWorkFlowNodeImpl("child", childDef, "instance", "instance", context);
		List<WorkFlowInstance> childInstances = child.instances((definitionCode, taskId) -> {
			if (definitionCode.equals("test1")) {
				return new ChildWorkFlowInstanceImpl("childInstance1", taskId, "instance", definitions.get(0));
			}
			if (definitionCode.equals("test2")) {
				return new ChildWorkFlowInstanceImpl("childInstance2", taskId, "instance", definitions.get(1));
			}
			if (definitionCode.equals("test3")) {
				return new ChildWorkFlowInstanceImpl("childInstance3", taskId, "instance", definitions.get(2));
			}
			return null;
		});

		WorkFlowTask[] tasks = child.getTasks();
		Assert.assertTrue(childInstances.size() == 2);
		Assert.assertTrue(tasks.length == 2);

		List<String> codes = new ArrayList<>();
		for (WorkFlowTask task : tasks) {
			for (WorkFlowInstance instance : childInstances) {
				ChildWorkFlowInstanceImpl ci = (ChildWorkFlowInstanceImpl) instance;
				if (ci.getParentId().equals(task.getTaskId())) {
					codes.add(ci.getDefinitionCode());
				}
			}
		}
		Object[] result = codes.stream().sorted().toArray();
		Assert.assertArrayEquals(new String[]{"test1", "test2"}, result);

	}

	@Test
	public void testTask() {
		String className = "group.devtool.workflow.impl.WorkFlowTaskTest$HelloWorldTask";
		JavaWorkFlowTaskImpl.JavaTaskWorkFlowTaskConfigImpl config = new JavaWorkFlowTaskImpl.JavaTaskWorkFlowTaskConfigImpl(className, false, "h");
		WorkFlowTask task = new JavaWorkFlowTaskImpl("java", "task", "task", config, "task", "task", WorkFlowTaskState.DONE);
		TaskWorkFlowNodeImpl node = new TaskWorkFlowNodeImpl("task", "task", 1, null, "task", "task", task);
		Assert.assertTrue(node.done());
	}

	@Test
	public void testTaskDefinition() {
		JavaTaskWorkFlowConfigImpl config = new JavaTaskWorkFlowConfigImpl(
						"group.devtool.workflow.impl.WorkFlowNodeTest$HelloWorldTask", false, "h");
		WorkFlowContextImpl context = new WorkFlowContextImpl("test");
		Assert.assertNull(context.lookup("h"));
		TaskWorkFlowNodeDefinitionImpl taskDefinition = new TaskWorkFlowNodeDefinitionImpl("hello", "hello",
						config);

		TaskWorkFlowNodeImpl node = new TaskWorkFlowNodeImpl(dbConfig.idSupplier().getNodeId(), taskDefinition, "test", "test", context);
		Assert.assertTrue(node.done());

		Assert.assertEquals("hello world", context.lookup("h"));
	}

	@Test
	public void testFork() {

	}


	public static class HelloWorldTask implements WorkFlowTaskJavaDelegate {

		@Override
		public String apply(WorkFlowContextImpl context) {
			HashMap<String, String> result = new HashMap<>();
			result.put("output", "hello world");
			return "hello world";
		}
	}

	private WorkFlowDefinitionImpl getDefinition(String code) {
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

		return new WorkFlowDefinitionImpl(code, code, code, nodes, links);
	}

	private void end(List<WorkFlowNodeDefinition> nodes, List<WorkFlowLinkDefinition> links) {
		nodes.add(new EndWorkFlowNodeDefinitionImpl("end", "end"));
		links.add(new SPELWorkFlowLinkDefinitionImpl("l4", "cc", "end", "#a > 3"));
		links.add(new SPELWorkFlowLinkDefinitionImpl("l5", "u1", "end", "#a < 2"));
	}

	private void child(List<WorkFlowNodeDefinition> nodes, List<WorkFlowLinkDefinition> links) {
		List<WorkFlowLinkDefinition> clinks = new ArrayList<>();
		List<WorkFlowNodeDefinition> cnodes = new ArrayList<>();

		cnodes.add(new StartWorkFlowNodeDefinitionImpl("start1", "start1"));
		cnodes.add(new UserWorkFlowNodeDefinitionImpl("cu1", "cu1", new UserWorkFlowConfigImpl(Collections.singletonList("cu1"), 1)));
		cnodes.add(new EndWorkFlowNodeDefinitionImpl("end1", "end1"));

		clinks.add(new SPELWorkFlowLinkDefinitionImpl("cl1", "start1", "cu1", "true"));
		clinks.add(new SPELWorkFlowLinkDefinitionImpl("cl2", "cu1", "end1", "true"));
		WorkFlowDefinitionImpl childDef = new WorkFlowDefinitionImpl("cc", "cc", "test", cnodes, clinks);
		ChildWorkFlowConfigImpl config3 = new ChildWorkFlowConfigImpl(Arrays.asList(new ChildWorkFlowNodeDefinitionImpl.ChildStartUpImpl(childDef.getCode(), 1, null)));
		ChildWorkFlowNodeDefinitionImpl child = new ChildWorkFlowNodeDefinitionImpl("child", "child", config3,
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
