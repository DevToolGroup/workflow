package group.devtool.workflow.engine;

import group.devtool.workflow.engine.operation.WorkFlowOperation;


/**
 * 流程操作异常重试服务
 */
public interface WorkFlowRetryService {

    /**
     * 添加流程操作
     *
     * @param operations 流程操作
     */
    void addOperation(WorkFlowOperation... operations);

    void addCallback(WorkFlowCallback.WorkFlowEvent event, WorkFlowContextImpl context);

    /**
     * 流程操作状态修改
     *
     * @param code          流程操作code
     * @param status       流程操作状态
     */
    void changeOperation(String code, Integer status);

    /**
     * 重试流程操作
     */
    void retryOperation();
}
