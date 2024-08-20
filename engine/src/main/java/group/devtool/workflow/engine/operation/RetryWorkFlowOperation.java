package group.devtool.workflow.engine.operation;

import group.devtool.workflow.engine.WorkFlowDispatch;
import group.devtool.workflow.engine.exception.OperationException;
import lombok.Getter;

@Getter
public class RetryWorkFlowOperation implements WorkFlowOperation {

	private final Long id;

	private final String code;

	private final WorkFlowOperation delegate;

	private Integer status;

	public RetryWorkFlowOperation(WorkFlowOperation delegate, Integer status, String code, Long id) {
		this.delegate = delegate;
		this.status = status;
		this.code = code;
		this.id = id;
	}

	public RetryWorkFlowOperation(WorkFlowOperation delegate, String code) {
		this(delegate, 0, code, null);
	}

	@Override
	public String getRootInstanceId() {
		return this.delegate.getRootInstanceId();
	}

	@Override
	public void operate(WorkFlowDispatch dispatch) throws OperationException {
		this.delegate.operate(dispatch);
	}

	public void setRunning() {
		this.status = 0;
	}

	public void setSuccess() {
		this.status = 1;
	}

	public void setFail() {
		this.status = -1;
	}
}