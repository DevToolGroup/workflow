package group.devtool.workflow.engine.operate;

import group.devtool.workflow.engine.WorkFlowContextImpl;
import group.devtool.workflow.engine.WorkFlowDispatch;
import group.devtool.workflow.engine.runtime.ChildWorkFlowNode;
import group.devtool.workflow.engine.runtime.WorkFlowInstance;
import group.devtool.workflow.engine.runtime.WorkFlowNode;

import java.io.Serializable;
import java.util.List;

/**
 * 流程操作。每个流程操作对应一个短事务，事务失败后，进入重试队列。
 */
public interface WorkFlowOperation extends Serializable {

    /**
     * 初始化子流程操作
     *
     * @param node 节点
     * @param instances 子流程实例
     * @param context 流程上下文
     * @return 子流程操作
     */
    static WorkFlowOperation ofChild(ChildWorkFlowNode node, List<WorkFlowInstance> instances, WorkFlowContextImpl context) {
        return new ChildWorkFlowOperation(node, instances, context);
    }

    /**
     * 初始化流程运行操作
     *
     * @param rootInstanceId 根流程实例ID
     * @param taskId 任务ID
     * @param context 流程上下文
     * @return 流程操作
     */
    static WorkFlowOperation ofRun(String rootInstanceId, String taskId, WorkFlowContextImpl context) {
        return new RunWorkFlowOperation(rootInstanceId, taskId, context);
    }

    /**
     * 初始化流程启动操作
     *
     * @param rootInstanceId 根流程实例ID
     * @param instance 流程实例
     * @param context 流程上下文
     * @return 流程启动操作
     */
    static WorkFlowOperation ofStart(String rootInstanceId, WorkFlowInstance instance, WorkFlowContextImpl context) {
        return new StartWorkFlowOperation(rootInstanceId, instance, context);
    }

    /**
     * 初始化流程流转操作
     *
     * @param rootInstanceId 根流程实例ID
     * @param instance 流程实例
     * @param node 流程节点
     * @param context 流程上下文
     * @return 流程流转操作
     */
    static WorkFlowOperation ofNext(String rootInstanceId, WorkFlowInstance instance, WorkFlowNode node, WorkFlowContextImpl context) {
        return new NextWorkFlowOperation(rootInstanceId, instance, node, context);
    }

    static WorkFlowOperation ofStop(String instanceId, WorkFlowContextImpl context) {
        return new StopWorkFlowOperation(instanceId, context);
    }

    public void operate(WorkFlowDispatch dispatch);

}