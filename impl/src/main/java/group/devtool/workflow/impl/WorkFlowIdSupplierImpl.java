/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.impl;

import group.devtool.workflow.engine.WorkFlowIdSupplier;

import java.util.UUID;

/**
 * 基于UUID的流程ID默认实现
 */
public class WorkFlowIdSupplierImpl implements WorkFlowIdSupplier {

	@Override
	public String getInstanceId() {
		return UUID.randomUUID().toString();
	}

	@Override
	public String getNodeId() {
		return UUID.randomUUID().toString();
	}

	@Override
	public String getTaskId() {
		return UUID.randomUUID().toString();
	}

	@Override
	public String getCode() {
		return UUID.randomUUID().toString();
	}

}
