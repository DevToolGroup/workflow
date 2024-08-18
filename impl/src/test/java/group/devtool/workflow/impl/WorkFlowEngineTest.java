/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.impl;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import group.devtool.workflow.engine.WorkFlowContext;
import group.devtool.workflow.engine.common.JacksonUtils;
import group.devtool.workflow.engine.exception.*;
import group.devtool.workflow.engine.runtime.*;
import group.devtool.workflow.impl.definition.*;
import group.devtool.workflow.impl.repository.WorkFlowRepository;
import group.devtool.workflow.impl.runtime.UserWorkFlowTaskImpl;
import org.junit.Assert;
import org.junit.Test;

import group.devtool.workflow.engine.definition.UserWorkFlowNodeDefinition.UserWorkFlowConfig;
import group.devtool.workflow.engine.definition.WorkFlowDefinition;
import group.devtool.workflow.engine.WorkFlowEngine;
import group.devtool.workflow.engine.definition.WorkFlowLinkDefinition;
import group.devtool.workflow.engine.definition.WorkFlowNodeDefinition;
import group.devtool.workflow.engine.WorkFlowVariable;
import group.devtool.workflow.impl.definition.ChildWorkFlowNodeDefinitionImpl.ChildWorkFlowConfigImpl;
import group.devtool.workflow.impl.definition.UserWorkFlowNodeDefinitionImpl.UserWorkFlowConfigImpl;

public class WorkFlowEngineTest extends InitWorkFlowConfig {

	@Test
	public void testDeploy() {
		WorkFlowEngine engine = new WorkFlowEngine(dbConfig);
		engine.deploy(getDefinition("engine1"));
	}

	@Test
	public void testUnDeploy() {
		WorkFlowEngine engine = new WorkFlowEngine(dbConfig);
		engine.deploy(getDefinition("engine2"));
		engine.undeploy("engine2");
		WorkFlowDefinition definition = engine.load("engine2", 1);
		Assert.assertNotNull(definition);
	}

	@Test
	public void testRun() {
		WorkFlowEngine engine = new WorkFlowEngine(dbConfig);
		engine.deploy(getDefinition("engine3"));
		String instanceId = engine.start("engine3", WorkFlowVariable.global(WorkFlowContext.USER, "admin"));
		Assert.assertNotNull(instanceId);

		WorkFlowNode u1 = getNode("engine_u1", instanceId);

		Assert.assertNotNull(u1);
		Assert.assertFalse(u1.done());
		AbstractWorkFlowNode an = (AbstractWorkFlowNode) u1;
		WorkFlowTask[] tasks = an.getTasks();
		Assert.assertEquals(2, tasks.length);

		// 校验用户
		Assert.assertThrows(NotUserTaskPermission.class, () -> engine.run(instanceId, tasks[1].getTaskId()));

		// 节点u1执行
		UserWorkFlowTaskImpl.UserWorkFlowTaskConfigImpl task1Config = JacksonUtils.deserialize(tasks[0].getTaskConfig(), UserWorkFlowTaskImpl.UserWorkFlowTaskConfigImpl.class);
		engine.run(instanceId, tasks[0].getTaskId(), WorkFlowVariable.global(WorkFlowContext.USER, task1Config.getPendingUser()));
		u1 = getNode("engine_u1", instanceId);
		Assert.assertNotNull(u1);
		Assert.assertFalse(u1.done());

		UserWorkFlowTaskImpl.UserWorkFlowTaskConfigImpl task2Config = JacksonUtils.deserialize(tasks[1].getTaskConfig(), UserWorkFlowTaskImpl.UserWorkFlowTaskConfigImpl.class);
		engine.run(instanceId, tasks[1].getTaskId(), WorkFlowVariable.global(WorkFlowContext.USER, task2Config.getPendingUser()),
						WorkFlowVariable.global("a", 3));

		// 节点u2判断
		WorkFlowNode u2 = getNode("engine_u2", instanceId);
		Assert.assertNotNull(u2);
		Assert.assertFalse(u2.done());

		AbstractWorkFlowNode an2 = (AbstractWorkFlowNode) u2;
		WorkFlowTask[] tasks2 = an2.getTasks();
		Assert.assertEquals(2, tasks2.length);

		// 节点u2执行
		// 校验用户
		Assert.assertThrows(NotUserTaskPermission.class, () -> engine.run(instanceId, tasks2[1].getTaskId()));

		UserWorkFlowTaskImpl.UserWorkFlowTaskConfigImpl task3Config = JacksonUtils.deserialize(tasks2[0].getTaskConfig(), UserWorkFlowTaskImpl.UserWorkFlowTaskConfigImpl.class);
		engine.run(instanceId, tasks2[0].getTaskId(), WorkFlowVariable.global(WorkFlowContext.USER, task3Config.getPendingUser()));
		u2 = getNode("engine_u2", instanceId);
		Assert.assertNotNull(u2);
		Assert.assertFalse(u2.done());

		UserWorkFlowTaskImpl.UserWorkFlowTaskConfigImpl task4Config = JacksonUtils.deserialize(tasks2[1].getTaskConfig(), UserWorkFlowTaskImpl.UserWorkFlowTaskConfigImpl.class);
		engine.run(instanceId, tasks2[1].getTaskId(), WorkFlowVariable.global(WorkFlowContext.USER, task4Config.getPendingUser()));

		// 嵌套子节点执行
		WorkFlowNode engineChild = getNode("engine_child", instanceId);
		Assert.assertNotNull(engineChild);
		Assert.assertFalse(engineChild.done());

		ChildWorkFlowNode child = (ChildWorkFlowNode) engineChild;
		WorkFlowTask[] childTasks = child.getTasks();
		Assert.assertNotNull(childTasks[0]);

		ChildWorkFlowTask childTask = (ChildWorkFlowTask) childTasks[0];
		Assert.assertFalse(childTask.completed());

		WorkFlowNode childU1 = getNode("engine_cu1", instanceId);
		Assert.assertNotNull(childU1);
		Assert.assertFalse(childU1.done());

		AbstractWorkFlowNode aChildU1 = (AbstractWorkFlowNode) childU1;
		WorkFlowTask[] aChildTask1 = aChildU1.getTasks();
		Assert.assertEquals(1, aChildTask1.length);

		WorkFlowTask aChildTaskU1 = aChildTask1[0];
		engine.run(instanceId, aChildTaskU1.getTaskId(), WorkFlowVariable.global(WorkFlowContext.USER, "engine_cu1"));

		WorkFlowInstance instance = getInstance(instanceId, instanceId);
		Assert.assertTrue(instance.done());

	}

