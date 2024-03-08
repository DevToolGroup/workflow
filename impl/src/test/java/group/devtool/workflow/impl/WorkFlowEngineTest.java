package group.devtool.workflow.impl;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import group.devtool.workflow.core.exception.WorkFlowDefinitionException;
import group.devtool.workflow.core.exception.WorkFlowException;
import group.devtool.workflow.core.exception.NotFoundWorkFlowInstance;
import group.devtool.workflow.core.exception.NotUserTaskPermission;
import org.junit.Assert;
import org.junit.Test;

import group.devtool.workflow.core.AbstractChildWorkFlowNode;
import group.devtool.workflow.core.AbstractWorkFlowNode;
import group.devtool.workflow.core.ChildWorkFlowTask;
import group.devtool.workflow.core.UserWorkFlowNodeDefinition.WorkFlowUserConfig;
import group.devtool.workflow.core.WorkFlowDefinition;
import group.devtool.workflow.core.WorkFlowEngine;
import group.devtool.workflow.core.WorkFlowInstance;
import group.devtool.workflow.core.WorkFlowLinkDefinition;
import group.devtool.workflow.core.WorkFlowNode;
import group.devtool.workflow.core.WorkFlowNodeDefinition;
import group.devtool.workflow.core.WorkFlowService;
import group.devtool.workflow.core.WorkFlowTask;
import group.devtool.workflow.core.WorkFlowVariable;
import group.devtool.workflow.impl.ChildWorkFlowNodeDefinitionImpl.MybatisWorkFlowChildConfig;
import group.devtool.workflow.impl.UserWorkFlowNodeDefinitionImpl.WorkFlowUserConfigImpl;

public class WorkFlowEngineTest extends WorkFlowBeforeTest {
  
  @Test
  public void testDeploy() throws WorkFlowException {
    WorkFlowEngine engine = new WorkFlowEngine(dbConfig);
		engine.deploy(getDefinition(1));
	}

  @Test
  public void testUnDeploy() throws WorkFlowException {
    WorkFlowEngine engine = new WorkFlowEngine(dbConfig);
		engine.deploy(getDefinition(1));
		engine.undeploy("engine");
		WorkFlowDefinition definition = engine.load("engine", 1);
		Assert.assertNotNull(definition);
	}

