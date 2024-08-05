/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.engine;

import java.util.ArrayList;
import java.util.List;

import group.devtool.workflow.engine.WorkFlowCallback.WorkFlowEvent;
import group.devtool.workflow.engine.definition.WorkFlowDefinition;
import group.devtool.workflow.engine.definition.WorkFlowNodeDefinition;
import group.devtool.workflow.engine.exception.*;
import group.devtool.workflow.engine.operate.WorkFlowOperation;
import group.devtool.workflow.engine.runtime.*;

/**
 * 流程引擎主要职责：
 * 1. 完成流程定义的部署及卸载
 * 2. 完成流程的启动、执行、终止
 * 3. 流程事件的触发
 */
public final class WorkFlowEngine {

    private final WorkFlowConfiguration config;

    private final WorkFlowDispatch.DefaultWorkFlowDispatch dispatch;

    public WorkFlowEngine(WorkFlowConfiguration config) {
        this.config = config;
        this.dispatch = WorkFlowDispatch.of(config);
    }

    /**
     * 部署流程定义
     *
     * @param definition 流程定义
     */
    public void deploy(WorkFlowDefinition definition) {
        config.definitionService().deploy(definition);
    }

    /**
     * 加载流程定义
     *
     * @param code    流程定义编码
     * @param version 流程定义版本
     * @return 流程定义
     */
    public WorkFlowDefinition load(String code, Integer version) {
        return config.definitionService().load(code, code, version, true);
    }

    /**
     * 卸载流程定义
     *
     * @param code 流程定义编码
     */
    public void undeploy(String code) {
        config.definitionService().undeploy(code);
    }

    /**
     * 加载流程图以及流程运行节点信息
     *
     * @param rootInstanceId 根流程实例ID
     * @return 流程图
     */
    public List<WorkFlowTask> loadTask(String rootInstanceId) {
        return config.service().loadActiveTask(rootInstanceId);
    }

    /**
     * 根据流程定义编码，启动流程引擎，初始化流程实例
     *
     * @param code      流程定义编码
     * @param variables 流程启动参数
     * @return 操作ID
     */
    public String start(String code, WorkFlowVariable... variables) {
        WorkFlowService service = config.service();
        // 初始化流程实例
        WorkFlowInstance instance = service.getInstance(code);

        // 初始化流程上下文
        WorkFlowContextImpl context = service.getContext(instance.getInstanceId(), variables);
        try {
            // 启动流程实例
            dispatch.addOperation(WorkFlowOperation.ofStart(instance.getInstanceId(), instance, context));

            // 运行流程调度
            dispatch.dispatch();

            return instance.getInstanceId();
        } catch (Exception e) {
            config.callback().callback(WorkFlowEvent.EXCEPTION, context);
            throw e;
        }
    }

    /**
     * 根据任务ID执行任务。
     * 如果任务完成判断流程节点是否完成，如果完成，递归流转。
     *
     * @param variables      流转变量
     * @param taskId         流程任务ID
     * @param rootInstanceId 根流程实例ID
     */
    public void run(String rootInstanceId, String taskId, WorkFlowVariable... variables) {
        // 初始化上下文
        WorkFlowContextImpl context = config.service().getContext(rootInstanceId, variables);
        try {
            dispatch.addOperation(WorkFlowOperation.ofRun(rootInstanceId, taskId, context));
            dispatch.dispatch();
        } catch (Exception e) {
            config.callback().callback(WorkFlowEvent.EXCEPTION, context);
            throw e;
        }
    }

    /**
     * 根据事件执行任务。
     * 如果任务完成判断流程节点是否完成，如果完成，递归流转。
     *
     * @param rootInstanceId 根流程实例ID
     * @param node           节点编码
     * @param event          事件名称
     * @param variables      流转变量
     * @throws NotFoundWorkFlowTask 流程异常
     */
    public void trigger(String rootInstanceId, String node, String event, WorkFlowVariable... variables) throws NotFoundWorkFlowTask {
        WorkFlowService service = config.service();

        // 初始化上下文 -- 上下文在并发条件下
        WorkFlowContextImpl context = service.getContext(rootInstanceId, variables);
        WorkFlowTask task = service.getTask(event, node, rootInstanceId);
        if (null == task) {
            throw new NotFoundWorkFlowTask("事件任务未找到。事件编码：" + event + " ，节点编码：" + node + "，流程实例ID：" + rootInstanceId);
        }
        dispatch.addOperation(WorkFlowOperation.ofRun(rootInstanceId, task.getTaskId(), context));
        dispatch.dispatch();
    }

    /**
     * 终止流程实例
     *
     * @param instanceId 流程
     */
    public void stop(String instanceId) {
        WorkFlowContextImpl ctx = config.service().getContext(instanceId);
        dispatch.addOperation(WorkFlowOperation.ofStop(instanceId, ctx));
        dispatch.dispatch();

    }

}
