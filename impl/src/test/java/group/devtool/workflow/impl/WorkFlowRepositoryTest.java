package group.devtool.workflow.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import group.devtool.workflow.core.exception.WorkFlowException;
import org.junit.Assert;
import org.junit.Test;

public class WorkFlowRepositoryTest extends WorkFlowBeforeTest {

  @Test
  public void testBulkSaveVariable() {

    List<WorkFlowVariableEntity> entities = new ArrayList<>();
    entities.add(buildVariableEntity("test"));
    try {
      dbConfig.dbTransaction().doInTransaction(() -> {
        dbConfig.repository().bulkSaveVariable(entities);
        Assert.assertNotNull(entities.get(0).getId());

        List<WorkFlowVariableEntity> variables = dbConfig.repository().loadVariable("test");
				Assert.assertEquals(1, variables.size());
        WorkFlowVariableEntity entity = variables.get(0);

        Assert.assertEquals("a", entity.getName());
        Assert.assertEquals("1", deserialize(entity.getValue()));
        return true;
      });

    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testSaveAndGetInstance() {
    WorkFlowInstanceEntity entity = new WorkFlowInstanceEntity();
    entity.setDefinitionCode("test");
    entity.setDefinitionVersion(1);
    entity.setInstanceId("test");
    entity.setRootInstanceId("test");
    entity.setState("DOING");
		dbConfig.dbTransaction().doInTransaction(() -> {
			dbConfig.repository().save(entity);
			Assert.assertNotNull(entity.getId());

			WorkFlowInstanceEntity insert = dbConfig.repository().getInstance("test", "test");
			Assert.assertEquals(entity.getInstanceId(), insert.getInstanceId());
			Assert.assertEquals(entity.getDefinitionCode(), insert.getDefinitionCode());
			Assert.assertEquals(entity.getRootInstanceId(), insert.getRootInstanceId());

			insert.setState("DONE");
			dbConfig.repository().save(insert);
			WorkFlowInstanceEntity update = dbConfig.repository().getInstance("test", "test");
			Assert.assertEquals(insert.getInstanceId(), update.getInstanceId());
			Assert.assertEquals(insert.getDefinitionCode(), update.getDefinitionCode());
			Assert.assertEquals(insert.getRootInstanceId(), update.getRootInstanceId());
			Assert.assertEquals(insert.getState(), update.getState());

			return true;
		});
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
    task.setNodeClass("user");
    task.setNodeState("DOING");
    task.setInstanceId("instanceId1");
    task.setRootInstanceId("instanceId1");
    tasks.add(task);
		dbConfig.dbTransaction().doInTransaction(() -> {

			dbConfig.repository().bulkSaveTask(tasks);
			Assert.assertNotNull(task.getId());

			List<WorkFlowTaskEntity> inserts = dbConfig.repository().getTaskByCode("nodeCode1", "instanceId1");
			Assert.assertEquals(1, inserts.size());

			Assert.assertEquals(task.getInstanceId(), inserts.get(0).getInstanceId());
			Assert.assertEquals(task.getRootInstanceId(), inserts.get(0).getRootInstanceId());
			Assert.assertEquals(task.getTaskId(), inserts.get(0).getTaskId());
			Assert.assertEquals(task.getTaskClass(), inserts.get(0).getTaskClass());
			Assert.assertEquals(task.getTaskState(), inserts.get(0).getTaskState());
			Assert.assertEquals(task.getNode().getNodeCode(), inserts.get(0).getNode().getNodeCode());
			Assert.assertEquals(task.getNode().getNodeClass(), inserts.get(0).getNode().getNodeClass());
			Assert.assertEquals(task.getNode().getNodeState(), inserts.get(0).getNode().getNodeState());

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
    task.setNodeCode("nodeCode2");
    task.setNodeClass("user");
    task.setNodeState("DOING");
    task.setInstanceId("instanceId2");
    task.setRootInstanceId("instanceId2");
    tasks.add(task);
    try {
      dbConfig.dbTransaction().doInTransaction(() -> {

        dbConfig.repository().bulkSaveTask(tasks);
        dbConfig.repository().changeTaskComplete("admin", System.currentTimeMillis(), "taskId2", "instanceId2");
        WorkFlowTaskEntity save = dbConfig.repository().getTaskById("taskId2", "instanceId2");
        Assert.assertNull(save);
        return true;
      });
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testChangeNodeComplete() {
    List<WorkFlowTaskEntity> tasks = new ArrayList<>();
    WorkFlowTaskEntity task = new WorkFlowTaskEntity();
    task.setTaskId("taskId3");
    task.setTaskClass("user");
    task.setTaskState("DOING");
    task.setConfig(serialize("user"));
    task.setNodeCode("nodeCode3");
    task.setNodeClass("user");
    task.setNodeState("DOING");
    task.setInstanceId("instanceId3");
    task.setRootInstanceId("instanceId3");
    tasks.add(task);
		dbConfig.dbTransaction().doInTransaction(() -> {

			dbConfig.repository().bulkSaveTask(tasks);

			dbConfig.repository().changeNodeComplete("nodeCode3", "instanceId3");
			List<WorkFlowTaskEntity> change = dbConfig.repository().getTaskByCode("nodeCode3", "instanceId3");

			Assert.assertEquals(0, change.size());

			return true;
		});
	}

  private WorkFlowVariableEntity buildVariableEntity(String instanceId) {
    WorkFlowVariableEntity entity = new WorkFlowVariableEntity();
    entity.setRootInstanceId(instanceId);
    entity.setName("a");
    entity.setValue(serialize("1"));
    entity.setNode("a");
    return entity;
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
