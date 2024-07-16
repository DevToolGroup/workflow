/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.impl.mapper;

import java.util.List;

import group.devtool.workflow.impl.entity.*;
import org.apache.ibatis.annotations.Param;


public interface WorkFlowMapper {

	// 流程定义相关
	void bulkSaveDefinition(@Param("items") List<WorkFlowDefinitionEntity> entities);

	void bulkSaveNodeDefinition(@Param("nodes") List<WorkFlowNodeDefinitionEntity> nodes);

	void bulkSaveLinkDefinition(@Param("links") List<WorkFlowLinkDefinitionEntity> links);

	int changeState(@Param("code") String code, @Param("afterState") String afterState, @Param("beforeState") String beforeState);

	List<WorkFlowDefinitionEntity> loadDefinition(@Param("code") String code, @Param("rootCode") String rootCode,
																								@Param("version") Integer version,
																								@Param("recursion") Boolean recursion);

	WorkFlowDefinitionEntity loadDeployedDefinition(@Param("code") String code, @Param("rootCode") String rootCode);

	Integer loadDefinitionLatestVersion(@Param("code") String code, @Param("rootCode") String rootCode);

	List<WorkFlowNodeDefinitionEntity> loadNodeDefinition(@Param("code") String code,
																												@Param("rootCode") String rootCode,
																												@Param("version") Integer version, @Param("recursion") Boolean recursion);

	List<WorkFlowLinkDefinitionEntity> loadLinkDefinition(@Param("code") String code,
																												@Param("rootCode") String rootCode,
																												@Param("version") Integer version, @Param("recursion") Boolean recursion);

	// 流程实例相关
	List<WorkFlowVariableEntity> loadVariable(@Param("instanceId") String instanceId);

	void bulkSaveVariable(@Param("variables") List<WorkFlowVariableEntity> variables);

	WorkFlowInstanceEntity loadInstance(@Param("instanceId") String instanceId,
																			@Param("rootInstanceId") String rootInstanceId);

	List<WorkFlowTaskEntity> loadTaskByNodeId(@Param("nodeId") String nodeId,
																						@Param("rootInstanceId") String rootInstanceId);

	WorkFlowTaskEntity loadTaskByTaskId(@Param("taskId") String taskId,
																			@Param("rootInstanceId") String rootInstanceId);

	WorkFlowNodeEntity loadNodeById(@Param("nodeId") String nodeId,
																	@Param("rootInstanceId") String rootInstanceId);

	int lockNode(@Param("nodeId") String nodeId,
								@Param("rootInstanceId") String rootInstanceId,
								@Param("version") Integer version);

	Integer changeTaskComplete(@Param("completeUser") String completeUser, @Param("completeTime") Long completeTime,
														 @Param("taskId") String taskId, @Param("rootInstanceId") String rootInstanceId);

	void saveNode(@Param("entity") WorkFlowNodeEntity entity);

	Integer changeNodeComplete(@Param("nodeId") String nodeId,
														 @Param("rootInstanceId") String rootInstanceId,
														 @Param("version") Integer version);


	int changeInstanceComplete(@Param("instanceId") String instanceId, @Param("rootInstanceId") String rootInstanceId);

	void saveInstance(@Param("entity") WorkFlowInstanceEntity entity);

	void bulkSaveTask(@Param("tasks") List<WorkFlowTaskEntity> entities);

	List<WorkFlowTaskEntity> loadActiveTask(@Param("rootInstanceId") String rootInstanceId);

	// 延时任务
	void addDelayTask(@Param("item") WorkFlowDelayItemEntity item);

	List<WorkFlowDelayItemEntity> loadDelayTask(@Param("current") Long current);

	void setDelaySuccess(@Param("itemId") String itemId, @Param("rootInstanceId") String rootInstanceId);

	int changeInstanceStop(@Param("instanceId") String instanceId, @Param("rootInstanceId") String rootInstanceId);

	WorkFlowNodeEntity loadActiveNodeByCode(@Param("nodeCode") String nodeCode, @Param("rootInstanceId") String rootInstanceId);

	WorkFlowNodeEntity loadChildActiveNodeByCode(@Param("nodeCode") String nodeCode,
																							 @Param("instanceId") String instanceId,
																							 @Param("rootInstanceId") String rootInstanceId);

	WorkFlowInstanceEntity loadParentInstance(@Param("parentTaskId") String parentTaskId, @Param("instanceId") String instanceId);
}