  @Test
  public void testRunEngine() {
    try {
      WorkFlowEngine engine = new WorkFlowEngine(dbConfig);
      engine.deploy(getDefinition(1));
      String instanceId = engine.start("engine", WorkFlowVariable.suspend(WorkFlowVariable.USER, "admin"));
      Assert.assertNotNull(instanceId);

      WorkFlowServiceImpl service = new WorkFlowServiceImpl();

      WorkFlowNode u1 = service.getNode("engine_u1", instanceId);

      Assert.assertNotNull(u1);
      Assert.assertFalse(u1.done());
      AbstractWorkFlowNode an = (AbstractWorkFlowNode) u1;
      WorkFlowTask[] tasks = an.getTasks();
      Assert.assertEquals(2, tasks.length);

      // 校验用户
      Assert.assertThrows(NotUserTaskPermission.class, () -> engine.run(instanceId, tasks[1].getTaskId()));

      // 节点u1执行
      engine.run(instanceId, tasks[0].getTaskId(), WorkFlowVariable.suspend(WorkFlowVariable.USER, "engine_u1"));
      u1 = service.getNode("engine_u1", instanceId);
      Assert.assertNotNull(u1);
      Assert.assertFalse(u1.done());

      engine.run(instanceId, tasks[1].getTaskId(), WorkFlowVariable.suspend(WorkFlowVariable.USER, "engine_u2"),
          WorkFlowVariable.suspend("a", 3));

      // 节点u2判断
      WorkFlowNode u2 = service.getNode("engine_u2", instanceId);
      Assert.assertNotNull(u2);
      Assert.assertFalse(u2.done());

      AbstractWorkFlowNode an2 = (AbstractWorkFlowNode) u2;
      WorkFlowTask[] tasks2 = an2.getTasks();
      Assert.assertEquals(2, tasks2.length);

      // 节点u2执行
      // 校验用户
      Assert.assertThrows(NotUserTaskPermission.class, () -> engine.run(instanceId, tasks2[1].getTaskId()));

      engine.run(instanceId, tasks2[0].getTaskId(), WorkFlowVariable.suspend(WorkFlowVariable.USER, "engine_u1"));
      u2 = service.getNode("engine_u2", instanceId);
      Assert.assertNotNull(u2);
      Assert.assertFalse(u2.done());

      engine.run(instanceId, tasks2[1].getTaskId(), WorkFlowVariable.suspend(WorkFlowVariable.USER, "engine_u2"));

      // 嵌套子节点执行
      WorkFlowNode engineChild = service.getNode("engine_child", instanceId);
      Assert.assertNotNull(engineChild);
      Assert.assertFalse(engineChild.done());

      AbstractChildWorkFlowNode child = (AbstractChildWorkFlowNode) engineChild;
      WorkFlowTask[] childTasks = child.getTasks();
      Assert.assertNotNull(childTasks[0]);

      ChildWorkFlowTask childTask = (ChildWorkFlowTask) childTasks[0];
      Assert.assertFalse(childTask.completed());

      WorkFlowNode childU1 = service.getNode("engine_cu1", instanceId);
      Assert.assertNotNull(childU1);
      Assert.assertFalse(childU1.done());

      AbstractWorkFlowNode aChildU1 = (AbstractWorkFlowNode) childU1;
      WorkFlowTask[] aChildTask1 = aChildU1.getTasks();
      Assert.assertEquals(1, aChildTask1.length);

      WorkFlowTask aChildTaskU1 = aChildTask1[0];
      engine.run(instanceId, aChildTaskU1.getTaskId(), WorkFlowVariable.suspend(WorkFlowVariable.USER, "engine_cu1"));

      WorkFlowInstance instance = service.getInstance(instanceId, instanceId);
      Assert.assertTrue(instance.done());

    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testStop() {
    try {
      WorkFlowEngine engine = new WorkFlowEngine(dbConfig);
      engine.deploy(getDefinition(1));
      String instanceId = engine.start("engine", WorkFlowVariable.suspend(WorkFlowVariable.USER, "admin"));

      engine.stop(instanceId);

    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testStopChild() {
    try {
      WorkFlowService service = dbConfig.service();

      WorkFlowEngine engine = new WorkFlowEngine(dbConfig);
      engine.deploy(getDefinition(1));
      String instanceId = engine.start("engine", WorkFlowVariable.suspend(WorkFlowVariable.USER, "admin"));

      WorkFlowNode u1 = service.getNode("engine_u1", instanceId);
      AbstractWorkFlowNode an = (AbstractWorkFlowNode) u1;
      WorkFlowTask[] tasks = an.getTasks();
      engine.run(instanceId, tasks[0].getTaskId(), WorkFlowVariable.suspend(WorkFlowVariable.USER, "engine_u1"));
      engine.run(instanceId, tasks[1].getTaskId(), WorkFlowVariable.suspend(WorkFlowVariable.USER, "engine_u2"),
          WorkFlowVariable.suspend("a", 3));

      WorkFlowNode u2 = service.getNode("engine_u2", instanceId);
      AbstractWorkFlowNode an2 = (AbstractWorkFlowNode) u2;
      WorkFlowTask[] tasks2 = an2.getTasks();
      engine.run(instanceId, tasks2[0].getTaskId(), WorkFlowVariable.suspend(WorkFlowVariable.USER, "engine_u1"));
      engine.run(instanceId, tasks2[1].getTaskId(), WorkFlowVariable.suspend(WorkFlowVariable.USER, "engine_u2"));

      Assert.assertNotNull(instanceId);
      WorkFlowNode childU1 = service.getNode("engine_cu1", instanceId);
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
    WorkFlowService service = dbConfig.service();

    String instanceId = null;
    try {

      WorkFlowEngine engine = new WorkFlowEngine(dbConfig);

      engine.deploy(getDefinition(1));
      instanceId = engine.start("engine", WorkFlowVariable.suspend(WorkFlowVariable.USER, "admin"));

      WorkFlowNode u1 = service.getNode("engine_u1", instanceId);
      AbstractWorkFlowNode an = (AbstractWorkFlowNode) u1;
      WorkFlowTask[] tasks = an.getTasks();
      engine.run(instanceId, tasks[0].getTaskId(), WorkFlowVariable.suspend(WorkFlowVariable.USER, "engine_u1"));
      engine.run(instanceId, tasks[1].getTaskId(), WorkFlowVariable.suspend(WorkFlowVariable.USER, "engine_u2"),
          WorkFlowVariable.suspend("a", 3));

      WorkFlowNode u2 = service.getNode("engine_u2", instanceId);
      AbstractWorkFlowNode an2 = (AbstractWorkFlowNode) u2;
      WorkFlowTask[] tasks2 = an2.getTasks();
      engine.run(instanceId, tasks2[0].getTaskId(), WorkFlowVariable.suspend(WorkFlowVariable.USER, "engine_u1"));

      // 抛出异常
      doThrow(DBMockException.class).when(ms).changeNodeComplete("engine_u2", instanceId);
      engine.run(instanceId, tasks2[1].getTaskId(), WorkFlowVariable.suspend(WorkFlowVariable.USER, "engine_u2"));

    } catch (Exception e) {
      e.printStackTrace();
      if (e instanceof DBMockException) {
        doAssert(repository, instanceId, e);
      } else {
        Assert.fail(e.getMessage());
      }
    }
  }

  private void doAssert(WorkFlowRepository repository, String instanceId, Exception e) {
		List<MybatisWorkFlowTransactionOperationEntity> operations = dbConfig.dbTransaction().doInTransaction(() -> {
			return repository.loadTransactionOperation(instanceId);
		});
		for (MybatisWorkFlowTransactionOperationEntity operation : operations) {
			Assert.assertEquals("DONE", operation.getState());
		}

		List<WorkFlowTaskEntity> tasks = dbConfig.dbTransaction().doInTransaction(() -> {
			return repository.getTaskByCode("engine_u2", instanceId);
		});
		Assert.assertEquals(2, tasks.size());

		for (WorkFlowTaskEntity task : tasks) {
			if ("engine_u1".equals(task.getCompleteUser())) {
				Assert.assertEquals("DONE", task.getTaskState());
			} else {
				Assert.assertEquals("DOING", task.getTaskState());
			}
		}
	}

  private WorkFlowDefinitionImpl getDefinition(int version)
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

    WorkFlowDefinitionImpl definition = new WorkFlowDefinitionImpl("engine", "engine", version, nodes, links);
    return definition;
  }

  private void start(List<WorkFlowNodeDefinition> nodes) throws WorkFlowDefinitionException {
    nodes.add(new StartWorkFlowNodeDefinitionImpl("engine_start", "engine_start"));
  }

  private void u1(List<WorkFlowNodeDefinition> nodes, List<WorkFlowLinkDefinition> links)
      throws WorkFlowDefinitionException {
    WorkFlowUserConfig config = new WorkFlowUserConfigImpl(
        Arrays.asList("engine_u1", "engine_u2"), 2);
    nodes.add(new UserWorkFlowNodeDefinitionImpl("engine_u1", "engine_u1", config));
    links.add(new SPELWorkFlowLinkDefinitionImpl("engine_start", "engine_u1", "true"));
  }

  private void u2(List<WorkFlowNodeDefinition> nodes, List<WorkFlowLinkDefinition> links)
      throws WorkFlowDefinitionException {
    WorkFlowUserConfig config2 = new WorkFlowUserConfigImpl(
        Arrays.asList("engine_u1", "engine_u2"), 2);
    nodes.add(new UserWorkFlowNodeDefinitionImpl("engine_u2", "engine_u2", config2));
    links.add(new SPELWorkFlowLinkDefinitionImpl("engine_u1", "engine_u2", "#a > 2"));
  }

  private void child(List<WorkFlowNodeDefinition> nodes, List<WorkFlowLinkDefinition> links)
      throws WorkFlowDefinitionException {
    List<WorkFlowLinkDefinition> clinks = new ArrayList<>();
    List<WorkFlowNodeDefinition> cnodes = new ArrayList<>();

    cnodes.add(new StartWorkFlowNodeDefinitionImpl("engine_start1", "engine_start1"));
    cnodes.add(
        new UserWorkFlowNodeDefinitionImpl("engine_cu1", "engine_cu1",
            new WorkFlowUserConfigImpl(List.of("engine_cu1"), 1)));
    cnodes.add(new EndWorkFlowNodeDefinitionImpl("engine_end1", "engine_end1"));

    clinks.add(new SPELWorkFlowLinkDefinitionImpl("engine_start1", "engine_cu1", "true"));
    clinks.add(new SPELWorkFlowLinkDefinitionImpl("engine_cu1", "engine_end1", "true"));
    WorkFlowDefinitionImpl childDef = new WorkFlowDefinitionImpl("engine_cc", "engine_cc", 1, cnodes, clinks);
    MybatisWorkFlowChildConfig config3 = new MybatisWorkFlowChildConfig(1, childDef.code());
    ChildWorkFlowNodeDefinitionImpl child = new ChildWorkFlowNodeDefinitionImpl("engine_child", "engine_child",
        config3,
        childDef);
    nodes.add(child);
    links.add(new SPELWorkFlowLinkDefinitionImpl("engine_u2", "engine_child", "true"));
  }

  private void end(List<WorkFlowNodeDefinition> nodes, List<WorkFlowLinkDefinition> links)
      throws WorkFlowDefinitionException {
    nodes.add(new EndWorkFlowNodeDefinitionImpl("engine_end", "engine_end"));
    links.add(new SPELWorkFlowLinkDefinitionImpl("engine_child", "engine_end", "true"));
    links.add(new SPELWorkFlowLinkDefinitionImpl("engine_u1", "engine_end", "#a < 2"));
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

  public static class DBMockException extends RuntimeException {
    public DBMockException() {
      super();
    }
  }
}
