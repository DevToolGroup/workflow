package group.devtool.workflow.engine;

import group.devtool.workflow.engine.exception.OperationException;
import group.devtool.workflow.engine.operation.RetryWorkFlowOperation;
import group.devtool.workflow.engine.operation.WorkFlowOperation;
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


	abstract class AbstractWorkFlowDispatch implements WorkFlowDispatch {

		private final WorkFlowConfiguration configuration;

		private final Deque<WorkFlowOperation> queue;

		protected AbstractWorkFlowDispatch(WorkFlowConfiguration configuration) {
			this.configuration = configuration;
			queue = new LinkedList<>();
		}

		@Override
		public WorkFlowConfiguration getConfig() {
			return configuration;
		}

		@Override
		public void addOperation(WorkFlowOperation... operations) {
			queue.addAll(Arrays.asList(operations));
		}

		protected void doDispatchBefore() {
			// do nothing
		}

		public void dispatch(WorkFlowOperation boot) {
			doDispatchBefore();

			// 启动流程调度
			addOperation(boot);

			WorkFlowTransaction dbTransaction = getConfig().dbTransaction();

			while (!queue.isEmpty()) {
				WorkFlowOperation operation = queue.pop();
				dbTransaction.doInTransaction(() -> {
					try {
						doOperationBefore(operation);
						operation.operate(this);
						doOperationAfter(operation);
					} catch (OperationException e) {
						doOperationException(operation);
					}
					return null;
				});
			}

			doDispatchAfter();
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

		public RetryWorkFlowDispatch(DefaultWorkFlowDispatch delegate) {
			super(delegate.getConfig());
			this.delegate = delegate;
		}

		@Override
		public void dispatch(WorkFlowOperation boot) {
			this.delegate.dispatch(boot);
		}

		@Override
		protected void doOperationBefore(WorkFlowOperation operation) {
			RetryWorkFlowOperation retryOperation = (RetryWorkFlowOperation) operation;
			retryOperation.setRunning();
			getConfig().retry().changeOperation(retryOperation);
		}

		@Override
		protected void doOperationAfter(WorkFlowOperation operation) {
			RetryWorkFlowOperation retryOperation = (RetryWorkFlowOperation) operation;
			retryOperation.setSuccess();
			getConfig().retry().changeOperation(retryOperation);
		}

		@Override
		protected void doOperationException(WorkFlowOperation operation) {
			RetryWorkFlowOperation retryOperation = (RetryWorkFlowOperation) operation;
			retryOperation.setFail();
			getConfig().retry().changeOperation(retryOperation);
		}

		@Override
		public void addOperation(WorkFlowOperation... operations) {
			getConfig().retry().addOperation(of(operations));
			this.delegate.addOperation(operations);
		}

		public WorkFlowOperation[] of(WorkFlowOperation... operations) {
			WorkFlowIdSupplier supplier = getConfig().idSupplier();
			return Arrays.stream(operations).map(op -> new RetryWorkFlowOperation(op, supplier.getId())).toArray(WorkFlowOperation[]::new);
		}
	}

}
