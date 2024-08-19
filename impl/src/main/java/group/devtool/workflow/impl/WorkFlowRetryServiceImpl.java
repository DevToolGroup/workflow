package group.devtool.workflow.impl;

import group.devtool.workflow.engine.*;
import group.devtool.workflow.engine.common.JacksonUtils;
import group.devtool.workflow.engine.exception.WorkFlowRuntimeException;
import group.devtool.workflow.engine.operation.*;
import group.devtool.workflow.engine.runtime.ChildWorkFlowNode;
import group.devtool.workflow.engine.runtime.WorkFlowInstance;
import group.devtool.workflow.engine.runtime.WorkFlowNode;
import group.devtool.workflow.impl.entity.RetryWorkFlowOperationEntity;
import group.devtool.workflow.impl.repository.WorkFlowOperationRepository;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
public class WorkFlowRetryServiceImpl extends AbstractWorkFlowRetryService implements Closeable {

    private static final String START = "START";

    private static final String RUN = "RUN";

    private static final String NEXT = "NEXT";

    private static final String CHILD = "CHILD";

    private static final String STOP = "STOP";

    private final WorkFlowConfigurationImpl config;

    private final ExecutorService pool;

    private final ArrayBlockingQueue<WorkFlowOperation> blockingQueue;

    private final Object wait = new Object();

    public WorkFlowRetryServiceImpl() {
        config = WorkFlowConfigurationImpl.CONFIG;
        pool = Executors.newFixedThreadPool(8);
        blockingQueue = new ArrayBlockingQueue<>(64);

        // 启动加载线程
        startLoadThread();

        // 启动重试线程
        startRetryThread();
    }

    private void startRetryThread() {
        for (int i = 0; i < 7; i++) {
            pool.execute(() -> {
                WorkFlowOperation fail;
                try {
                    fail = blockingQueue.take();
                } catch (InterruptedException e) {
                    log.error("流程重试线程中断...", e);
                    Thread.currentThread().interrupt();
                    return;
                }
                try {
                    WorkFlowDispatch.AbstractWorkFlowDispatch dispatch = WorkFlowDispatch.of(getConfig());
                    dispatch.retryDispatch(fail);
                } catch (Exception e) {
                    log.error("流程操作重试异常. 流程操作: {}, 异常堆栈: {}", fail.getRootInstanceId(), e);
                }
            });
        }
    }

