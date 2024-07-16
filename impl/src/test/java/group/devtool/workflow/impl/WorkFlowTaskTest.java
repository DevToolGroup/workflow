/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.impl;

import group.devtool.workflow.engine.WorkFlowContext;
import group.devtool.workflow.engine.exception.NotFoundWorkFlowVariable;
import group.devtool.workflow.engine.exception.NotUserTaskPermission;
import group.devtool.workflow.impl.runtime.*;
import org.junit.Assert;
import org.junit.Test;

import group.devtool.workflow.engine.runtime.UserWorkFlowTask.UserWorkFlowTaskConfig;
import group.devtool.workflow.engine.WorkFlowContextImpl;
import group.devtool.workflow.engine.runtime.WorkFlowTaskJavaDelegate;
import group.devtool.workflow.engine.WorkFlowVariable;
import group.devtool.workflow.impl.runtime.UserWorkFlowTaskImpl.UserWorkFlowTaskConfigImpl;

public class WorkFlowTaskTest extends InitWorkFlowConfig {

  @Test
  public void testStart() {
    WorkFlowContextImpl context = new WorkFlowContextImpl("start", WorkFlowVariable.global(WorkFlowContext.USER, "admin"));
		StartWorkFlowTaskImpl task = new StartWorkFlowTaskImpl("start", "start", "start", "start");
		task.complete(context);
		Assert.assertTrue(task.completed());
	}

  @Test
  public void testEnd() {
    WorkFlowContextImpl context = new WorkFlowContextImpl("end");
		EndWorkFlowTaskImpl task = new EndWorkFlowTaskImpl("end", "end", "end", "end");
		task.complete(context);
		Assert.assertTrue(task.completed());
	}

  @Test
  public void testChild() {
    WorkFlowContextImpl context = new WorkFlowContextImpl("child");
		ChildWorkFlowTaskImpl task = new ChildWorkFlowTaskImpl("child", "child", "child", "child");
		task.complete(context);
		Assert.assertTrue(task.completed());
	}

  @Test
  public void testUser() {
    UserWorkFlowTaskConfig config = new UserWorkFlowTaskConfigImpl("u1");
    WorkFlowContextImpl context = new WorkFlowContextImpl("child");
		UserWorkFlowTaskImpl task = new UserWorkFlowTaskImpl("child", "child",config, "child", "child");
		// 缺少用户
		Assert.assertThrows(NotFoundWorkFlowVariable.class, () -> task.complete(context));
		context.addRuntimeVariable(WorkFlowVariable.global(WorkFlowContext.USER, "admin"));
		// 用户无权限
		Assert.assertThrows(NotUserTaskPermission.class, () -> task.complete(context));
		context.addRuntimeVariable(WorkFlowVariable.global(WorkFlowContext.USER, "u1"));

		task.complete(context);
		Assert.assertTrue(task.completed());
	}

  @SuppressWarnings("unchecked")
  @Test
  public void testTask() {
    String className = "group.devtool.workflow.impl.WorkFlowTaskTest$HelloWorldTask";
		JavaWorkFlowTaskImpl.JavaTaskWorkFlowTaskConfigImpl config = new JavaWorkFlowTaskImpl.JavaTaskWorkFlowTaskConfigImpl(className, false,"return");
    WorkFlowContextImpl context = new WorkFlowContextImpl("child");
		JavaWorkFlowTaskImpl task = new JavaWorkFlowTaskImpl("child", "child", config, "child", "child");
		task.complete(context);
		Assert.assertTrue(task.completed());
		Object output = context.lookup("return");
		Assert.assertEquals("hello world", output);
	}

  public static class HelloWorldTask implements WorkFlowTaskJavaDelegate {

    @Override
    public String apply(WorkFlowContextImpl context) {
      System.out.println("call hello world java task");
      return "hello world";
    }

  }

}
