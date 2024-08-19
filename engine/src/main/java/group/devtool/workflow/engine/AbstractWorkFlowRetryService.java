package group.devtool.workflow.engine;

import group.devtool.workflow.engine.operation.RetryWorkFlowOperation;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public abstract class AbstractWorkFlowRetryService implements WorkFlowRetryService {

    @Override
    public void retryOperation() {
        List<RetryWorkFlowOperation> fails;
        Long lastId = null;
        do {
            fails = loadFailOperation(lastId);
            RetryWorkFlowOperation last = null;
            for (RetryWorkFlowOperation fail : fails) {
                last = fail;
                try {
                    execute(fail);
                } catch (Exception e) {
                    log.error("重试操作执行异常. 异常信息: ", e);
                }
            }
            if (last != null) {
                lastId = last.getId();
            }
        } while (!fails.isEmpty());
    }

    protected abstract void execute(RetryWorkFlowOperation fail);

    protected abstract WorkFlowConfiguration getConfig();

    protected abstract List<RetryWorkFlowOperation> loadFailOperation(Long lastId);

    public abstract void changeOperation(String code, Integer status, Integer beforeStatus);
}
