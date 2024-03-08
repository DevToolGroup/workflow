package group.devtool.workflow.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import group.devtool.workflow.core.ChildWorkFlowNodeDefinition;
import group.devtool.workflow.core.WorkFlowDefinition;
import group.devtool.workflow.core.WorkFlowDefinitionService;
import group.devtool.workflow.core.WorkFlowLinkDefinition;
import group.devtool.workflow.core.WorkFlowNodeDefinition;
import group.devtool.workflow.core.exception.*;

/**
 * {@link WorkFlowDefinitionService} 默认实现
 */
public class WorkFlowDefinitionServiceImpl implements WorkFlowDefinitionService {

	private final WorkFlowConfigurationImpl config;

	public WorkFlowDefinitionServiceImpl() {
		this.config = WorkFlowConfigurationImpl.CONFIG;
	}

	private List<WorkFlowDefinitionEntity> toEntity(WorkFlowDefinition definition)
					throws CastWorkFlowClassException, UnknownDefinitionException {
		String rootDefinitionCode = definition.code();
		List<WorkFlowDefinitionEntity> definitions = new ArrayList<>();
		definitions.add(toEntity(null, definition, rootDefinitionCode));

		// 广度遍历子流程
		List<WorkFlowDefinition> stack = new ArrayList<>(List.of(definition));
		do {
			Iterator<WorkFlowDefinition> iter = stack.iterator();
			List<WorkFlowDefinition> children = new ArrayList<>();

			while (iter.hasNext()) {
				WorkFlowDefinition def = iter.next();
				for (WorkFlowNodeDefinition node : ((WorkFlowDefinitionImpl) def).getNodes()) {
					if (!(node instanceof ChildWorkFlowNodeDefinition)) {
						continue;
					}
					WorkFlowDefinition child = ((ChildWorkFlowNodeDefinition) node).getChild();
					children.add(child);
					definitions.add(toEntity(node.getCode(), child, rootDefinitionCode));
				}
				iter.remove();
			}

			stack = children;
		} while (stack.size() > 0);
		return definitions;
	}

	private WorkFlowDefinitionEntity toEntity(String nodeCode, WorkFlowDefinition item, String rootCode)
					throws CastWorkFlowClassException, UnknownDefinitionException {
		WorkFlowDefinitionEntity entity = new WorkFlowDefinitionEntity();
		entity.setCode(item.code());
		entity.setName(item.name());
		entity.setState("Y");
		entity.setVersion(0);
		entity.setRootCode(rootCode);
		entity.setNodeCode(nodeCode);
		entity.setNodes(toNodeEntity(rootCode, item.code(), ((WorkFlowDefinitionImpl) item).getNodes()));
		entity.setLinks(toLinkEntity(rootCode, item.code(), ((WorkFlowDefinitionImpl) item).getLinks()));
		return entity;
	}

	private List<WorkFlowLinkDefinitionEntity> toLinkEntity(String rootDefinitionCode, String definitionCode,
																													List<WorkFlowLinkDefinition> links) throws CastWorkFlowClassException {
		List<WorkFlowLinkDefinitionEntity> result = new ArrayList<>(links.size());
		for (WorkFlowLinkDefinition link : links) {
			result.add(toLinkEntity(rootDefinitionCode, definitionCode, link));
		}
		return result;
	}

	private WorkFlowLinkDefinitionEntity toLinkEntity(String rootDefinitionCode, String definitionCode,
																										WorkFlowLinkDefinition link) throws CastWorkFlowClassException {
		SPELWorkFlowLinkDefinitionImpl linkImpl = (SPELWorkFlowLinkDefinitionImpl) link;
		WorkFlowLinkDefinitionEntity entity = new WorkFlowLinkDefinitionEntity();
		entity.setRootDefinitionCode(rootDefinitionCode);
		entity.setDefinitionCode(definitionCode);
		entity.setVersion(0);
		entity.setSource(linkImpl.getSource());
		entity.setTarget(linkImpl.getTarget());
		entity.setParser(linkImpl.getParser());
		entity.setExpression(linkImpl.getExpression());
		return entity;
	}

