package group.devtool.workflow.engine;

import group.devtool.workflow.engine.operate.WorkFlowOperation;

import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;

/**
 * 调度流程操作
 */
public interface WorkFlowDispatch {

    static DefaultWorkFlowDispatch of(WorkFlowConfiguration configuration) {
        return new DefaultWorkFlowDispatch(configuration);
    }

    WorkFlowConfiguration getConfig();

    void addCallback(WorkFlowCallback.WorkFlowEvent event, WorkFlowContextImpl context);

    void addOperation(WorkFlowOperation... operations);


    abstract class AbstractWorkFlowDispatch implements WorkFlowDispatch {
        private final WorkFlowConfiguration configuration;

        private final Deque<WorkFlowOperation> queue;

        private final Deque<WorkFlowCallbackPayload> callbacks;

        protected AbstractWorkFlowDispatch(WorkFlowConfiguration configuration) {
            this.configuration = configuration;
            queue = new LinkedList<>();
            callbacks = new LinkedList<>();
        }

        @Override
        public WorkFlowConfiguration getConfig() {
            return configuration;
        }

        @Override
        public void addCallback(WorkFlowCallback.WorkFlowEvent event, WorkFlowContextImpl context) {
            callbacks.add(new WorkFlowCallbackPayload(event, context));
        }

        @Override
        public void addOperation(WorkFlowOperation... operations) {
            Collections.addAll(queue, operations);
        }

        public void dispatch() {
            dispatchBefore();
            while (!queue.isEmpty()) {
                try {
                    WorkFlowOperation operation = queue.pop();
                    operation.operate(this);
                    doCallback();
                } catch (Exception exception) {
                    // TODO add exception log
                    dispatchException();
                }
            }
            dispatchAfter();
        }

        private void doCallback() {
            callbacks.pop();
        }

        protected abstract void dispatchBefore();

        protected abstract void dispatchException();

        protected abstract void dispatchAfter();

    }

    class DefaultWorkFlowDispatch extends AbstractWorkFlowDispatch {

        public DefaultWorkFlowDispatch(WorkFlowConfiguration configuration) {
            super(configuration);
        }

        @Override
        protected void dispatchBefore() {
            // do nothing
        }

        @Override
        protected void dispatchException() {
            // do nothing
        }

        @Override
        protected void dispatchAfter() {
            // do nothing
        }

    }

    class WorkFlowCallbackPayload {

        private final WorkFlowCallback.WorkFlowEvent event;

        private final WorkFlowContextImpl context;

        public WorkFlowCallbackPayload(WorkFlowCallback.WorkFlowEvent event, WorkFlowContextImpl context) {
            this.event = event;
            this.context = context;
        }

        public WorkFlowCallback.WorkFlowEvent getEvent() {
            return event;
        }

        public WorkFlowContextImpl getContext() {
            return context;
        }
    }
}
