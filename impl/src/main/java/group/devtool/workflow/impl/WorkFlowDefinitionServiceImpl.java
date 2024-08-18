/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import group.devtool.workflow.engine.definition.*;
import group.devtool.workflow.engine.WorkFlowDefinitionService;
import group.devtool.workflow.engine.exception.*;
import group.devtool.workflow.impl.definition.SPELWorkFlowLinkDefinitionImpl;
import group.devtool.workflow.impl.definition.WorkFlowDefinitionImpl;
import group.devtool.workflow.impl.entity.WorkFlowDefinitionEntity;
import group.devtool.workflow.impl.entity.WorkFlowLinkDefinitionEntity;
import group.devtool.workflow.impl.entity.WorkFlowNodeDefinitionEntity;
import group.devtool.workflow.impl.repository.WorkFlowDefinitionRepository;

/**
 * {@link WorkFlowDefinitionService} 默认实现
 */
public class WorkFlowDefinitionServiceImpl implements WorkFlowDefinitionService {

	private final WorkFlowConfigurationImpl config;

	public WorkFlowDefinitionServiceImpl() {
		this.config = WorkFlowConfigurationImpl.CONFIG;
	}

	@Override
	public WorkFlowDefinition load(String code, String rootCode, Integer version, Boolean recursion) {

		WorkFlowDefinitionRepository definitionRepository = config.definitionRepository();

		if (null == version) {
			WorkFlowDefinitionEntity deployed = definitionRepository.loadDeployedDefinition(code, rootCode);
			if (null == deployed) {
				throw new NotFoundWorkFlowDefinition(code, null);
			}
			version = deployed.getVersion();
		}
		// 加载父子流程定义
		List<WorkFlowDefinitionEntity> entities = definitionRepository.loadDefinition(code, rootCode, version, recursion);
		if (null == entities) {
			throw new NotFoundWorkFlowDefinition(code, version);
		}

		// 加载父子流程节点定义
		List<WorkFlowNodeDefinitionEntity> nodeEntities = definitionRepository.loadNodeDefinition(code, rootCode, version, recursion);
		if (null == nodeEntities) {
			throw new NotFoundWorkFlowDefinition("流程定义缺少节点信息。编码：" + code + "，版本：" + version);
		}
		Map<String, List<WorkFlowNodeDefinitionEntity>> nem = nodeEntities.stream()
						.collect(Collectors.groupingBy(WorkFlowNodeDefinitionEntity::getDefinitionCode));

		// 加载父子流程连线定义
		List<WorkFlowLinkDefinitionEntity> linkEntities = definitionRepository.loadLinkDefinition(code, rootCode, version, recursion);
		if (null == linkEntities) {
			throw new NotFoundWorkFlowDefinition("流程定义缺少连接线信息。编码：" + code + "，版本：" + version);
		}
		Map<String, List<WorkFlowLinkDefinitionEntity>> lem = linkEntities.stream()
						.collect(Collectors.groupingBy(WorkFlowLinkDefinitionEntity::getDefinitionCode));

		// 构造流程定义
		WorkFlowDefinitionEntity rootEntity = null;
		Map<String, List<WorkFlowDefinitionEntity>> enm = new HashMap<>();
		for (WorkFlowDefinitionEntity entity : entities) {
			entity.setNodes(nem.get(entity.getCode()));
			entity.setLinks(lem.get(entity.getCode()));
			if (entity.getNodeCode() == null || !recursion) {
				rootEntity = entity;
			} else {
				List<WorkFlowDefinitionEntity> cs = enm.getOrDefault(entity.getNodeCode(), new ArrayList<>());
				cs.add(entity);
				enm.put(entity.getNodeCode(), cs);
			}
		}

		if (null == rootEntity) {
			throw new NotFoundWorkFlowDefinition("缺少流程定义信息。编码：" + code + "，版本：" + version);
		}

		return config.definitionFactory().factory(rootEntity, enm);
	}

