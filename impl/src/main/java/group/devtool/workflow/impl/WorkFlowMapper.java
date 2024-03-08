package group.devtool.workflow.impl;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import group.devtool.workflow.impl.WorkFlowSchedulerImpl.DelayItemImpl;

public interface WorkFlowMapper {

  // 流程定义相关
  void bulkSaveDefinition(@Param("items") List<WorkFlowDefinitionEntity> entities);

  int bulkSaveNodeDefinition(@Param("nodes") List<WorkFlowNodeDefinitionEntity> nodes);

  int bulkSaveLinkDefinition(@Param("links") List<WorkFlowLinkDefinitionEntity> links);

  int changeState(@Param("code") String code, @Param("afterState") String afterState, @Param("beforeState") String beforeState);

  List<WorkFlowDefinitionEntity> loadDefinition(@Param("code") String code, @Param("version") Integer version,
                                                @Param("recursion") Boolean recursion);

  WorkFlowDefinitionEntity loadDeployedDefinition(@Param("code") String code);

  Integer loadDefinitionLatestVersion(@Param("code") String code);

  List<WorkFlowNodeDefinitionEntity> loadNodeDefinition(@Param("code") String code,
                                                        @Param("version") Integer version, @Param("recursion") Boolean recursion);

  List<WorkFlowLinkDefinitionEntity> loadLinkDefinition(@Param("code") String code,
                                                        @Param("version") Integer version, @Param("recursion") Boolean recursion);

  // 流程实例相关
  List<WorkFlowVariableEntity> loadVariable(@Param("instanceId") String instanceId);

  int bulkSaveVariable(@Param("variables") List<WorkFlowVariableEntity> variables);

  WorkFlowInstanceEntity loadInstance(@Param("instanceId") String instanceId,
                                      @Param("rootInstanceId") String rootInstanceId);

  List<WorkFlowTaskEntity> loadTaskByActiveNode(@Param("nodeCode") String nodeCode,
                                                @Param("rootInstanceId") String rootInstanceId);

  WorkFlowTaskEntity loadTaskByTaskId(@Param("taskId") String taskId,
                                      @Param("rootInstanceId") String rootInstanceId);

  Integer changeTaskComplete(@Param("completeUser") String completeUser, @Param("completeTime") Long completeTime,
      @Param("taskId") String taskId, @Param("rootInstanceId") String rootInstanceId);

  Integer changeNodeComplete(@Param("nodeCode") String code, @Param("rootInstanceId") String rootInstanceId);

  int updateInstance(@Param("entity") WorkFlowInstanceEntity entity);

  int saveInstance(@Param("entity") WorkFlowInstanceEntity entity);

  int bulkSaveTask(@Param("tasks") List<WorkFlowTaskEntity> entities);

  // 延时任务
  void addDelayTask(@Param("item") DelayItemImpl item);

  List<DelayItemImpl> loadDelayTask(@Param("current") Long current);

  int setDelaySuccess(@Param("itemId") String itemId, @Param("rootInstanceId") String rootInstanceId);

  // 分步事务
  int addTransactionOperation(@Param("entity") MybatisWorkFlowTransactionOperationEntity entity);

  void deleteVariableById(@Param("ids") List<Long> deleteIds, @Param("rootInstanceId") String rootInstanceId);

  void changeTaskDoing(@Param("taskId") String taskId, @Param("rootInstanceId") String rootInstanceId);

  void deleteInstanceById(@Param("instanceId") String instanceId, @Param("rootInstanceId") String rootInstanceId);

  void changeNodeDoing(@Param("nodeCode") String nodeCode, @Param("rootInstanceId") String rootInstanceId);

  void deleteTaskById(@Param("ids") List<String> deleteIds, @Param("rootInstanceId") String rootInstanceId);

  void doRollbackOperation(@Param("id") Long id, @Param("txId") String txId, @Param("rootInstanceId") String rootInstanceId2);

  void cleanTransactionOperation(@Param("txId") String txId, @Param("rootInstanceId") String rootInstanceId);

  List<MybatisWorkFlowTransactionOperationEntity> loadTransactionOperation(@Param("rootInstanceId") String rootInstanceId);

  List<WorkFlowTaskEntity> loadActiveTask(@Param("rootInstanceId") String rootInstanceId);

}