	@Test
	public void testStop() {
		try {
			WorkFlowEngine engine = new WorkFlowEngine(dbConfig);
			engine.deploy(getDefinition("engine4"));
			String instanceId = engine.start("engine4", WorkFlowVariable.global(WorkFlowContext.USER, "admin"));

			engine.stop(instanceId);

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testStopChild() {
		try {
			WorkFlowEngine engine = new WorkFlowEngine(dbConfig);
			engine.deploy(getDefinition("engine5"));
			String instanceId = engine.start("engine5", WorkFlowVariable.global(WorkFlowContext.USER, "admin"));

			WorkFlowNode u1 = getNode("engine_u1", instanceId);
			AbstractWorkFlowNode an = (AbstractWorkFlowNode) u1;
			WorkFlowTask[] tasks = an.getTasks();
			engine.run(instanceId, tasks[0].getTaskId(), WorkFlowVariable.global(WorkFlowContext.USER, "engine_u1"));
			engine.run(instanceId, tasks[1].getTaskId(), WorkFlowVariable.global(WorkFlowContext.USER, "engine_u2"),
							WorkFlowVariable.global("a", 3));

			WorkFlowNode u2 = getNode("engine_u2", instanceId);
			AbstractWorkFlowNode an2 = (AbstractWorkFlowNode) u2;
			WorkFlowTask[] tasks2 = an2.getTasks();
			engine.run(instanceId, tasks2[0].getTaskId(), WorkFlowVariable.global(WorkFlowContext.USER, "engine_u1"));
			engine.run(instanceId, tasks2[1].getTaskId(), WorkFlowVariable.global(WorkFlowContext.USER, "engine_u2"));

			Assert.assertNotNull(instanceId);
			WorkFlowNode childU1 = getNode("engine_cu1", instanceId);
			Assert.assertNotEquals(instanceId, childU1.getInstanceId());

			Assert.assertThrows(NotFoundWorkFlowInstance.class, () -> engine.stop(childU1.getInstanceId()));

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testRollback() {
		WorkFlowRepository repository = new WorkFlowRepository();
		WorkFlowRepository ms = spy(repository);
		dbConfig.setRepository(ms);

		String instanceId = null;
		try {

			WorkFlowEngine engine = new WorkFlowEngine(dbConfig);

			engine.deploy(getDefinition("engine6"));
			instanceId = engine.start("engine6", WorkFlowVariable.global(WorkFlowContext.USER, "admin"));

			WorkFlowNode u1 = getNode("engine_u1", instanceId);
			AbstractWorkFlowNode an = (AbstractWorkFlowNode) u1;
			WorkFlowTask[] tasks = an.getTasks();
			engine.run(instanceId, tasks[0].getTaskId(), WorkFlowVariable.global(WorkFlowContext.USER, "engine_u1"));
			engine.run(instanceId, tasks[1].getTaskId(), WorkFlowVariable.global(WorkFlowContext.USER, "engine_u2"),
							WorkFlowVariable.global("a", 3));

			WorkFlowNode u2 = getNode("engine_u2", instanceId);
			AbstractWorkFlowNode an2 = (AbstractWorkFlowNode) u2;
			WorkFlowTask[] tasks2 = an2.getTasks();
			engine.run(instanceId, tasks2[0].getTaskId(), WorkFlowVariable.global(WorkFlowContext.USER, "engine_u1"));

			// 抛出异常
			doThrow(DBMockException.class).when(ms).changeNodeComplete("engine_u2", instanceId, 1);
			engine.run(instanceId, tasks2[1].getTaskId(), WorkFlowVariable.global(WorkFlowContext.USER, "engine_u2"));

		} catch (Exception e) {
			e.printStackTrace();
			if (!(e instanceof DBMockException)) {
				Assert.fail(e.getMessage());
			}
		}
	}

	@Test
	public void testRunMultiChild() {
		WorkFlowEngine engine = new WorkFlowEngine(dbConfig);
		engine.deploy(getMultiDefinition());
		String instanceId = engine.start("multi", WorkFlowVariable.global(WorkFlowContext.USER, "admin"));
		Assert.assertNotNull(instanceId);

		WorkFlowNode u1 = getNode("engine_u1", instanceId);

		Assert.assertNotNull(u1);
		Assert.assertFalse(u1.done());
		AbstractWorkFlowNode an = (AbstractWorkFlowNode) u1;
		WorkFlowTask[] tasks = an.getTasks();
		Assert.assertEquals(2, tasks.length);

		// 节点u1执行
		UserWorkFlowTaskImpl.UserWorkFlowTaskConfigImpl task1Config = JacksonUtils.deserialize(tasks[0].getTaskConfig(), UserWorkFlowTaskImpl.UserWorkFlowTaskConfigImpl.class);
		engine.run(instanceId, tasks[0].getTaskId(), WorkFlowVariable.global(WorkFlowContext.USER, task1Config.getPendingUser()));

		UserWorkFlowTaskImpl.UserWorkFlowTaskConfigImpl task2Config = JacksonUtils.deserialize(tasks[1].getTaskConfig(), UserWorkFlowTaskImpl.UserWorkFlowTaskConfigImpl.class);
		engine.run(instanceId, tasks[1].getTaskId(), WorkFlowVariable.global(WorkFlowContext.USER, task2Config.getPendingUser()),
						WorkFlowVariable.global("a", 3));

		// 嵌套子节点执行
		WorkFlowNode engineChild = getNode("engine_child", instanceId);
		Assert.assertNotNull(engineChild);
		Assert.assertFalse(engineChild.done());

		ChildWorkFlowNode child = (ChildWorkFlowNode) engineChild;
		WorkFlowTask[] childTasks = child.getTasks();
		Assert.assertEquals(2, childTasks.length);

		ChildWorkFlowTask childTask1 = (ChildWorkFlowTask) childTasks[0];
		Assert.assertFalse(childTask1.completed());

		ChildWorkFlowTask childTask2 = (ChildWorkFlowTask) childTasks[1];
		Assert.assertFalse(childTask2.completed());

		WorkFlowNode childU1 = getChildNode("engine_cu1", childTask1.getTaskId(), instanceId);
		AbstractWorkFlowNode aChildU1 = (AbstractWorkFlowNode) childU1;
		WorkFlowTask[] aChildTask1 = aChildU1.getTasks();

		WorkFlowTask aChildTaskU1 = aChildTask1[0];
		engine.run(instanceId, aChildTaskU1.getTaskId(), WorkFlowVariable.global(WorkFlowContext.USER, "engine_cu1"));

		WorkFlowNode childU2 = getChildNode("engine_cu1", childTask2.getTaskId(), instanceId);
		AbstractWorkFlowNode aChildU2 = (AbstractWorkFlowNode) childU2;
		WorkFlowTask[] aChildTask2 = aChildU2.getTasks();

		WorkFlowTask aChildTaskU2 = aChildTask2[0];
		engine.run(instanceId, aChildTaskU2.getTaskId(), WorkFlowVariable.global(WorkFlowContext.USER, "engine_cu1"));

		WorkFlowInstance instance = getInstance(instanceId, instanceId);
		Assert.assertTrue(instance.done());
	}

	private WorkFlowDefinitionImpl getMultiDefinition() {
		List<WorkFlowNodeDefinition> nodes = new ArrayList<>();
		List<WorkFlowLinkDefinition> links = new ArrayList<>();
		// 开始
		nodes.add(new StartWorkFlowNodeDefinitionImpl("engine_start", "engine_start"));

		// 用户节点 1
		UserWorkFlowConfig config = new UserWorkFlowConfigImpl(Arrays.asList("engine_u1", "engine_u2"), 2);
		nodes.add(new UserWorkFlowNodeDefinitionImpl("engine_u1", "engine_u1", config));
		links.add(new SPELWorkFlowLinkDefinitionImpl("l1", "engine_start", "engine_u1", "true"));

		// 子流程
		List<WorkFlowLinkDefinition> clinks = new ArrayList<>();
		List<WorkFlowNodeDefinition> cnodes = new ArrayList<>();

		cnodes.add(new StartWorkFlowNodeDefinitionImpl("engine_start1", "engine_start1"));
		cnodes.add(
						new UserWorkFlowNodeDefinitionImpl("engine_cu1", "engine_cu1",
										new UserWorkFlowConfigImpl(Arrays.asList("engine_cu1"), 1)));
		cnodes.add(new EndWorkFlowNodeDefinitionImpl("engine_end1", "engine_end1"));

		clinks.add(new SPELWorkFlowLinkDefinitionImpl("cl1", "engine_start1", "engine_cu1", "true"));
		clinks.add(new SPELWorkFlowLinkDefinitionImpl("cl2", "engine_cu1", "engine_end1", "true"));

		WorkFlowDefinitionImpl childDef1 = new WorkFlowDefinitionImpl("engine_c1", "engine_c1", "multi", cnodes, clinks);
		WorkFlowDefinitionImpl childDef2 = new WorkFlowDefinitionImpl("engine_c2", "engine_c2", "multi", cnodes, clinks);

		ChildWorkFlowConfigImpl config3 = new ChildWorkFlowConfigImpl(Arrays.asList(
						new ChildWorkFlowNodeDefinitionImpl.ChildStartUpImpl(childDef1.getCode(), 1, null),
						new ChildWorkFlowNodeDefinitionImpl.ChildStartUpImpl(childDef2.getCode(), 1, null)
		));

		ChildWorkFlowNodeDefinitionImpl child = new ChildWorkFlowNodeDefinitionImpl("engine_child", "engine_child",
						config3,
						Arrays.asList(childDef1, childDef2));
		nodes.add(child);
		links.add(new SPELWorkFlowLinkDefinitionImpl("l3", "engine_u1", "engine_child", "true"));


		// 结束
		nodes.add(new EndWorkFlowNodeDefinitionImpl("engine_end", "engine_end"));
		links.add(new SPELWorkFlowLinkDefinitionImpl("l4", "engine_child", "engine_end", "true"));

		return new WorkFlowDefinitionImpl("multi", "multi", "multi", nodes, links);
	}

	private WorkFlowDefinition getDefinition(String code) {
		List<WorkFlowNodeDefinition> nodes = new ArrayList<>();
		List<WorkFlowLinkDefinition> links = new ArrayList<>();
		// 开始
		start(nodes);

		// 用户节点 1
		u1(nodes, links);

		// 用户节点 2
		u2(nodes, links);

		// 子流程节点
		child(code, nodes, links);

		// 结束
		end(nodes, links);

		return new WorkFlowDefinitionImpl(code, code, code, nodes, links);
	}

	private void start(List<WorkFlowNodeDefinition> nodes) {
		nodes.add(new StartWorkFlowNodeDefinitionImpl("engine_start", "engine_start"));
	}

	private void u1(List<WorkFlowNodeDefinition> nodes, List<WorkFlowLinkDefinition> links) {
		UserWorkFlowConfig config = new UserWorkFlowConfigImpl(
						Arrays.asList("engine_u1", "engine_u2"), 2);
		nodes.add(new UserWorkFlowNodeDefinitionImpl("engine_u1", "engine_u1", config));
		links.add(new SPELWorkFlowLinkDefinitionImpl("l1", "engine_start", "engine_u1", "true"));
	}

	private void u2(List<WorkFlowNodeDefinition> nodes, List<WorkFlowLinkDefinition> links) {
		UserWorkFlowConfig config2 = new UserWorkFlowConfigImpl(
						Arrays.asList("engine_u1", "engine_u2"), 2);
		nodes.add(new UserWorkFlowNodeDefinitionImpl("engine_u2", "engine_u2", config2));
		links.add(new SPELWorkFlowLinkDefinitionImpl("l2", "engine_u1", "engine_u2", "#a != null ? (#a > 2) : false"));
	}

	private void child(String code, List<WorkFlowNodeDefinition> nodes, List<WorkFlowLinkDefinition> links) {
		List<WorkFlowLinkDefinition> clinks = new ArrayList<>();
		List<WorkFlowNodeDefinition> cnodes = new ArrayList<>();

		cnodes.add(new StartWorkFlowNodeDefinitionImpl("engine_start1", "engine_start1"));
		cnodes.add(
						new UserWorkFlowNodeDefinitionImpl("engine_cu1", "engine_cu1",
										new UserWorkFlowConfigImpl(Arrays.asList("engine_cu1"), 1)));
		cnodes.add(new EndWorkFlowNodeDefinitionImpl("engine_end1", "engine_end1"));

		clinks.add(new SPELWorkFlowLinkDefinitionImpl("cl1", "engine_start1", "engine_cu1", "true"));
		clinks.add(new SPELWorkFlowLinkDefinitionImpl("cl2", "engine_cu1", "engine_end1", "true"));
		WorkFlowDefinitionImpl childDef = new WorkFlowDefinitionImpl("engine_cc", "engine_cc", code, cnodes, clinks);
		ChildWorkFlowConfigImpl config3 = new ChildWorkFlowConfigImpl(Arrays.asList(new ChildWorkFlowNodeDefinitionImpl.ChildStartUpImpl(childDef.getCode(), 1, null)));
		ChildWorkFlowNodeDefinitionImpl child = new ChildWorkFlowNodeDefinitionImpl("engine_child", "engine_child",
						config3,
						Arrays.asList(childDef));
		nodes.add(child);
		links.add(new SPELWorkFlowLinkDefinitionImpl("l3", "engine_u2", "engine_child", "true"));
	}

	private void end(List<WorkFlowNodeDefinition> nodes, List<WorkFlowLinkDefinition> links) {
		nodes.add(new EndWorkFlowNodeDefinitionImpl("engine_end", "engine_end"));
		links.add(new SPELWorkFlowLinkDefinitionImpl("l4", "engine_child", "engine_end", "true"));
		links.add(new SPELWorkFlowLinkDefinitionImpl("l5", "engine_u1", "engine_end", "#a != null ? (#a < 2) : false"));
	}

	private WorkFlowNode getChildNode(String nodeCode, String taskId, String instanceId) {
		return dbConfig.dbTransaction().doInTransaction(() -> {
			WorkFlowServiceImpl service = new WorkFlowServiceImpl();
			return service.getChildActiveNodeByCode(nodeCode, taskId, instanceId);
		});
	}

	private WorkFlowNode getNode(String nodeCode, String instanceId) {
		return dbConfig.dbTransaction().doInTransaction(() -> {
			WorkFlowServiceImpl service = new WorkFlowServiceImpl();
			return service.getActiveNodeByCode(nodeCode, instanceId);
		});
	}

	private WorkFlowInstance getInstance(String instanceId, String rootInstanceId) {
		return dbConfig.dbTransaction().doInTransaction(() -> {
			WorkFlowServiceImpl service = new WorkFlowServiceImpl();
			return service.getInstance(instanceId, rootInstanceId);
		});
	}

	public static class DBMockException extends RuntimeException {
		public DBMockException() {
			super();
		}
	}
}
