package group.devtool.workflow.impl;

import group.devtool.workflow.engine.*;
import group.devtool.workflow.engine.common.JacksonUtils;
import group.devtool.workflow.engine.exception.WorkFlowRuntimeException;
import group.devtool.workflow.engine.operation.*;
import group.devtool.workflow.engine.runtime.ChildWorkFlowNode;
import group.devtool.workflow.engine.runtime.WorkFlowInstance;
import group.devtool.workflow.engine.runtime.WorkFlowNode;
import group.devtool.workflow.impl.entity.RetryWorkFlowOperationEntity;
import group.devtool.workflow.impl.entity.WorkFlowCallbackPayloadEntity;
import group.devtool.workflow.impl.repository.WorkFlowOperationRepository;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;

@Slf4j
public class WorkFlowRetryServiceImpl implements WorkFlowRetryService, Closeable {

    private static final String START = "START";

    private static final String RUN = "RUN";

    private static final String NEXT = "NEXT";

    private static final String CHILD = "CHILD";

    private static final String STOP = "STOP";

    private final WorkFlowConfigurationImpl config;

    private final ExecutorService pool;

    private final LinkedBlockingDeque<RetryWorkFlowOperation> failOperationQueue;

    private final LinkedBlockingDeque<WorkFlowCallbackPayloadEntity> callbackBlockingQueue;

    private final Object operationWait = new Object();

    private final Object callbackWait = new Object();

    public WorkFlowRetryServiceImpl() {
        config = WorkFlowConfigurationImpl.CONFIG;
        pool = Executors.newFixedThreadPool(12);

        failOperationQueue = new LinkedBlockingDeque<>();
        callbackBlockingQueue = new LinkedBlockingDeque<>();

        // 启动加载线程
        pool.execute(this::retryOperation);

        // 启动重试线程
        for (int i = 0; i < 4; i++) {
            pool.execute(this::dispatchOperation);
        }

        pool.execute(this::dispatchCallback);
        // 启动回调线程
        for (int i = 0; i < 6; i++) {
            pool.execute(this::doCallback);
        }
    }