	private List<WorkFlowNodeDefinitionEntity> toNodeEntity(String rootDefinitionCode, String definitionCode,
																													List<WorkFlowNodeDefinition> nodes)
					throws UnknownDefinitionException {
		List<WorkFlowNodeDefinitionEntity> result = new ArrayList<>(nodes.size());
		for (WorkFlowNodeDefinition node : nodes) {
			result.add(toNodeEntity(rootDefinitionCode, definitionCode, node));
		}
		return result;
	}

	private WorkFlowNodeDefinitionEntity toNodeEntity(String rootDefinitionCode, String definitionCode,
																										WorkFlowNodeDefinition node) throws UnknownDefinitionException {
		WorkFlowNodeDefinitionEntity entity = new WorkFlowNodeDefinitionEntity();
		entity.setCode(node.getCode());
		entity.setName(node.getName());
		entity.setType(node.getType());
		entity.setRootDefinitionCode(rootDefinitionCode);
		entity.setDefinitionCode(definitionCode);
		entity.setVersion(0);

		if (null == node.getConfig()) {
			return entity;
		}

		try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
				 ObjectOutputStream oo = new ObjectOutputStream(bos)) {
			oo.writeObject(node.getConfig());
			entity.setConfig(bos.toByteArray());
		} catch (IOException e) {
			throw new UnknownDefinitionException(e.getMessage());
		}
		return entity;
	}

	@Override
	public WorkFlowDefinition load(String code, Integer version, Boolean recursion)
					throws WorkFlowException {
		if (null == version) {
			WorkFlowDefinitionEntity deployed = loadDeployedDefinitionInTransaction(code);
			if (null == deployed) {
				throw new NotFoundWorkFlowDefinition(code, null);
			}
			version = deployed.getVersion();
		}
		// 加载父子流程定义
		List<WorkFlowDefinitionEntity> entities = loadDefinitionInTransaction(code, version, recursion);
		if (null == entities) {
			throw new NotFoundWorkFlowDefinition(code, version);
		}

		// 加载父子流程节点定义
		List<WorkFlowNodeDefinitionEntity> nodeEntities = loadNodeDefinitionInTransaction(code, version, recursion);
		if (null == nodeEntities) {
			throw new UnknownDefinitionException("流程定义缺少节点信息");
		}
		Map<String, List<WorkFlowNodeDefinitionEntity>> nem = nodeEntities.stream()
						.collect(Collectors.groupingBy(i -> i.getDefinitionCode()));

		// 加载父子流程连线定义
		List<WorkFlowLinkDefinitionEntity> linkEntities = loadLinkDefinitionInTransaction(code, version, recursion);
		if (null == linkEntities) {
			throw new UnknownDefinitionException("流程定义缺少连接线信息");
		}
		Map<String, List<WorkFlowLinkDefinitionEntity>> lem = linkEntities.stream()
						.collect(Collectors.groupingBy(i -> i.getDefinitionCode()));

		// 构造流程定义
		WorkFlowDefinitionEntity rootEntity = null;
		Map<String, WorkFlowDefinitionEntity> enm = new HashMap<>();
		for (WorkFlowDefinitionEntity entity : entities) {
			entity.setNodes(nem.get(entity.getCode()));
			entity.setLinks(lem.get(entity.getCode()));
			if (entity.getNodeCode() == null || !recursion) {
				rootEntity = entity;
			} else {
				enm.put(entity.getNodeCode(), entity);
			}
		}

		// 深度递归调用
		return config.definitionFactory().factory(rootEntity, enm);
	}

	private List<WorkFlowLinkDefinitionEntity> loadLinkDefinitionInTransaction(String code, Integer version,
																																						 Boolean recursion)
					throws WorkFlowException {
		return config.dbTransaction().doInTransaction(() -> {
			return config.definitionRepository().loadLinkDefinition(code, version, recursion);
		});
	}

	private List<WorkFlowNodeDefinitionEntity> loadNodeDefinitionInTransaction(String code, Integer version,
																																						 Boolean recursion) throws WorkFlowException {

		return config.dbTransaction().doInTransaction(() -> {
			return config.definitionRepository().loadNodeDefinition(code, version, recursion);
		});
	}

	private List<WorkFlowDefinitionEntity> loadDefinitionInTransaction(String code, Integer version,
																																		 Boolean recursion) throws WorkFlowException {
		return config.dbTransaction().doInTransaction(() -> {
			return config.definitionRepository().loadDefinition(code, version, recursion);
		});
	}

	private WorkFlowDefinitionEntity loadDeployedDefinitionInTransaction(String code) throws WorkFlowException {
		return config.dbTransaction().doInTransaction(() -> {
			WorkFlowDefinitionEntity deployed = config.definitionRepository().loadDeployedDefinition(code);
			return deployed;
		});
	}

	private void validCodeUnique(String definitionCode, List<WorkFlowNodeDefinition> nodes)
					throws ConflictDefinitionCode {
		validCodeUnique(definitionCode, nodes, new HashSet<>());
	}

	private boolean validCodeUnique(String definitionCode, List<WorkFlowNodeDefinition> nodes, Set<String> codes)
					throws ConflictDefinitionCode {
		if (codes.contains(definitionCode)) {
			throw new ConflictDefinitionCode("编码已存在" + definitionCode);
		}
		codes.add(definitionCode);
		for (WorkFlowNodeDefinition node : nodes) {
			if (codes.contains(node.getCode())) {
				throw new ConflictDefinitionCode("节点编码已存在" + node.getCode());
			}
			codes.add(node.getCode());
			if (node instanceof ChildWorkFlowNodeDefinition child) {
				WorkFlowDefinitionImpl childDefinition = (WorkFlowDefinitionImpl) child.getChild();
				validCodeUnique(childDefinition.code(), childDefinition.getNodes(), codes);
			}
		}
		return true;
	}

	/**
	 * 这里注意在数据库层面增加code， version唯一索引，防止并发插入
	 */
	@Override
	public void deploy(WorkFlowDefinition definition) throws WorkFlowException {
		validCodeUnique(definition.code(), ((WorkFlowDefinitionImpl) definition).getNodes());
		// 父子流程定义展开
		List<WorkFlowDefinitionEntity> deploying = toEntity(definition);

		// 部署流程定义
		config.dbTransaction().doInTransaction(() -> {
			Integer version = config.definitionRepository().loadDefinitionLatestVersion(definition.code());
			if (null == version) {
				version = 1;
			} else {
				version += 1;
			}
			WorkFlowDefinitionEntity deployed = config.definitionRepository().loadDeployedDefinition(definition.code());
			// 卸载已部署的
			if (null != deployed) {
				requireUndeployTransaction(deployed.getCode());
			}
			// 部署最新版本
			List<WorkFlowLinkDefinitionEntity> links = new ArrayList<>();
			List<WorkFlowNodeDefinitionEntity> nodes = new ArrayList<>();
			reduce(deploying, links, nodes, version);
			config.definitionRepository().bulkSave(deploying);
			config.definitionRepository().bulkSaveLink(links);
			config.definitionRepository().bulkSaveNode(nodes);
			return true;
		});
	}

	private void reduce(List<WorkFlowDefinitionEntity> deploying, List<WorkFlowLinkDefinitionEntity> links,
											List<WorkFlowNodeDefinitionEntity> nodes, Integer version) {

		for (WorkFlowDefinitionEntity item : deploying) {
			item.setVersion(version);
			item.getLinks().forEach(i -> i.setVersion(version));
			links.addAll(item.getLinks());
			item.getNodes().forEach(i -> i.setVersion(version));
			nodes.addAll(item.getNodes());
		}
	}

	@Override
	public void undeploy(String code) throws WorkFlowException {
		config.dbTransaction().doInTransaction(() -> {
			requireUndeployTransaction(code);
			return true;
		});
	}

	private void requireUndeployTransaction(String code) throws WorkFlowException {
		int rows = config.definitionRepository().changeState(code, "N", "Y");
		if (rows == 0) {
			throw new UnDeployWorkFlowDefinitionException(code, "流程定义状态已改变");
		}
	}

}
