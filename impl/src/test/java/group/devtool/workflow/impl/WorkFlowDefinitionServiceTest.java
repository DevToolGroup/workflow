package group.devtool.workflow.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import group.devtool.workflow.core.exception.WorkFlowDefinitionException;
import org.junit.Assert;
import org.junit.Test;

import group.devtool.workflow.core.ChildWorkFlowNodeDefinition;
import group.devtool.workflow.core.UserWorkFlowNodeDefinition;
import group.devtool.workflow.core.WorkFlowContext;
import group.devtool.workflow.core.WorkFlowDefinition;
import group.devtool.workflow.core.WorkFlowDefinitionService;
import group.devtool.workflow.core.WorkFlowLinkDefinition;
import group.devtool.workflow.core.WorkFlowNodeDefinition;
import group.devtool.workflow.core.WorkFlowVariable;
import group.devtool.workflow.core.UserWorkFlowNodeDefinition.WorkFlowUserConfig;
import group.devtool.workflow.impl.ChildWorkFlowNodeDefinitionImpl.MybatisWorkFlowChildConfig;
import group.devtool.workflow.impl.UserWorkFlowNodeDefinitionImpl.WorkFlowUserConfigImpl;

public class WorkFlowDefinitionServiceTest  extends WorkFlowBeforeTest {
  
