package group.devtool.workflow.impl.entity;

import lombok.Data;

@Data
public class WorkFlowCallbackPayloadEntity {

    private Long id;

    private String code;

    private String rootInstanceId;

    private String event;

    private String context;

    private Integer status;

    public WorkFlowCallbackPayloadEntity() {

    }

    public WorkFlowCallbackPayloadEntity(String code, String event, String context, String rootInstanceId) {
        this.code = code;
        this.event = event;
        this.context = context;
        this.rootInstanceId = rootInstanceId;
        this.status = 0;
    }


}