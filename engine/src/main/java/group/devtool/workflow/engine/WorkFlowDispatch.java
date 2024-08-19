package group.devtool.workflow.engine;

import group.devtool.workflow.engine.exception.OperationException;
import group.devtool.workflow.engine.operation.RetryWorkFlowOperation;
import group.devtool.workflow.engine.operation.WorkFlowOperation;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;

/**
 * 调度流程操作
 */
public interface WorkFlowDispatch {

    static AbstractWorkFlowDispatch of(WorkFlowConfiguration configuration) {
        return new RetryWorkFlowDispatch(new DefaultWorkFlowDispatch(configuration));
    }

    WorkFlowConfiguration getConfig();

    void addOperation(WorkFlowOperation... operations);

    void addCallback(WorkFlowCallback.WorkFlowEvent workFlowEvent, WorkFlowContextImpl context);


    @Slf4j
    abstract class AbstractWorkFlowDispatch implements WorkFlowDispatch {

        private final WorkFlowConfiguration configuration;

        private final Deque<WorkFlowOperation> operations;

        private final Deque<WorkFlowCallbackPayload> callbacks;

        protected AbstractWorkFlowDispatch(WorkFlowConfiguration configuration) {
            this.configuration = configuration;
            operations = new LinkedList<>();
            callbacks = new LinkedList<>();
        }

        @Override
        public WorkFlowConfiguration getConfig() {
            return configuration;
        }

        @Override
        public void addOperation(WorkFlowOperation... ops) {
            operations.addAll(Arrays.asList(ops));
        }

        @Override
        public void addCallback(WorkFlowCallback.WorkFlowEvent event, WorkFlowContextImpl context) {
            callbacks.add(new WorkFlowCallbackPayload(event, context));
        }

        protected void doDispatchBefore() {
            // do nothing
        }

        public void dispatch(WorkFlowOperation boot) {
            doDispatchBefore();
            addOperation(boot);
            doDispatch();
            doDispatchAfter();
        }

        private void doDispatch() {
            WorkFlowTransaction dbTransaction = getConfig().dbTransaction();
            while (!operations.isEmpty()) {
                WorkFlowOperation operation = operations.pop();
                dbTransaction.doInTransaction(() -> {
                    try {
                        doOperationBefore(operation);
                        operation.operate(this);
                        doOperationAfter(operation);
                    } catch (OperationException e) {
                        log.error("流程操作执行异常. 异常信息: ", e);
                        doOperationException(operation);
                    }
                    return null;
                });
            }
        }

        public void retryDispatch(WorkFlowOperation retryOperation) {
            operations.add(retryOperation);
            doDispatch();
        }

        protected void doOperationBefore(WorkFlowOperation operation) {
            // do nothing
        }

        protected void doOperationAfter(WorkFlowOperation operation) {
            // do nothing
        }

        protected void doOperationException(WorkFlowOperation operation) {
            // do nothing
        }

        protected void doDispatchAfter() {
            // do nothing
        }

    }

    class DefaultWorkFlowDispatch extends AbstractWorkFlowDispatch {

        public DefaultWorkFlowDispatch(WorkFlowConfiguration configuration) {
            super(configuration);
        }

    }

    @Slf4j
    class RetryWorkFlowDispatch extends AbstractWorkFlowDispatch {

        private final AbstractWorkFlowDispatch delegate;

        private final WorkFlowConfiguration config;

        public RetryWorkFlowDispatch(DefaultWorkFlowDispatch delegate) {
            super(delegate.getConfig());
            this.delegate = delegate;
            this.config = delegate.getConfig();
        }

        @Override
        public void dispatch(WorkFlowOperation boot) {
            this.delegate.dispatch(boot);
        }

        @Override
        protected void doOperationBefore(WorkFlowOperation operation) {
            RetryWorkFlowOperation retryOperation = (RetryWorkFlowOperation) operation;
            retryOperation.setRunning();
            config.retry().changeOperation(retryOperation.getCode(), retryOperation.getStatus(), 0);
        }

        @Override
        protected void doOperationAfter(WorkFlowOperation operation) {
            RetryWorkFlowOperation retryOperation = (RetryWorkFlowOperation) operation;
            retryOperation.setSuccess();
            config.retry().changeOperation(retryOperation.getCode(), retryOperation.getStatus(), 2);
        }

        @Override
        protected void doOperationException(WorkFlowOperation operation) {
            RetryWorkFlowOperation retryOperation = (RetryWorkFlowOperation) operation;
            retryOperation.setFail();
            config.retry().changeOperation(retryOperation.getCode(), retryOperation.getStatus(), 0);
        }

        @Override
        public void addOperation(WorkFlowOperation... ops) {
            config.retry().addOperation(of(ops));
            this.delegate.addOperation(ops);
        }

        public WorkFlowOperation[] of(WorkFlowOperation... operations) {
            WorkFlowIdSupplier supplier = config.idSupplier();
            return Arrays.stream(operations)
                    .map(op -> new RetryWorkFlowOperation(op, supplier.getId()))
                    .toArray(WorkFlowOperation[]::new);
        }
    }

    @Getter
    class WorkFlowCallbackPayload {
        private final WorkFlowCallback.WorkFlowEvent event;

        private final WorkFlowContextImpl context;

        public WorkFlowCallbackPayload(WorkFlowCallback.WorkFlowEvent event, WorkFlowContextImpl context) {
            this.event = event;
            this.context = context;
        }
    }
}
