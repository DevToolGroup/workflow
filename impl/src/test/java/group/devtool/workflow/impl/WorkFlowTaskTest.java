package group.devtool.workflow.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import group.devtool.workflow.core.exception.WorkFlowException;
import org.junit.Assert;
import org.junit.Test;

import group.devtool.workflow.core.TaskWorkFlowNodeDefinition.JavaTaskConfig;
import group.devtool.workflow.core.UserWorkFlowTask.WorkFlowUserTaskConfig;
import group.devtool.workflow.core.WorkFlowContext;
import group.devtool.workflow.core.WorkFlowTaskJavaDelegate;
import group.devtool.workflow.core.WorkFlowVariable;
import group.devtool.workflow.impl.TaskWorkFlowNodeDefinitionImpl.JavaTaskConfigImpl;
import group.devtool.workflow.impl.UserWorkFlowTaskImpl.MybatisWorkFlowUserTaskConfig;

public class WorkFlowTaskTest extends WorkFlowBeforeTest {

  @Test
  public void testStart() throws WorkFlowException {
    WorkFlowContext context = new WorkFlowContext("start");
    context.localVariable(WorkFlowVariable.suspend(WorkFlowVariable.USER, "admin"));
		StartWorkFlowTaskImpl task = new StartWorkFlowTaskImpl("start", "start", "start");
		task.complete(context);
		Assert.assertTrue(task.completed());
	}

  @Test
  public void testEnd() throws WorkFlowException {
    WorkFlowContext context = new WorkFlowContext("end");
		EndWorkFlowTaskImpl task = new EndWorkFlowTaskImpl("end", "end", "end");
		task.complete(context);
		Assert.assertTrue(task.completed());
	}

  @Test
  public void testChild() throws WorkFlowException {
    WorkFlowContext context = new WorkFlowContext("child");
		ChildWorkFlowTaskImpl task = new ChildWorkFlowTaskImpl("child", "child", "child");
		task.complete(context);
		Assert.assertTrue(task.completed());
	}

  @Test
  public void testUser() throws WorkFlowException {
    WorkFlowUserTaskConfig config = new MybatisWorkFlowUserTaskConfig("u1", List.of("u1"), 1);
    WorkFlowContext context = new WorkFlowContext("child");
		UserWorkFlowTaskImpl task = new UserWorkFlowTaskImpl("child", config, "child", "child");
		// 缺少用户
		Assert.assertThrows(WorkFlowException.class, () -> task.complete(context));
		context.localVariable(WorkFlowVariable.suspend(WorkFlowVariable.USER, "admin"));
		// 用户无权限
		Assert.assertThrows(WorkFlowException.class, () -> task.complete(context));
		context.localVariable(WorkFlowVariable.suspend(WorkFlowVariable.USER, "u1"));
		task.complete(context);
		Assert.assertTrue(task.completed());
	}

  @SuppressWarnings("unchecked")
  @Test
  public void testTask() throws WorkFlowException {
    String className = "group.devtool.workflow.impl.WorkFlowTaskTest$HelloWorldTask";
    JavaTaskConfig config = new JavaTaskConfigImpl(className, false, "h");
    WorkFlowContext context = new WorkFlowContext("child");
		JavaWorkFlowTaskImpl task = new JavaWorkFlowTaskImpl("child", config, "child", "child");
		task.complete(context);
		Assert.assertTrue(task.completed());
		Map<String, String> output = (Map<String, String>) context.lookup("h");
		Assert.assertEquals("hello world", output.get("output"));
	}

  public static class HelloWorldTask implements WorkFlowTaskJavaDelegate {

    @Override
    public Serializable apply(WorkFlowContext context) {
      System.out.println("call hello world java task");
      HashMap<String, String> result = new HashMap<>();
      result.put("output", "hello world");
      return result;
    }

  }

}