	/**
	 * 这里注意在数据库层面增加code， version唯一索引，防止并发插入
	 */
	@Override
	public void deploy(WorkFlowDefinition definition) throws TransactionException {
		// 父子流程定义展开
		List<WorkFlowDefinitionEntity> deploying = toEntity(definition);

		// 部署流程定义
		Integer version = config.definitionRepository().loadDefinitionLatestVersion(definition.getCode(), definition.getCode());
		if (null == version) {
			version = 1;
		} else {
			version += 1;
		}
		WorkFlowDefinitionEntity deployed = config.definitionRepository().loadDeployedDefinition(definition.getCode(), definition.getCode());
		// 卸载已部署的
		if (null != deployed) {
			undeploy(deployed.getCode());
		}
		// 部署最新版本
		List<WorkFlowLinkDefinitionEntity> links = new ArrayList<>();
		List<WorkFlowNodeDefinitionEntity> nodes = new ArrayList<>();

		// 更新，流程、节点、连线的版本，并合并
		reduce(deploying, links, nodes, version);

		// 持久化
		WorkFlowDefinitionRepository repository = config.definitionRepository();
		repository.bulkSave(deploying);
		repository.bulkSaveLink(links);
		repository.bulkSaveNode(nodes);
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
	public void undeploy(String code) throws TransactionException {
		int rows = config.definitionRepository().undeploy(code, "N", "Y");
		if (rows == 0) {
			throw new ConcurrencyException("卸载流程定义异常，流程定义状态已改变。流程定义编码：" + code);
		}
	}

	private List<WorkFlowDefinitionEntity> toEntity(WorkFlowDefinition definition) {
		String rootDefinitionCode = definition.getCode();
		List<WorkFlowDefinitionEntity> definitions = new ArrayList<>();
		definitions.add(toEntity(null, definition, rootDefinitionCode));

		// 广度遍历子流程
		List<WorkFlowDefinition> stack = new ArrayList<>();
		stack.add(definition);
		do {
			Iterator<WorkFlowDefinition> it = stack.iterator();
			List<WorkFlowDefinition> children = new ArrayList<>();

			while (it.hasNext()) {
				WorkFlowDefinition def = it.next();
				for (WorkFlowNodeDefinition node : ((WorkFlowDefinitionImpl) def).getNodes()) {
					if (node instanceof ChildWorkFlowNodeDefinition) {
						List<WorkFlowDefinition> child = ((ChildWorkFlowNodeDefinition) node).getChild();
						for (WorkFlowDefinition c : child) {
							definitions.add(toEntity(node.getCode(), c, rootDefinitionCode));
						}
						children.addAll(child);
					}

				}
				it.remove();
			}

			stack = children;
		} while (!stack.isEmpty());
		return definitions;
	}

	private WorkFlowDefinitionEntity toEntity(String nodeCode, WorkFlowDefinition item, String rootCode) {
		WorkFlowDefinitionEntity entity = new WorkFlowDefinitionEntity();
		entity.setCode(item.getCode());
		entity.setName(item.getName());
		entity.setState("Y");
		entity.setVersion(item.getVersion());
		entity.setRootCode(rootCode);
		entity.setNodeCode(nodeCode);
		entity.setNodes(toNodeEntity(rootCode, item.getCode(), item.getVersion(), ((WorkFlowDefinitionImpl) item).getNodes()));
		entity.setLinks(toLinkEntity(rootCode, item.getCode(), item.getVersion(), ((WorkFlowDefinitionImpl) item).getLinks()));
		return entity;
	}

	private List<WorkFlowLinkDefinitionEntity> toLinkEntity(String rootDefinitionCode, String definitionCode,
																													Integer version, List<WorkFlowLinkDefinition> links) {
		List<WorkFlowLinkDefinitionEntity> result = new ArrayList<>(links.size());
		for (WorkFlowLinkDefinition link : links) {
			result.add(toLinkEntity(rootDefinitionCode, definitionCode, link, version));
		}
		return result;
	}

	private WorkFlowLinkDefinitionEntity toLinkEntity(String rootDefinitionCode, String definitionCode,
																										WorkFlowLinkDefinition link, Integer version) {
		SPELWorkFlowLinkDefinitionImpl linkImpl = (SPELWorkFlowLinkDefinitionImpl) link;
		WorkFlowLinkDefinitionEntity entity = new WorkFlowLinkDefinitionEntity();
		entity.setRootDefinitionCode(rootDefinitionCode);
		entity.setDefinitionCode(definitionCode);
		entity.setVersion(version);
		entity.setCode(link.getCode());
		entity.setSource(linkImpl.getSource());
		entity.setTarget(linkImpl.getTarget());
		entity.setParser(linkImpl.getParser());
		entity.setExpression(linkImpl.getExpression());
		return entity;
	}

	private List<WorkFlowNodeDefinitionEntity> toNodeEntity(String rootDefinitionCode, String definitionCode,
																													Integer version, List<WorkFlowNodeDefinition> nodes) {
		List<WorkFlowNodeDefinitionEntity> result = new ArrayList<>(nodes.size());
		for (WorkFlowNodeDefinition node : nodes) {
			result.add(toNodeEntity(rootDefinitionCode, definitionCode, node, version));
		}
		return result;
	}

	private WorkFlowNodeDefinitionEntity toNodeEntity(String rootDefinitionCode, String definitionCode,
																										WorkFlowNodeDefinition node, Integer version) {
		WorkFlowNodeDefinitionEntity entity = new WorkFlowNodeDefinitionEntity();
		entity.setCode(node.getCode());
		entity.setName(node.getName());
		entity.setType(node.getType());
		entity.setRootDefinitionCode(rootDefinitionCode);
		entity.setDefinitionCode(definitionCode);
		entity.setVersion(version);

		if (null == node.getConfig()) {
			return entity;
		}

		entity.setConfig(node.getConfig(node.getConfig()));
		return entity;
	}


}
