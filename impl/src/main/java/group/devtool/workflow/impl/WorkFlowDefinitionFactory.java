package group.devtool.workflow.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import group.devtool.workflow.core.WorkFlowDefinition;
import group.devtool.workflow.core.WorkFlowLinkDefinition;
import group.devtool.workflow.core.WorkFlowNodeDefinition;
import group.devtool.workflow.core.ChildWorkFlowNodeDefinition.WorkFlowChildConfig;
import group.devtool.workflow.core.DelayWorkFlowNodeDefinition.DelayTaskConfig;
import group.devtool.workflow.core.MergeWorkFlowNodeDefinition.WorkFlowMergeStrategy;
import group.devtool.workflow.core.TaskWorkFlowNodeDefinition.WorkFlowTaskConfig;
import group.devtool.workflow.core.UserWorkFlowNodeDefinition.WorkFlowUserConfig;
import group.devtool.workflow.core.exception.CastWorkFlowClassException;
import group.devtool.workflow.core.exception.DeserializeException;
import group.devtool.workflow.core.exception.WorkFlowException;

public class WorkFlowDefinitionFactory {

  public static WorkFlowDefinitionFactory DEFINITION = new WorkFlowDefinitionFactory();

  enum LinkFactory {
    SPEL((entity) -> new SPELWorkFlowLinkDefinitionImpl(entity));

		private final LinkFunction init;

    LinkFactory(LinkFunction init) {
      this.init = init;
    }

    public WorkFlowLinkDefinition apply(WorkFlowLinkDefinitionEntity entity) throws WorkFlowException {
      return init.apply(entity);
    }
  }

  enum NodeFactory {
    // 开始节点
    START((code, name, config, objs) -> new StartWorkFlowNodeDefinitionImpl(code, name)),

    // 用户节点
    USER((code, name, config, objs) -> new UserWorkFlowNodeDefinitionImpl(code, name,
        deserialize(config, WorkFlowUserConfig.class))),

    // 任务节点
    TASK((code, name, config, objs) -> new TaskWorkFlowNodeDefinitionImpl(code, name,
        deserialize(config, WorkFlowTaskConfig.class))),

    // 嵌套子流程节点，运行态下子流程定义为空
    CHILD((code, name, config, objs) -> new ChildWorkFlowNodeDefinitionImpl(code, name,
        deserialize(config, WorkFlowChildConfig.class), objs.length > 0 ? (WorkFlowDefinition) objs[0]: null)),

    // 延时节点
    DELAY((code, name, config, objs) -> new DelayWorkFlowNodeDefinitionImpl(code, name,
        deserialize(config, DelayTaskConfig.class))),

    // 汇聚节点
    MERGE((code, name, config, objs) -> new MergeWorkFlowNodeDefinitionImpl(code, name,
        deserialize(config, WorkFlowMergeStrategy.class))),

    // 结束节点
    END((code, name, config, objs) -> new EndWorkFlowNodeDefinitionImpl(code, name)),
    ;

    private final NodeFunction init;

    NodeFactory(NodeFunction init) {
      this.init = init;
    }

    private static <T> T deserialize(byte[] config, Class<? extends T> clazz) throws WorkFlowException {
      try (ByteArrayInputStream is = new ByteArrayInputStream(config);
          ObjectInputStream ois = new ObjectInputStream(is)) {
        Object oo = ois.readObject();
        if (!(clazz.isAssignableFrom(oo.getClass()))) {
          throw new CastWorkFlowClassException("节点定义配置类型不匹配，预期类型：" + clazz.getSimpleName());
        }
        return clazz.cast(oo);

      } catch (IOException | ClassNotFoundException e) {
        throw new DeserializeException(String.format("解析节点定义异常，%s", e.getMessage()));
      }
    }

    public WorkFlowNodeDefinition apply(String code, String name, byte[] config, Object... others)
        throws WorkFlowException {
      return init.apply(code, name, config, others);
    }
  }

  public WorkFlowDefinition factory(WorkFlowDefinitionEntity de,
                                    Map<String, WorkFlowDefinitionEntity> nem) throws WorkFlowException {
    WorkFlowDefinitionEntity mde = de;
    return new WorkFlowDefinitionImpl(mde.getCode(),
        mde.getName(),
        mde.getVersion(),
        nodeFactory(mde.getNodes(), nem),
        linkFactory(mde.getLinks()));
  }

  private List<WorkFlowLinkDefinition> linkFactory(List<? extends WorkFlowLinkDefinitionEntity> lde)
      throws WorkFlowException {
    List<WorkFlowLinkDefinition> links = new ArrayList<>(lde.size());
    for (WorkFlowLinkDefinitionEntity entity : lde) {
      links.add(LinkFactory.valueOf(entity.getParser().toUpperCase()).apply(entity));
    }
    return links;
  }

  private List<WorkFlowNodeDefinition> nodeFactory(List<? extends WorkFlowNodeDefinitionEntity> nde,
      Map<String, WorkFlowDefinitionEntity> nem)
      throws WorkFlowException {
    List<WorkFlowNodeDefinition> nodes = new ArrayList<>(nde.size());
    for (WorkFlowNodeDefinitionEntity entity : nde) {
      WorkFlowDefinitionEntity child = nem.get(entity.getCode());
      if (null == child) {
        nodes.add(NodeFactory.valueOf(entity.getType()).apply(entity.getCode(), entity.getName(), entity.getConfig()));
      } else {
        WorkFlowDefinition childDefinition = factory(child, nem);
        nodes.add(NodeFactory.valueOf(entity.getType()).apply(entity.getCode(), entity.getName(), entity.getConfig(),
            childDefinition));
      }
    }
    return nodes;
  }

  public interface NodeFunction {

    WorkFlowNodeDefinition apply(String code, String name, byte[] config, Object... others) throws WorkFlowException;

  }

  public interface LinkFunction {

    WorkFlowLinkDefinition apply(WorkFlowLinkDefinitionEntity entity) throws WorkFlowException;

  }
}
