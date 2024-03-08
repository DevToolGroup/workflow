package group.devtool.workflow.core;

import java.io.Serializable;

public interface WorkFlowTaskJavaDelegate {
  
  Serializable apply(WorkFlowContext context);
}