  @Test
  public void testLoad() {
    WorkFlowDefinitionRepository repository = mock(WorkFlowDefinitionRepository.class);
    dbConfig.setDefinitionRepository(repository);
    try {
      WorkFlowDefinitionImpl definition = getDefinition();
      WorkFlowDefinitionService service = dbConfig.definitionService();
      Method method = service.getClass().getDeclaredMethod("toEntity", WorkFlowDefinition.class);
      method.setAccessible(true);
      @SuppressWarnings("unchecked")
      List<WorkFlowDefinitionEntity> entities = (List<WorkFlowDefinitionEntity>) method.invoke(service,
          definition);

      List<WorkFlowLinkDefinitionEntity> links = new ArrayList<>();
      List<WorkFlowNodeDefinitionEntity> nodes = new ArrayList<>();

      Method reduce = service.getClass().getDeclaredMethod("reduce", List.class, List.class, List.class, Integer.class);
      reduce.setAccessible(true);
      reduce.invoke(service, entities, links, nodes, 1);

      entities.stream().forEach(i -> {
        i.setNodes(new ArrayList<>());
        i.setLinks(new ArrayList<>());
      });
      when(repository.loadDefinition("test", 1, true)).thenReturn(entities);
      when(repository.loadNodeDefinition("test", 1, true)).thenReturn(nodes);
      when(repository.loadLinkDefinition("test", 1, true)).thenReturn(links);

      WorkFlowDefinition newDefinition = service.load("test", 1, true);

      Assert.assertEquals("test", newDefinition.code());
      Assert.assertEquals("test", newDefinition.name());
      Assert.assertEquals(Integer.valueOf(1), newDefinition.version());

      WorkFlowNodeDefinition s = newDefinition.start();
      Assert.assertEquals("start", s.getCode());
      Assert.assertEquals("start", s.getName());

      WorkFlowNodeDefinition e = newDefinition.end();
      Assert.assertEquals("end", e.getCode());
      Assert.assertEquals("end", e.getName());

      WorkFlowContext context = new WorkFlowContext("test");

      List<WorkFlowNodeDefinition> nu1 = newDefinition.next("start", context);
      WorkFlowNodeDefinition u1 = nu1.get(0);
      Assert.assertEquals("u1", u1.getCode());
      Assert.assertEquals("u1", u1.getName());
      Assert.assertTrue(u1 instanceof UserWorkFlowNodeDefinition);
      UserWorkFlowNodeDefinition ud1 = (UserWorkFlowNodeDefinition) u1;
      Assert.assertEquals(Integer.valueOf(2), ud1.getConfig().confirm());
      Assert.assertTrue(ud1.getConfig().member().contains("u1"));
      Assert.assertTrue(ud1.getConfig().member().contains("u2"));

      context.add(WorkFlowVariable.bound("a", 3, "u1", "u1"));
      List<WorkFlowNodeDefinition> nu2 = newDefinition.next("u1", context);
      WorkFlowNodeDefinition u2 = nu2.get(0);
      Assert.assertEquals("u2", u2.getCode());
      Assert.assertEquals("u2", u2.getName());
      Assert.assertTrue(u2 instanceof UserWorkFlowNodeDefinition);
      UserWorkFlowNodeDefinition ud2 = (UserWorkFlowNodeDefinition) u2;
      Assert.assertTrue(ud2.getConfig().member().contains("u1"));
      Assert.assertTrue(ud2.getConfig().member().contains("u2"));

      List<WorkFlowNodeDefinition> nc = newDefinition.next("u2", context);
      WorkFlowNodeDefinition uc1 = nc.get(0);
      Assert.assertTrue(uc1 instanceof ChildWorkFlowNodeDefinition);
      Assert.assertEquals("child", uc1.getCode());
      Assert.assertEquals("child", uc1.getName());

      ChildWorkFlowNodeDefinition udc1 = (ChildWorkFlowNodeDefinition) uc1;
      Assert.assertTrue(udc1.getChild() instanceof WorkFlowDefinition);

      WorkFlowDefinition udcc = udc1.getChild();
      Assert.assertEquals("cc", udcc.code());
      Assert.assertEquals("cc", udcc.name());
      WorkFlowNodeDefinition ccs = udcc.start();
      Assert.assertEquals("start1", ccs.getCode());
      Assert.assertEquals("start1", ccs.getName());

      WorkFlowNodeDefinition cce = udcc.start();
      Assert.assertEquals("start1", cce.getCode());
      Assert.assertEquals("start1", cce.getName());

      List<WorkFlowNodeDefinition> ccnc = udcc.next("start1", context);
      WorkFlowNodeDefinition ccu = ccnc.get(0);
      Assert.assertEquals("cu1", ccu.getCode());
      Assert.assertEquals("cu1", ccu.getName());
      Assert.assertTrue(ccu instanceof UserWorkFlowNodeDefinition);
      UserWorkFlowNodeDefinition ccud = (UserWorkFlowNodeDefinition) ccu;
      Assert.assertTrue(ccud.getConfig().member().contains("cu1"));

      List<WorkFlowNodeDefinition> ccne = udcc.next("cu1", context);
      WorkFlowNodeDefinition ccce = ccne.get(0);
      Assert.assertEquals("end1", ccce.getCode());
      Assert.assertEquals("end1", ccce.getName());

      context.add(WorkFlowVariable.bound("a", 4, "cc", "cc"));
      List<WorkFlowNodeDefinition> ne = newDefinition.next("cc", context);
      WorkFlowNodeDefinition ne1 = ne.get(0);

      Assert.assertEquals("end", ne1.getCode());
      Assert.assertEquals("end", ne1.getName());

    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testReduce() {
    WorkFlowDefinitionRepository repository = mock(WorkFlowDefinitionRepository.class);
    dbConfig.setDefinitionRepository(repository);
    WorkFlowDefinitionService service = dbConfig.definitionService();
    try {
      WorkFlowDefinitionImpl definition = getDefinition();

      Method method = service.getClass().getDeclaredMethod("toEntity", WorkFlowDefinition.class);
      method.setAccessible(true);

      @SuppressWarnings("unchecked")
      List<WorkFlowDefinitionEntity> entities = (List<WorkFlowDefinitionEntity>) method.invoke(service,
          definition);

      List<WorkFlowLinkDefinitionEntity> links = new ArrayList<>();
      List<WorkFlowNodeDefinitionEntity> nodes = new ArrayList<>();

      Method reduce = service.getClass().getDeclaredMethod("reduce", List.class, List.class, List.class, Integer.class);
      reduce.setAccessible(true);
      reduce.invoke(service, entities, links, nodes, 1);

			Assert.assertEquals(8, nodes.size());
			Assert.assertEquals(7, links.size());

      Assert.assertTrue(entities.stream().allMatch(i -> i.getVersion() == 1));
      Assert.assertTrue(nodes.stream().allMatch(i -> i.getVersion() == 1));
      Assert.assertTrue(links.stream().allMatch(i -> i.getVersion() == 1));

    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }

  }

  @Test
  public void testToEntity() throws Exception {
    WorkFlowDefinitionRepository repository = mock(WorkFlowDefinitionRepository.class);

    dbConfig.setDefinitionRepository(repository);
    WorkFlowDefinitionService service = dbConfig.definitionService();
    try {
      WorkFlowDefinitionImpl definition = getDefinition();
      Method method = service.getClass().getDeclaredMethod("toEntity", WorkFlowDefinition.class);
      method.setAccessible(true);

      @SuppressWarnings("unchecked")
      List<WorkFlowDefinitionEntity> entities = (List<WorkFlowDefinitionEntity>) method.invoke(service,
          definition);
      Assert.assertEquals(2, entities.size());

      // 子流程校验
      WorkFlowDefinitionEntity ce = entities.stream().filter(i -> null != i.getNodeCode()).findFirst().get();
      Assert.assertEquals("cc", ce.getName());
      Assert.assertEquals("cc", ce.getCode());
      Assert.assertEquals("child", ce.getNodeCode());
      Assert.assertEquals("test", ce.getRootCode());
      Assert.assertEquals(Integer.valueOf(0), ce.getVersion());

      List<WorkFlowNodeDefinitionEntity> cn = ce.getNodes();
      Assert.assertTrue(cn.stream().allMatch(i -> i.getDefinitionCode().equals("cc")));
      Assert.assertTrue(cn.stream().allMatch(i -> i.getRootDefinitionCode().equals("test")));
      Assert.assertTrue(cn.stream().allMatch(i -> i.getVersion().equals(0)));
			Assert.assertEquals(3, cn.size());

      WorkFlowNodeDefinitionEntity cs = cn.get(0);
      Assert.assertEquals("start1", cs.getCode());
      Assert.assertEquals("start1", cs.getName());
      Assert.assertEquals("START", cs.getType());
      Assert.assertEquals("cc", cs.getDefinitionCode());
      Assert.assertEquals("test", cs.getRootDefinitionCode());
      Assert.assertEquals(Integer.valueOf(0), cs.getVersion());
			Assert.assertNull(cs.getConfig());

      WorkFlowNodeDefinitionEntity cu = cn.get(1);
      Assert.assertEquals("cu1", cu.getCode());
      Assert.assertEquals("cu1", cu.getName());
      Assert.assertEquals("USER", cu.getType());
      Assert.assertEquals("cc", cu.getDefinitionCode());
      Assert.assertEquals("test", cu.getRootDefinitionCode());
      Assert.assertEquals(Integer.valueOf(0), cu.getVersion());
      Object obj = deserialize(cu.getConfig());
      Assert.assertTrue(obj instanceof WorkFlowUserConfigImpl);
      WorkFlowUserConfigImpl uc = (WorkFlowUserConfigImpl) obj;
      Assert.assertEquals("cu1", uc.member().get(0));
      Assert.assertEquals(Integer.valueOf(1), uc.confirm());

      WorkFlowNodeDefinitionEntity cce = cn.get(2);
      Assert.assertEquals("end1", cce.getCode());
      Assert.assertEquals("end1", cce.getName());
      Assert.assertEquals("END", cce.getType());
      Assert.assertEquals("cc", cce.getDefinitionCode());
      Assert.assertEquals("test", cce.getRootDefinitionCode());
      Assert.assertEquals(Integer.valueOf(0), cce.getVersion());

      List<WorkFlowLinkDefinitionEntity> cl = ce.getLinks();
      Assert.assertTrue(cl.stream().allMatch(i -> i.getDefinitionCode().equals("cc")));

      WorkFlowLinkDefinitionEntity cl1 = cl.get(0);
      Assert.assertEquals("start1", cl1.getSource());
      Assert.assertEquals("cu1", cl1.getTarget());
      Assert.assertEquals("true", cl1.getExpression());
      Assert.assertEquals("test", cl1.getRootDefinitionCode());
      Assert.assertEquals(Integer.valueOf(0), cl1.getVersion());

      WorkFlowLinkDefinitionEntity cl2 = cl.get(1);
      Assert.assertEquals("cu1", cl2.getSource());
      Assert.assertEquals("end1", cl2.getTarget());
      Assert.assertEquals("true", cl2.getExpression());
      Assert.assertEquals("test", cl2.getRootDefinitionCode());
      Assert.assertEquals(Integer.valueOf(0), cl2.getVersion());

      WorkFlowDefinitionEntity pe = entities.stream().filter(i -> null == i.getNodeCode()).findFirst().get();
      Assert.assertEquals("test", pe.getName());
      Assert.assertEquals("test", pe.getCode());

      List<WorkFlowNodeDefinitionEntity> pn = pe.getNodes();
      Assert.assertTrue(pn.stream().allMatch(i -> i.getDefinitionCode().equals("test")));

      List<WorkFlowLinkDefinitionEntity> pl = pe.getLinks();
      Assert.assertTrue(pl.stream().allMatch(i -> i.getDefinitionCode().equals("test")));

    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testDefinitionNodeUniqueError() throws WorkFlowDefinitionException {
    WorkFlowDefinitionRepository repository = mock(WorkFlowDefinitionRepository.class);

    dbConfig.setDefinitionRepository(repository);
    WorkFlowDefinitionService service = dbConfig.definitionService();
    List<WorkFlowLinkDefinition> links = new ArrayList<>();
    List<WorkFlowNodeDefinition> nodes = new ArrayList<>();

		nodes.add(new EndWorkFlowNodeDefinitionImpl("end", "end"));
		nodes.add(new EndWorkFlowNodeDefinitionImpl("end", "end"));
		links.add(new SPELWorkFlowLinkDefinitionImpl("start", "end", ""));
		WorkFlowDefinitionImpl definition = new WorkFlowDefinitionImpl("test", "test", 1, nodes, links);
		Assert.assertThrows(WorkFlowDefinitionException.class, () -> service.deploy(definition));

	}

  @Test
  public void testDefinitionNodeNestUniqueError() throws WorkFlowDefinitionException {
    WorkFlowDefinitionRepository repository = mock(WorkFlowDefinitionRepository.class);

    dbConfig.setDefinitionRepository(repository);
    WorkFlowDefinitionService service = dbConfig.definitionService();
    List<WorkFlowLinkDefinition> links = new ArrayList<>();
    List<WorkFlowNodeDefinition> nodes = new ArrayList<>();

		List<WorkFlowLinkDefinition> clinks = new ArrayList<>();
		List<WorkFlowNodeDefinition> cnodes = new ArrayList<>();
		cnodes.add(new StartWorkFlowNodeDefinitionImpl("start1", "start"));
		cnodes.add(new EndWorkFlowNodeDefinitionImpl("end1", "end"));
		clinks.add(new SPELWorkFlowLinkDefinitionImpl("start1", "end1", ""));

		WorkFlowDefinitionImpl childDef = new WorkFlowDefinitionImpl("child", "child", 1, cnodes, clinks);
		MybatisWorkFlowChildConfig config = new MybatisWorkFlowChildConfig(1, "child");
		ChildWorkFlowNodeDefinitionImpl child = new ChildWorkFlowNodeDefinitionImpl("child", "child", config,
				childDef);
		nodes.add(new StartWorkFlowNodeDefinitionImpl("start", "start"));
		nodes.add(child);
		nodes.add(new EndWorkFlowNodeDefinitionImpl("end", "end"));
		links.add(new SPELWorkFlowLinkDefinitionImpl("start", "end", ""));

		WorkFlowDefinitionImpl definition = new WorkFlowDefinitionImpl("test", "test", 1, nodes, links);
		Assert.assertThrows(WorkFlowDefinitionException.class,
				() -> service.deploy(definition));
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