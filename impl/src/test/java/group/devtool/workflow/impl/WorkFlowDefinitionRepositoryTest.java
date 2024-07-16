/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import group.devtool.workflow.engine.exception.WorkFlowException;
import group.devtool.workflow.impl.entity.WorkFlowDefinitionEntity;
import group.devtool.workflow.impl.entity.WorkFlowLinkDefinitionEntity;
import group.devtool.workflow.impl.entity.WorkFlowNodeDefinitionEntity;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;


public class WorkFlowDefinitionRepositoryTest extends InitWorkFlowConfig {

  @Test
  public void testBulkSave() throws WorkFlowException {
    List<WorkFlowDefinitionEntity> entities = new ArrayList<>();
    WorkFlowDefinitionEntity entity = new WorkFlowDefinitionEntity();
    entity.setCode("test");
    entity.setName("test");
    entity.setVersion(1);
    entity.setState("Y");
    entity.setRootCode("test");
    entities.add(entity);
		dbConfig.dbTransaction().doInTransaction(() -> {
			dbConfig.definitionRepository().bulkSave(entities);
			System.out.println("id:" + entities.get(0).getId());
			return true;
		});
		Assert.assertNotNull(entity.getId());
	}

  @Test
  public void testLoadDefinition() throws WorkFlowException {
		List<WorkFlowDefinitionEntity> entities = getWorkFlowDefinitionEntities();
		dbConfig.dbTransaction().doInTransaction(() -> {
			dbConfig.definitionRepository().bulkSave(entities);

			List<WorkFlowDefinitionEntity> all = dbConfig.definitionRepository().loadDefinition("test1", "test1", 1, true);
			Assert.assertEquals(2, all.size());

			List<WorkFlowDefinitionEntity> child = dbConfig.definitionRepository().loadDefinition("test1", "test1", 1, false);
			Assert.assertEquals(1, child.size());
			return true;
		});
	}

	private static List<WorkFlowDefinitionEntity> getWorkFlowDefinitionEntities() {
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
		return entities;
	}

	@Test
  public void testLoadDeployedDefinition() throws WorkFlowException {
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
			WorkFlowDefinitionEntity definition = dbConfig.definitionRepository().loadDeployedDefinition("deployed", "deployed");
			Assert.assertNull(definition);
			dbConfig.definitionRepository().undeploy("deployed", "Y", "N");
			definition = dbConfig.definitionRepository().loadDeployedDefinition("deployed", "deployed");
			Assert.assertNotNull(definition);
			Assert.assertEquals("deployed", definition.getCode());
			return true;
		});

	}

  @Test
  public void testLoadDefinitionVersion() throws WorkFlowException {
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
			Integer version = dbConfig.definitionRepository().loadDefinitionLatestVersion("latest", "latest");
			Assert.assertEquals(Integer.valueOf(1), version);

			Integer version2 = dbConfig.definitionRepository().loadDefinitionLatestVersion("latest2", "latest");
			Assert.assertNull(version2);
			return true;
		});

	}

  @Test
  public void testLoadNodeDefinition() throws WorkFlowException {
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
							"start", 1, false);
			Assert.assertNotNull(entities.get(0).getId());
			Assert.assertEquals("start", entities.get(0).getCode());
			return true;
		});
	}

  @Test
  public void testLoadLinkDefinition() throws WorkFlowException {
    List<WorkFlowLinkDefinitionEntity> links = new ArrayList<>();
    WorkFlowLinkDefinitionEntity link = new WorkFlowLinkDefinitionEntity();
		link.setCode("start");
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
							"start", 1, false);
			Assert.assertNotNull(entities.get(0).getId());
			Assert.assertEquals("start", entities.get(0).getSource());
			return true;
		});
	}

  @After
  public void after() throws IOException {
    // dbConfig.taskScheduler().close();
  }
}
