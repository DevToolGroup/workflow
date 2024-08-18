/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.impl;

import java.util.ArrayList;
import java.util.List;

import group.devtool.workflow.engine.common.JacksonUtils;
import group.devtool.workflow.impl.entity.WorkFlowInstanceEntity;
import group.devtool.workflow.impl.entity.WorkFlowNodeEntity;
import group.devtool.workflow.impl.entity.WorkFlowTaskEntity;
import group.devtool.workflow.impl.entity.WorkFlowVariableEntity;
import org.junit.Assert;
import org.junit.Test;

public class WorkFlowRepositoryTest extends InitWorkFlowConfig {

	@Test
	public void testBulkSaveVariable() {

		List<WorkFlowVariableEntity> entities = new ArrayList<>();
		entities.add(buildVariableEntity("test"));
		List<WorkFlowVariableEntity> variables = dbConfig.dbTransaction().doInTransaction(() -> {
			dbConfig.repository().bulkSaveVariable(entities);
			Assert.assertNotNull(entities.get(0).getId());

			return dbConfig.repository().loadVariable("test");
		});
		Assert.assertEquals(1, variables.size());
		WorkFlowVariableEntity entity = variables.get(0);

		Assert.assertEquals("a", entity.getName());
		Assert.assertEquals("1", deserialize(entity.getValue()));
	}

	@Test
	public void testSaveAndGetInstance() {
		WorkFlowInstanceEntity entity = new WorkFlowInstanceEntity();
		entity.setDefinitionCode("test");
		entity.setRootDefinitionCode("test");
		entity.setDefinitionVersion(1);
		entity.setInstanceId("test1");
		entity.setRootInstanceId("test1");
		entity.setState("DOING");
		entity.setScope("ROOT");

		WorkFlowInstanceEntity result = dbConfig.dbTransaction().doInTransaction(() -> {
			dbConfig.repository().save(entity);
			Assert.assertNotNull(entity.getId());

			WorkFlowInstanceEntity insert = dbConfig.repository().loadInstance("test1", "test1");

			dbConfig.repository().changeInstanceComplete(insert.getInstanceId(), insert.getRootInstanceId());
			return dbConfig.repository().loadInstance("test1", "test1");

		});
		Assert.assertEquals(entity.getInstanceId(), result.getInstanceId());
		Assert.assertEquals(entity.getDefinitionCode(), result.getDefinitionCode());
		Assert.assertEquals(entity.getRootInstanceId(), result.getRootInstanceId());
		Assert.assertEquals("DONE", result.getState());
	}

	@Test
	public void testSaveAndGetTask() {
		List<WorkFlowTaskEntity> tasks = new ArrayList<>();
		WorkFlowTaskEntity task = new WorkFlowTaskEntity();
		task.setTaskId("taskId1");
		task.setTaskClass("user");
		task.setTaskState("DOING");
		task.setConfig(serialize("user"));
		task.setNodeCode("nodeCode1");
		task.setNodeId("nodeCode1");
		task.setInstanceId("instanceId1");
		task.setRootInstanceId("instanceId1");
		tasks.add(task);
		dbConfig.dbTransaction().doInTransaction(() -> {

			dbConfig.repository().bulkSaveTask(tasks);
			Assert.assertNotNull(task.getId());

			List<WorkFlowTaskEntity> inserts = dbConfig.repository().loadTaskByNodeId("nodeCode1", "instanceId1");
			Assert.assertEquals(1, inserts.size());

			Assert.assertEquals(task.getInstanceId(), inserts.get(0).getInstanceId());
			Assert.assertEquals(task.getRootInstanceId(), inserts.get(0).getRootInstanceId());
			Assert.assertEquals(task.getTaskId(), inserts.get(0).getTaskId());
			Assert.assertEquals(task.getTaskClass(), inserts.get(0).getTaskClass());
			Assert.assertEquals(task.getTaskState(), inserts.get(0).getTaskState());

			Assert.assertEquals("user", deserialize(inserts.get(0).getConfig()));
			return true;
		});
	}

	@Test
	public void testChangeTaskComplete() {
		List<WorkFlowTaskEntity> tasks = new ArrayList<>();
		WorkFlowTaskEntity task = new WorkFlowTaskEntity();
		task.setTaskId("taskId2");
		task.setTaskClass("user");
		task.setTaskState("DOING");
		task.setConfig(serialize("user"));
		task.setNodeId("nodeCode2");
		task.setNodeCode("nodeCode2");
		task.setInstanceId("instanceId2");
		task.setRootInstanceId("instanceId2");
		tasks.add(task);
		WorkFlowTaskEntity result = dbConfig.dbTransaction().doInTransaction(() -> {

			dbConfig.repository().bulkSaveTask(tasks);
			dbConfig.repository().changeTaskComplete("admin", System.currentTimeMillis(), "taskId2", "instanceId2");
			return dbConfig.repository().loadTaskById("taskId2", "instanceId2");

		});
		Assert.assertNull(result);

	}

	@Test
	public void testChangeNodeComplete() {
		WorkFlowNodeEntity node = new WorkFlowNodeEntity();
		node.setConfig(serialize("user"));
		node.setNodeId("nodeCode3");
		node.setNodeCode("nodeCode3");
		node.setNodeClass("USER");
		node.setNodeState("DOING");
		node.setVersion(1);
		node.setInstanceId("instanceId3");
		node.setRootInstanceId("instanceId3");

		WorkFlowNodeEntity result = dbConfig.dbTransaction().doInTransaction(() -> {

			dbConfig.repository().save(node);

			dbConfig.repository().changeNodeComplete("nodeCode3", "instanceId3", node.getVersion());
			return dbConfig.repository().loadNode("nodeCode3", "instanceId3");
		});

		Assert.assertEquals("DONE", result.getNodeState());

	}

	private WorkFlowVariableEntity buildVariableEntity(String instanceId) {
		WorkFlowVariableEntity entity = new WorkFlowVariableEntity();
		entity.setRootInstanceId(instanceId);
		entity.setName("a");
		entity.setType("GLOBAL");
		entity.setValue(serialize("1"));
		return entity;
	}

	public String serialize(String string) {
		return JacksonUtils.serialize(string);
	}

	public Object deserialize(String config) {
		return JacksonUtils.deserialize(config, Object.class);
	}
}
