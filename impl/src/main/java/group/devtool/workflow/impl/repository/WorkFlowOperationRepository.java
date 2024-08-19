package group.devtool.workflow.impl.repository;

import group.devtool.workflow.impl.WorkFlowConfigurationImpl;
import group.devtool.workflow.impl.entity.RetryWorkFlowOperationEntity;
import group.devtool.workflow.impl.mapper.WorkFlowMapper;

import java.util.List;

public class WorkFlowOperationRepository {

    private final WorkFlowConfigurationImpl config;

    public WorkFlowOperationRepository() {
        this.config = WorkFlowConfigurationImpl.CONFIG;
    }

    public List<RetryWorkFlowOperationEntity> loadFailOperation(Long lastId) {
        WorkFlowMapper mapper = config.getMapper();
        return mapper.loadFailOperation(lastId);
    }

    public void batchSave(List<RetryWorkFlowOperationEntity> entities) {
        WorkFlowMapper mapper = config.getMapper();
        mapper.batchSave(entities);
    }

    public void updateStatus(String code, Integer status, Integer beforeStatus) {
        WorkFlowMapper mapper = config.getMapper();
        mapper.updateStatus(code, status, beforeStatus);
    }
}
