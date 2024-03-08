package group.devtool.workflow.core;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 流程上下文，保存流程运行过程中涉及的流程实例信息，节点信息，变量信息
 */
public class WorkFlowContext implements Serializable {

  private String position;

  private String instanceId;

  private final String rootInstanceId;

  private final List<WorkFlowVariable> variables;

  private final Map<String, Object> instanceVariable;

  private final Map<String, Map<String, Object>> nodeVariable;

  private final Map<String, Object> localVariables;

  private final List<WorkFlowVariable> suspendVariables;

  public WorkFlowContext(String rootInstanceId, WorkFlowVariable... variables) {
    this.rootInstanceId = rootInstanceId;
    this.variables = new ArrayList<>();

    this.instanceVariable = new HashMap<>();
    this.nodeVariable = new HashMap<>();

    this.localVariables = new HashMap<>();
    this.suspendVariables = new ArrayList<>();
    this.add(variables);
  }

  public WorkFlowContext(String rootInstanceId) {
    this.rootInstanceId = rootInstanceId;
    this.variables = new ArrayList<>();
    this.instanceVariable = new HashMap<>();
    this.nodeVariable = new HashMap<>();
    this.localVariables = new HashMap<>();
    this.suspendVariables = new ArrayList<>();
  }

  public List<WorkFlowVariable> variables() {
    return variables;
  }

  public Map<String, Object> localVariables() {
    return localVariables;
  } 

  public Map<String, Map<String, Object>> nodeVariables() {
    return nodeVariable;
  }
  
  public Map<String, Object> instanceVariables() {
    return instanceVariable;
  }

  public void add(WorkFlowVariable... vars) {
    for (WorkFlowVariable variable: vars) {
      variables.add(variable);
      instanceVariable.put(variable.getName(), variable.getValue());
      Map<String, Object> values = nodeVariable.getOrDefault(variable.getNode(), new HashMap<>());
      values.put(variable.getName(), variable.getValue());
      nodeVariable.put(variable.getNode(), values);
    }
  }

  public Object lookup(String name) {
    if (localVariables.containsKey(name)) {
      return localVariables.get(name);
    }
    Map<String, Object> nv = nodeVariable.getOrDefault(position, new HashMap<>());
    if (nv.containsKey(name)) {
      return nv.get(name);
    }
    return instanceVariable.get(name);
  }

  public void localVariable(WorkFlowVariable... variables) {
    suspendVariables.addAll(Arrays.asList(variables));
    for (WorkFlowVariable variable: variables) {
      localVariables.put(variable.getName(), variable.getValue());
    }
  }

  public void position(String code) {
    position = code;
  }

  public String getPosition() {
    return position;
  }

  public void instanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public String instanceId() {
    return instanceId;
  }

  public String rootInstanceId() {
    return rootInstanceId;
  }

  public boolean isChild() {
    return null == rootInstanceId || instanceId != rootInstanceId;
  }

  public void snapshot(String taskId) {
    for (WorkFlowVariable variable: suspendVariables) {
      variable.bindTask(taskId, position);
      add(variable);
    }
    suspendVariables.clear();
    localVariables.clear();
  }
}
