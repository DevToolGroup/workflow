package group.devtool.workflow.engine;

import group.devtool.workflow.engine.exception.OperationException;
import group.devtool.workflow.engine.operation.RetryWorkFlowOperation;
import group.devtool.workflow.engine.operation.WorkFlowOperation;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * 调度流程操作
 */
public interface WorkFlowDispatch {

    static AbstractWorkFlowDispatch of(WorkFlowConfiguration configuration) {
        return new RetryWorkFlowDispatch(new DefaultWorkFlowDispatch(configuration));
    }

    WorkFlowConfiguration getConfig();

    /**
     * 流程操作持久化，为后续重试准备。
     *
     * 注意：如果采用重试，注意该方法在事务内，不需要额外的开启事务
     *
     * @param operations 流程操作
     */
    void addOperation(WorkFlowOperation... operations);

    /**
     * 回调持久化，为后续重试准备。
     *
     * 注意：如果采用重试，注意该方法在事务内，不需要额外的开启事务
     *
     * @param event   时间类型
     * @param context 上下文
     */
    void addCallback(WorkFlowCallback.WorkFlowEvent event, WorkFlowContextImpl context);


    @Slf4j
    abstract class AbstractWorkFlowDispatch implements WorkFlowDispatch {

        private final WorkFlowConfiguration configuration;

        protected AbstractWorkFlowDispatch(WorkFlowConfiguration configuration) {
            this.configuration = configuration;
        }

        @Override
        public WorkFlowConfiguration getConfig() {
            return configuration;
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

        protected abstract void doDispatch();

        public abstract void retryDispatch(WorkFlowOperation retryOperation);

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

    @Slf4j
    class DefaultWorkFlowDispatch extends AbstractWorkFlowDispatch {
        private final LinkedBlockingDeque<WorkFlowOperation> operations;

        public DefaultWorkFlowDispatch(WorkFlowConfiguration configuration) {
            super(configuration);
            operations = new LinkedBlockingDeque<>();

        }

        @Override
        public void addOperation(WorkFlowOperation... ops) {
            operations.addAll(Arrays.asList(ops));
        }

        @Override
        protected void doDispatch() {
            WorkFlowConfiguration config = super.getConfig();

            WorkFlowTransaction dbTransaction = config.dbTransaction();
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

        @Override
        public void retryDispatch(WorkFlowOperation retryOperation) {
            operations.add(retryOperation);
            doDispatch();
        }

        @Override
        public void addCallback(WorkFlowCallback.WorkFlowEvent event, WorkFlowContextImpl context) {
            super.getConfig().callback().callback(event, context);
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
        protected void doDispatch() {
            this.delegate.doDispatch();
        }

        @Override
        public void retryDispatch(WorkFlowOperation retryOperation) {
            this.delegate.retryDispatch(retryOperation);
        }

        @Override
        protected void doOperationBefore(WorkFlowOperation operation) {
            RetryWorkFlowOperation retryOperation = (RetryWorkFlowOperation) operation;
            retryOperation.setRunning();
            config.retry().changeOperation(retryOperation.getCode(), retryOperation.getStatus());
        }

        @Override
        protected void doOperationAfter(WorkFlowOperation operation) {
            RetryWorkFlowOperation retryOperation = (RetryWorkFlowOperation) operation;
            retryOperation.setSuccess();
            config.retry().changeOperation(retryOperation.getCode(), retryOperation.getStatus());
        }

        @Override
        protected void doOperationException(WorkFlowOperation operation) {
            RetryWorkFlowOperation retryOperation = (RetryWorkFlowOperation) operation;
            retryOperation.setFail();
            config.retry().changeOperation(retryOperation.getCode(), retryOperation.getStatus());
        }

        @Override
        public void addOperation(WorkFlowOperation... ops) {
            config.retry().addOperation(ops);
            this.delegate.addOperation(ops);
        }

        @Override
        public void addCallback(WorkFlowCallback.WorkFlowEvent event, WorkFlowContextImpl context) {
            config.retry().addCallback(event, context);
        }
    }
}
