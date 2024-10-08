/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.engine;

import java.io.Closeable;


public interface WorkFlowDelayTaskScheduler extends Closeable {

  boolean ready();

  void addTask(DelayItem item);

  /**
   * 延时事项接口
   */
  interface DelayItem {

    /**
     * @return 延时时间
     */
    Long getDelay() ;

    /**
     */
    void run();
  }

}
