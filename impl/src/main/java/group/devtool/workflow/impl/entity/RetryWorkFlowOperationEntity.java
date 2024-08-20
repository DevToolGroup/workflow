package group.devtool.workflow.impl.entity;

import lombok.Data;

@Data
public class RetryWorkFlowOperationEntity {

    private Long id;

    private String code;

    private Integer status;

    private String rootInstanceId;

    private String type;

    private String instanceId;

    private String context;

    private String taskId;

    private String nodeId;

    private String children;


}
