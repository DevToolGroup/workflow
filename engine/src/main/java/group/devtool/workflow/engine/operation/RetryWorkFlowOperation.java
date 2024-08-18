package group.devtool.workflow.engine.operation;

import group.devtool.workflow.engine.WorkFlowDispatch;
import group.devtool.workflow.engine.exception.OperationException;
import lombok.Getter;

@Getter
public class RetryWorkFlowOperation implements WorkFlowOperation {

	private final String id;

	private final WorkFlowOperation delegate;

	private Integer status;

	public RetryWorkFlowOperation(WorkFlowOperation delegate, Integer status, String id) {
		this.delegate = delegate;
		this.status = status;
		this.id = id;
	}

	public RetryWorkFlowOperation(WorkFlowOperation delegate, String id) {
		this(delegate, 0, id);
	}

	@Override
	public void operate(WorkFlowDispatch dispatch) throws OperationException {
		this.delegate.operate(dispatch);
	}

	public void setRunning() {
		this.status = 2;
	}

	public void setSuccess() {
		this.status = 1;
	}

	public void setFail() {
		this.status = -1;
	}
}