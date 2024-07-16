/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.engine.runtime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import group.devtool.workflow.engine.WorkFlowContextImpl;
import group.devtool.workflow.engine.exception.SerializeException;

import java.io.Serializable;

/**
 * 流程任务接口
 * 一个流程节点对应一个或多个流程任务
 */
public interface WorkFlowTask {

	ObjectMapper MAPPER = new ObjectMapper();

	/**
	 * @return 任务唯一ID
	 */
	String getTaskId();

	/**
	 * @return 节点ID
	 */
	String getNodeId();

	/**
	 * @return 流程节点编码
	 */
	String getNodeCode();

	/**
	 * @return 流程实例ID
	 */
	String getInstanceId();

	/**
	 * 变更流程任务状态
	 *
	 * @param context 流程上下文
	 * @ 流程异常
	 */
	void complete(WorkFlowContextImpl context) ;

	/**
	 * 判断流程任务是否已完成
	 *
	 * @return 流程任务已完成返回true，相反，返回false
	 */
	boolean completed();

	default String getConfig(WorkFlowTaskConfig target) throws SerializeException {
		try {
			return MAPPER.writeValueAsString(target);
		} catch (JsonProcessingException e) {
			throw new SerializeException("节点任务序列化异常：" + e.getMessage());
		}
	}

	String getRootInstanceId();

	Long getCompleteTime();

	String getCompleteUser();

	String getTaskClass();

	String getTaskConfig();

	/**
	 * 任务配置
	 */
	public interface WorkFlowTaskConfig extends Serializable {

	}

	enum WorkFlowTaskState {

		DOING,

		DONE

	}

}