    private void startLoadThread() {
        pool.execute(() -> {
            boolean running = true;
            while (running) {
                super.retryOperation();
                synchronized (wait) {
                    try {
                        wait.wait(500);
                    } catch (InterruptedException e) {
                        running = false;
                        log.error("线程中断...", e);
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });
    }

    @Override
    protected void execute(RetryWorkFlowOperation fail) {
        blockingQueue.add(fail);
    }

    @Override
    protected WorkFlowConfiguration getConfig() {
        return config;
    }

    @Override
    protected List<RetryWorkFlowOperation> loadFailOperation(Long lastId) {
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
                        getVariable(entity.getRootInstanceId(), entity.getParameters())
                ), entity.getStatus(), entity.getKey(), entity.getId());

            case RUN:
                return new RetryWorkFlowOperation(new RunWorkFlowOperation(entity.getRootInstanceId(),
                        entity.getTaskId(),
                        getVariable(entity.getRootInstanceId(), entity.getParameters())
                ), entity.getStatus(), entity.getKey(), entity.getId());

            case NEXT:
                return new RetryWorkFlowOperation(new NextWorkFlowOperation(entity.getRootInstanceId(),
                        entity.getInstanceId(),
                        getNode(entity.getRootInstanceId(), entity.getNodeId()), getVariable(entity.getRootInstanceId(), entity.getParameters())
                ), entity.getStatus(), entity.getKey(), entity.getId());

            case CHILD:
                return new RetryWorkFlowOperation(new ChildWorkFlowOperation(entity.getRootInstanceId(),
                        (ChildWorkFlowNode) getNode(entity.getRootInstanceId(), entity.getNodeId()),
                        getInstances(entity.getInstanceIds(), entity.getRootInstanceId()),
                        getVariable(entity.getRootInstanceId(), entity.getParameters())
                ), entity.getStatus(), entity.getKey(), entity.getId());

            case STOP:
                return new RetryWorkFlowOperation(new StopWorkFlowOperation(entity.getRootInstanceId(),
                        getVariable(entity.getRootInstanceId(), entity.getParameters())
                ), entity.getStatus(), entity.getKey(), entity.getId());
            default:
                throw new WorkFlowRuntimeException(String.format("流程操作类型不支持. 类型名称：%s, 流程操作ID: %s",
                        entity.getType(),
                        entity.getKey()));
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

    private WorkFlowContextImpl getVariable(String rootInstanceId, String parameters) {
        List<WorkFlowVariable> variables = JacksonUtils.deserialize(parameters);
        return new WorkFlowContextImpl(rootInstanceId, variables.toArray(new WorkFlowVariable[]{}));
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

    private List<RetryWorkFlowOperationEntity> toEntity(WorkFlowOperation... operations) {
        if (operations.length == 0) {
            return new ArrayList<>();
        }

        List<RetryWorkFlowOperationEntity> entities = new ArrayList<>();

        for (WorkFlowOperation operation : operations) {
            RetryWorkFlowOperation op = (RetryWorkFlowOperation) operation;

            RetryWorkFlowOperationEntity entity = new RetryWorkFlowOperationEntity();
            entity.setKey(op.getCode());
            entity.setStatus(op.getStatus());
            entity.setRootInstanceId(op.getDelegate().getRootInstanceId());

            if (op.getDelegate() instanceof StartWorkFlowOperation) {
                entity.setType(START);

                StartWorkFlowOperation delegate = ((StartWorkFlowOperation) op.getDelegate());
                entity.setInstanceId(delegate.getInstance().getInstanceId());
                entity.setParameters(JacksonUtils.serialize((delegate.getContext().getRuntimeVariables())));

            } else if (op.getDelegate() instanceof RunWorkFlowOperation) {
                entity.setType(RUN);

                RunWorkFlowOperation delegate = ((RunWorkFlowOperation) op.getDelegate());
                entity.setTaskId(delegate.getTaskId());
                entity.setParameters(JacksonUtils.serialize((delegate.getContext().getRuntimeVariables())));

            } else if (op.getDelegate() instanceof StopWorkFlowOperation) {
                entity.setType(STOP);

                StopWorkFlowOperation delegate = ((StopWorkFlowOperation) op.getDelegate());
                entity.setParameters(JacksonUtils.serialize((delegate.getContext().getRuntimeVariables())));

            } else if (op.getDelegate() instanceof NextWorkFlowOperation) {
                entity.setType(NEXT);

                NextWorkFlowOperation delegate = ((NextWorkFlowOperation) op.getDelegate());
                entity.setNodeId(delegate.getNode().getNodeClass());
                entity.setInstanceId(delegate.getInstanceId());
                entity.setParameters(JacksonUtils.serialize((delegate.getContext().getRuntimeVariables())));

            } else if (op.getDelegate() instanceof ChildWorkFlowOperation) {
                entity.setType(CHILD);

                ChildWorkFlowOperation delegate = ((ChildWorkFlowOperation) op.getDelegate());
                entity.setNodeId(delegate.getNode().getNodeId());
                entity.setInstanceIds(delegate.getInstances().stream().map(WorkFlowInstance::getInstanceId).collect(Collectors.joining(",")));
                entity.setParameters(JacksonUtils.serialize((delegate.getContext().getRuntimeVariables())));

            } else {
                throw new WorkFlowRuntimeException("操作类型不支持");
            }
            entities.add(entity);
        }
        return entities;
    }

    @Override
    public void changeOperation(String code, Integer status, Integer beforeStatus) {
        WorkFlowOperationRepository repository = config.operationRepository();
        repository.updateStatus(code, status, beforeStatus);
    }


    @Override
    public void close() {
        pool.shutdown();
    }
}
