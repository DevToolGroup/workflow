package group.devtool.workflow.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import group.devtool.workflow.core.exception.WorkFlowException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;


public class WorkFlowDefinitionRepositoryTest extends WorkFlowBeforeTest {

  @Test
  public void testBulkSave() {
    List<WorkFlowDefinitionEntity> entities = new ArrayList<>();
    WorkFlowDefinitionEntity entity = new WorkFlowDefinitionEntity();
    entity.setCode("test");
    entity.setName("test");
    entity.setVersion(1);
    entity.setState("Y");
    entity.setRootCode("test");
    entities.add(entity);
		WorkFlowConfigurationImpl.CONFIG.dbTransaction().doInTransaction(() -> {
			dbConfig.definitionRepository().bulkSave(entities);
			return true;
		});
		Assert.assertNotNull(entity.getId());
	}

  @Test
  public void testLoadDefinition() {
		List<WorkFlowDefinitionEntity> entities = new ArrayList<>();
		WorkFlowDefinitionEntity entity = new WorkFlowDefinitionEntity();
		entity.setCode("test1");
		entity.setName("test1");
		entity.setVersion(1);
		entity.setState("Y");
		entity.setRootCode("test1");
		entities.add(entity);
		WorkFlowDefinitionEntity entity2 = new WorkFlowDefinitionEntity();
		entity2.setCode("test2");
		entity2.setName("test2");
		entity2.setVersion(1);
		entity2.setState("Y");
		entity2.setNodeCode("cc");
		entity2.setRootCode("test1");
		entities.add(entity2);
		dbConfig.dbTransaction().doInTransaction(() -> {
			dbConfig.definitionRepository().bulkSave(entities);

			List<WorkFlowDefinitionEntity> all = dbConfig.definitionRepository().loadDefinition("test1", 1, true);
			Assert.assertEquals(2, all.size());

			List<WorkFlowDefinitionEntity> child = dbConfig.definitionRepository().loadDefinition("test1", 1, false);
			Assert.assertEquals(1, child.size());
			return true;
		});
	}

  @Test
  public void testLoadDeployedDefinition() {
		dbConfig.dbTransaction().doInTransaction(() -> {
			List<WorkFlowDefinitionEntity> entities = new ArrayList<>();
			WorkFlowDefinitionEntity entity = new WorkFlowDefinitionEntity();
			entity.setCode("deployed");
			entity.setName("deployed");
			entity.setVersion(1);
			entity.setState("N");
			entity.setRootCode("deployed");
			entities.add(entity);
			dbConfig.definitionRepository().bulkSave(entities);
			WorkFlowDefinitionEntity definition = dbConfig.definitionRepository().loadDeployedDefinition("deployed");
			Assert.assertNull(definition);
			dbConfig.definitionRepository().changeState("deployed", "Y", "N");
			definition = dbConfig.definitionRepository().loadDeployedDefinition("deployed");
			Assert.assertNotNull(definition);
			Assert.assertEquals("deployed", definition.getCode());
			return true;
		});

	}

  @Test
  public void testLoadDefinitionVersion() {
		dbConfig.dbTransaction().doInTransaction(() -> {
			List<WorkFlowDefinitionEntity> entities = new ArrayList<>();
			WorkFlowDefinitionEntity entity = new WorkFlowDefinitionEntity();
			entity.setCode("latest");
			entity.setName("latest");
			entity.setVersion(1);
			entity.setState("Y");
			entity.setRootCode("latest");
			entities.add(entity);
			dbConfig.definitionRepository().bulkSave(entities);
			Integer version = dbConfig.definitionRepository().loadDefinitionLatestVersion("latest");
			Assert.assertEquals(Integer.valueOf(1), version);

			Integer version2 = dbConfig.definitionRepository().loadDefinitionLatestVersion("latest2");
			Assert.assertNull(version2);
			return true;
		});

	}

  @Test
  public void testLoadNodeDefinition() {
    List<WorkFlowNodeDefinitionEntity> nodes = new ArrayList<>();
    WorkFlowNodeDefinitionEntity node = new WorkFlowNodeDefinitionEntity();
    node.setName("start");
    node.setCode("start");
    node.setDefinitionCode("start");
    node.setRootDefinitionCode("start");
    node.setType("START");
    node.setVersion(1);
    nodes.add(node);
		dbConfig.dbTransaction().doInTransaction(() -> {
			dbConfig.definitionRepository().bulkSaveNode(nodes);
			List<WorkFlowNodeDefinitionEntity> entities = dbConfig.definitionRepository().loadNodeDefinition("start",
					1, false);
			Assert.assertNotNull(entities.get(0).getId());
			Assert.assertEquals("start", entities.get(0).getCode());
			return true;
		});
	}

  @Test
  public void testLoadLinkDefinition() {
    List<WorkFlowLinkDefinitionEntity> links = new ArrayList<>();
    WorkFlowLinkDefinitionEntity link = new WorkFlowLinkDefinitionEntity();
    link.setSource("start");
    link.setTarget("start");
    link.setExpression("START");
    link.setParser("EPEL");
    link.setDefinitionCode("start");
    link.setRootDefinitionCode("start");
    link.setVersion(1);
    links.add(link);
		dbConfig.dbTransaction().doInTransaction(() -> {
			dbConfig.definitionRepository().bulkSaveLink(links);
			List<WorkFlowLinkDefinitionEntity> entities = dbConfig.definitionRepository().loadLinkDefinition("start",
					1, false);
			Assert.assertNotNull(entities.get(0).getId());
			Assert.assertEquals("start", entities.get(0).getSource());
			return true;
		});
	}

  @After
  public void after() throws IOException {
    System.out.println("after");
    System.out.println(System.currentTimeMillis());
    dbConfig.taskScheduler().close();
  }
}