    private void dispatchCallback() {
        while (true) {
            if (callbackBlockingQueue.size() > 256) {
                synchronized (callbackWait) {
                    try {
                        callbackWait.wait(300);
                    } catch (InterruptedException e) {
                        log.error("回调调度线程中断...", e);
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
            List<WorkFlowCallbackPayloadEntity> payloads = loadCallback();
            for (WorkFlowCallbackPayloadEntity payload : payloads) {
                if (hasFailCallback(payload)) {
                    continue;
                }
                callbackBlockingQueue.add(payload);
            }
        }
    }

    private boolean hasFailCallback(WorkFlowCallbackPayloadEntity payload) {
        if (payload.getStatus() == -1) {
            return false;
        }
        WorkFlowCallbackPayloadEntity data = config.callbackRepository().getFailCallback(payload.getRootInstanceId());
        return Objects.nonNull(data);
    }

    private List<WorkFlowCallbackPayloadEntity> loadCallback() {
        return config.callbackRepository().loadCallback();
    }

    private void doCallback() {
        while (true) {
            WorkFlowCallbackPayloadEntity payload;
            try {
                payload = callbackBlockingQueue.take();
            } catch (InterruptedException e) {
                log.error("回调执行线程中断...", e);
                Thread.currentThread().interrupt();
                return;
            }
            try {
                config.callback().callback(WorkFlowCallback.WorkFlowEvent.valueOf(payload.getEvent()),
                        JacksonUtils.deserialize(payload.getContext()));

                changeCallback(payload.getCode(), 1);
            } catch (Exception e) {
                log.error("回调执行异常. 回调事件: {}, 回调上下文: {}, 异常: {}",
                        payload.getEvent(),
                        JacksonUtils.serialize(payload.getContext()),
                        e);
                changeCallback(payload.getCode(), -1);
            }

        }
    }

    private void changeCallback(String code, Integer status) {
        config.callbackRepository().changeCallback(code, status);
    }

    private void dispatchOperation() {
        while (true) {
            RetryWorkFlowOperation fail;
            try {
                fail = failOperationQueue.take();
            } catch (InterruptedException e) {
                log.error("流程重试线程中断...", e);
                Thread.currentThread().interrupt();
                return;
            }
            // 校验fail是否正常入库
            if (config.operationRepository().notExistOperation(fail.getCode())) {
                continue;
            }
            try {
                WorkFlowDispatch.AbstractWorkFlowDispatch dispatch = WorkFlowDispatch.of(config);
                dispatch.retryDispatch(fail);
            } catch (Exception e) {
                log.error("流程操作重试异常. 流程操作: {}, 异常堆栈: {}", fail.getRootInstanceId(), e);
            }
        }
    }

    @Override
    public void retryOperation() {
        List<RetryWorkFlowOperation> fails;
        Long lastId = null;
        do {
            if (failOperationQueue.size() >= 256) {
                synchronized (operationWait) {
                    try {
                        operationWait.wait(500);
                    } catch (InterruptedException e) {
                        log.error("线程中断...", e);
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }

            fails = loadFailOperation(lastId);
            RetryWorkFlowOperation last = null;
            for (RetryWorkFlowOperation fail : fails) {
                last = fail;
                failOperationQueue.add(fail);
            }
            if (last != null) {
                lastId = last.getId();
            }
        } while (!fails.isEmpty());
    }

    private List<RetryWorkFlowOperation> loadFailOperation(Long lastId) {
        WorkFlowOperationRepository repository = config.operationRepository();
        List<RetryWorkFlowOperationEntity> entities = repository.loadFailOperation(lastId);
        return toOperation(entities);
    }

    private List<RetryWorkFlowOperation> toOperation(List<RetryWorkFlowOperationEntity> entities) {
        List<RetryWorkFlowOperation> result = new ArrayList<>();
        for (RetryWorkFlowOperationEntity entity : entities) {
            result.add(toOperation(entity));
        }
        return result;
    }

    private RetryWorkFlowOperation toOperation(RetryWorkFlowOperationEntity entity) {
        switch (entity.getType()) {
            case START:
                return new RetryWorkFlowOperation(new StartWorkFlowOperation(entity.getRootInstanceId(),
                        getInstance(entity.getInstanceId(), entity.getRootInstanceId()),
                        getVariable(entity.getContext())
                ), entity.getStatus(), entity.getCode(), entity.getId());

            case RUN:
                return new RetryWorkFlowOperation(new RunWorkFlowOperation(entity.getRootInstanceId(),
                        entity.getTaskId(),
                        getVariable(entity.getContext())
                ), entity.getStatus(), entity.getCode(), entity.getId());

            case NEXT:
                return new RetryWorkFlowOperation(new NextWorkFlowOperation(entity.getRootInstanceId(),
                        entity.getInstanceId(),
                        getNode(entity.getRootInstanceId(), entity.getNodeId()), getVariable(entity.getContext())
                ), entity.getStatus(), entity.getCode(), entity.getId());

            case CHILD:
                return new RetryWorkFlowOperation(new ChildWorkFlowOperation(entity.getRootInstanceId(),
                        (ChildWorkFlowNode) getNode(entity.getRootInstanceId(), entity.getNodeId()),
                        getInstances(entity.getChildren(), entity.getRootInstanceId()),
                        getVariable(entity.getContext())
                ), entity.getStatus(), entity.getCode(), entity.getId());

            case STOP:
                return new RetryWorkFlowOperation(new StopWorkFlowOperation(entity.getRootInstanceId(),
                        getVariable(entity.getContext())
                ), entity.getStatus(), entity.getCode(), entity.getId());
            default:
                throw new WorkFlowRuntimeException(String.format("流程操作类型不支持. 类型名称：%s, 流程操作ID: %s",
                        entity.getType(),
                        entity.getCode()));
        }
    }

    private List<WorkFlowInstance> getInstances(String instanceIds, String rootInstanceId) {
        String[] is = instanceIds.split(",");

        List<WorkFlowInstance> result = new ArrayList<>();
        for (String instanceId : is) {
            result.add(getInstance(instanceId, rootInstanceId));
        }
        return result;
    }

    private WorkFlowNode getNode(String rootInstanceId, String nodeId) {
        return config.service().getNode(nodeId, rootInstanceId);
    }

    private WorkFlowContextImpl getVariable(String parameters) {
        return JacksonUtils.deserialize(parameters);
    }

    private WorkFlowInstance getInstance(String instanceId, String rootInstanceId) {
        return config.service().getInstance(instanceId, rootInstanceId);
    }

    @Override
    public void addOperation(WorkFlowOperation... operations) {
        WorkFlowOperationRepository repository = config.operationRepository();
        List<RetryWorkFlowOperationEntity> entities = toEntity(operations);
        repository.batchSave(entities);
    }

    @Override
    public void addCallback(WorkFlowCallback.WorkFlowEvent event, WorkFlowContextImpl context) {
        WorkFlowIdSupplier idSupplier = config.idSupplier();
        WorkFlowCallbackPayloadEntity payload = new WorkFlowCallbackPayloadEntity(idSupplier.getCode(),
                event.name(),
                JacksonUtils.serialize(context),
                context.getRootInstanceId()
        );

        WorkFlowCallbackRepository repository = config.callbackRepository();
        repository.saveCallback(payload);
    }

    private List<RetryWorkFlowOperationEntity> toEntity(WorkFlowOperation... operations) {
        if (operations.length == 0) {
            return new ArrayList<>();
        }
        WorkFlowIdSupplier supplier = config.idSupplier();

        List<RetryWorkFlowOperationEntity> entities = new ArrayList<>();
        for (WorkFlowOperation operation : operations) {
            RetryWorkFlowOperation op = new RetryWorkFlowOperation(operation, supplier.getCode());

            RetryWorkFlowOperationEntity entity = new RetryWorkFlowOperationEntity();
            entity.setCode(op.getCode());
            entity.setStatus(op.getStatus());
            entity.setRootInstanceId(op.getDelegate().getRootInstanceId());

            if (op.getDelegate() instanceof StartWorkFlowOperation) {
                entity.setType(START);

                StartWorkFlowOperation delegate = ((StartWorkFlowOperation) op.getDelegate());
                entity.setInstanceId(delegate.getInstance().getInstanceId());
                entity.setContext(JacksonUtils.serialize((delegate.getContext())));

            } else if (op.getDelegate() instanceof RunWorkFlowOperation) {
                entity.setType(RUN);

                RunWorkFlowOperation delegate = ((RunWorkFlowOperation) op.getDelegate());
                entity.setTaskId(delegate.getTaskId());
                entity.setContext(JacksonUtils.serialize((delegate.getContext())));

            } else if (op.getDelegate() instanceof StopWorkFlowOperation) {
                entity.setType(STOP);

                StopWorkFlowOperation delegate = ((StopWorkFlowOperation) op.getDelegate());
                entity.setContext(JacksonUtils.serialize((delegate.getContext())));

            } else if (op.getDelegate() instanceof NextWorkFlowOperation) {
                entity.setType(NEXT);

                NextWorkFlowOperation delegate = ((NextWorkFlowOperation) op.getDelegate());
                entity.setNodeId(delegate.getNode().getNodeClass());
                entity.setInstanceId(delegate.getInstanceId());
                entity.setContext(JacksonUtils.serialize((delegate.getContext())));

            } else if (op.getDelegate() instanceof ChildWorkFlowOperation) {
                entity.setType(CHILD);

                ChildWorkFlowOperation delegate = ((ChildWorkFlowOperation) op.getDelegate());
                entity.setNodeId(delegate.getNode().getNodeId());
                entity.setChildren(delegate.getInstances().stream().map(WorkFlowInstance::getInstanceId).collect(Collectors.joining(",")));
                entity.setContext(JacksonUtils.serialize((delegate.getContext())));

            } else {
                throw new WorkFlowRuntimeException("操作类型不支持");
            }
            entities.add(entity);
        }
        return entities;
    }

    @Override
    public void changeOperation(String code, Integer status) {
        WorkFlowOperationRepository repository = config.operationRepository();
        repository.updateStatus(code, status);
    }


    @Override
    public void close() {
        pool.shutdown();
    }
}
