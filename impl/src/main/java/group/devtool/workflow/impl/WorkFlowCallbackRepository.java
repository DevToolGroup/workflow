package group.devtool.workflow.impl;

import group.devtool.workflow.impl.entity.WorkFlowCallbackPayloadEntity;

import java.util.List;

public class WorkFlowCallbackRepository {

    private final WorkFlowConfigurationImpl configuration;

    public WorkFlowCallbackRepository(WorkFlowConfigurationImpl configuration) {
        this.configuration = configuration;
    }

    public void saveCallback(WorkFlowCallbackPayloadEntity payload) {
        configuration.getMapper().saveCallback(payload);
    }

    public List<WorkFlowCallbackPayloadEntity> loadCallback() {
        return configuration.getMapper().loadCallback();
    }

    public void changeCallback(String code, Integer status) {
        configuration.getMapper().changeCallback(code, status);
    }

    public WorkFlowCallbackPayloadEntity getFailCallback(String rootInstanceId) {
        return configuration.getMapper().getFailCallback(rootInstanceId);
    }
}
