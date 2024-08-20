package group.devtool.workflow.impl.repository;

import group.devtool.workflow.impl.WorkFlowConfigurationImpl;
import group.devtool.workflow.impl.entity.RetryWorkFlowOperationEntity;
import group.devtool.workflow.impl.mapper.WorkFlowMapper;

import java.util.List;
import java.util.Objects;

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

    public void updateStatus(String code, Integer status) {
        WorkFlowMapper mapper = config.getMapper();
        mapper.updateStatus(code, status);
    }

    public boolean notExistOperation(String code) {
        WorkFlowMapper mapper = config.getMapper();
        RetryWorkFlowOperationEntity entity = mapper.getOperation(code);
        return Objects.isNull(entity);
    }
}
