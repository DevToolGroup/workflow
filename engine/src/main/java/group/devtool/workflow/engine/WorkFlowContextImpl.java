/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.engine;


import group.devtool.workflow.engine.runtime.WorkFlowNode;
import group.devtool.workflow.engine.runtime.WorkFlowTask;

import java.io.Serializable;
import java.util.*;

/**
 * {@link WorkFlowContext} 默认实现。
 */
public class WorkFlowContextImpl implements WorkFlowContext {

    private String taskId;

    private String nodeCode;

    private String nodeId;

    private String instanceId;

    private final String rootInstanceId;

    private final Map<String, Serializable> globalVariables;

    private final transient List<WorkFlowVariable> runtimeVariables;

    public WorkFlowContextImpl(String rootInstanceId, WorkFlowVariable... variables) {
        this(rootInstanceId);
        for (WorkFlowVariable variable : variables) {
            this.globalVariables.put(variable.getName(), variable.getValue());
        }
    }

    public WorkFlowContextImpl(String rootInstanceId) {
        this.rootInstanceId = rootInstanceId;
        this.globalVariables = new HashMap<>();
        this.runtimeVariables = new ArrayList<>();
    }

    @Override
    public Map<String, Serializable> getVariableMap() {
        return globalVariables;
    }

    public List<WorkFlowVariable> getRuntimeVariables() {
        return runtimeVariables;
    }

    /**
     * 每次流程变量持久化后，调用该方法清空新增变量
     */
    public synchronized void clearRuntimeVariables() {
        runtimeVariables.clear();
    }

    public synchronized void addRuntimeVariable(WorkFlowVariable... vars) {
        for (WorkFlowVariable variable : vars) {
            globalVariables.put(variable.getName(), variable.getValue());
            runtimeVariables.add(variable);
        }
    }

    @Override
    public Object lookup(String name) {
        return globalVariables.get(name);
    }

    @Override
    public String getNodeCode() {
        return nodeCode;
    }

    @Override
    public String getNodeId() {
        return nodeId;
    }

    @Override
    public String getInstanceId() {
        return instanceId;
    }

    @Override
    public String getRootInstanceId() {
        return rootInstanceId;
    }

    @Override
    public boolean isChildInstance() {
        return null == rootInstanceId || !Objects.equals(instanceId, rootInstanceId);
    }

    @Override
    public String getTaskId() {
        return taskId;
    }

    public void setTask(WorkFlowTask task) {
        this.taskId = task.getTaskId();
        this.nodeId = task.getNodeId();
        this.nodeCode = task.getNodeCode();
        this.instanceId = task.getInstanceId();
    }

    public void setNode(WorkFlowNode node) {
        this.taskId = null;
        this.nodeId = node.getNodeId();
        this.nodeCode = node.getNodeCode();
        this.instanceId = node.getInstanceId();
    }

    public void setNodeCode(String nodeCode) {
        this.nodeCode = nodeCode;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }
}
