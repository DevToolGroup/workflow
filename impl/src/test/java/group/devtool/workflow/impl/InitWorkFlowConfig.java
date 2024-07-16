/*
 * WorkFlow is a fully functional, non BPMN, lightweight process engine framework developed in Java language, which can be embedded in Java applications and run as a service in servers or clusters.
 *
 * License: GNU GENERAL PUBLIC LICENSE, Version 3, 29 June 2007
 * See the license.txt file in the root directory or see <http://www.gnu.org/licenses/>.
 */
package group.devtool.workflow.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Supplier;

import group.devtool.workflow.engine.WorkFlowTransaction;
import group.devtool.workflow.engine.exception.WorkFlowException;
import group.devtool.workflow.impl.mapper.WorkFlowMapper;
import group.devtool.workflow.impl.repository.WorkFlowDefinitionRepository;
import group.devtool.workflow.impl.repository.WorkFlowRepository;
import group.devtool.workflow.impl.repository.WorkFlowSchedulerRepository;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.Before;

import group.devtool.workflow.engine.WorkFlowCallback;

public abstract class InitWorkFlowConfig {

	public WorkFlowConfigurationImpl dbConfig = WorkFlowConfigurationImpl.CONFIG;

	@Before
	public void init() throws SQLException {
		JdbcDataSource ds = new JdbcDataSource();
		ds.setURL("jdbc:h2:mem:workflow;MODE=MySQL;DB_CLOSE_DELAY=-1");

		try (Connection connection = ds.getConnection()) {
			Statement statement = connection.createStatement();

			// 流程定义
			statement.execute("CREATE TABLE IF NOT EXISTS `wf_definition` (" +
							"  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '数据库主键'," +
							"  `code` varchar(64) NOT NULL COMMENT '流程定义编码'," +
							"  `name` varchar(255) NOT NULL COMMENT '流程定义名称'," +
							"  `version` int unsigned NOT NULL COMMENT '流程定义版本'," +
							"  `state` varchar(1) NOT NULL COMMENT '流程定义状态'," +
							"  `node_code` varchar(64) DEFAULT NULL COMMENT '父流程节点定义编码'," +
							"  `root_code` varchar(64) NOT NULL COMMENT '根流程定义编码'," +
							"  PRIMARY KEY (`id`)," +
							"  UNIQUE KEY `ix_definition_code_version` (`root_code`, `code`,`version`)" +
							") ");

			// 流程节点定义
			statement.execute("CREATE TABLE IF NOT EXISTS `wf_node_definition` (" +
							"  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '数据库主键'," +
							"  `code` varchar(64) NOT NULL COMMENT '流程节点定义编码'," +
							"  `name` varchar(64) NOT NULL COMMENT '流程节点定义名称'," +
							"  `type` varchar(16) NOT NULL COMMENT '流程节点定义类型'," +
							"  `config` blob COMMENT '流程节点定义配置'," +
							"  `version` int unsigned NOT NULL COMMENT '流程节点定义关联的流程定义版本'," +
							"  `definition_code` varchar(64) NOT NULL COMMENT '流程节点定义关联的流程定义编码'," +
							"  `root_definition_code` varchar(64) NOT NULL COMMENT '根流程定义编码'," +
							"  PRIMARY KEY (`id`)," +
							"  KEY `ix_node_definition_code_version` (`definition_code`,`version`) USING BTREE," +
							"  KEY `ix_root_definition_code_version` (`root_definition_code`,`version`) USING BTREE" +
							") ");

			// 流程连线定义
			statement.execute("CREATE TABLE IF NOT EXISTS `wf_link_definition` (" +
							"  `id` int unsigned NOT NULL AUTO_INCREMENT," +
							"  `code` varchar(64) NOT NULL COMMENT '连线编码'," +
							"  `source` varchar(64) NOT NULL COMMENT '连线起始节点定义编码'," +
							"  `target` varchar(64) NOT NULL COMMENT '连线目标节点定义编码'," +
							"  `parser` varchar(32) NOT NULL COMMENT '连线表达式解析器类型'," +
							"  `expression` varchar(3000) COMMENT '连线条件表达式'," +
							"  `version` int unsigned NOT NULL COMMENT '连线关联的流程定义的版本'," +
							"  `definition_code` varchar(64) NOT NULL COMMENT '连线关联的流程定义的编码'," +
							"  `root_definition_code` varchar(64) NOT NULL COMMENT '连线关联的根流程定义的编码'," +
							"  PRIMARY KEY (`id`)," +
							"  KEY `ix_link_definition_code_version` (`definition_code`,`version`) USING BTREE," +
							"  KEY `ix_root_link_definition_code_version` (`root_definition_code`,`version`) USING BTREE" +
							") ");

			statement.execute("CREATE TABLE IF NOT EXISTS `wf_instance` (" +
							"  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '数据库主键'," +
							"  `instance_id` varchar(64) NOT NULL COMMENT '流程实例ID'," +
							"  `state` varchar(8) NOT NULL COMMENT '流程实例状态'," +
							"  `scope` varchar(8) NOT NULL COMMENT '流程实例范围'," +
							"  `root_instance_id` varchar(64) NOT NULL COMMENT '根流程实例ID'," +
							"  `parent_task_id` varchar(64) DEFAULT NULL COMMENT '嵌套子流程节点任务ID'," +
							"  `definition_code` varchar(64) NOT NULL COMMENT '关联流程定义的编码'," +
							"  `root_definition_code` varchar(64) NOT NULL COMMENT '关联根流程定义的编码'," +
							"  `definition_version` int unsigned NOT NULL COMMENT '关联的流程定义版本'," +
							"  PRIMARY KEY (`id`)," +
							"  UNIQUE KEY `ix_instance_id` (`instance_id`) USING BTREE," +
							"  KEY `ix_root_instance_id` (`root_instance_id`,`instance_id`) USING BTREE" +
							") ");

			statement.execute("CREATE TABLE IF NOT EXISTS `wf_instance_node` (" +
							"  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '数据库主键'," +
							"  `node_id` varchar(64) NOT NULL COMMENT '流程节点实例关联的流程节点ID'," +
							"  `node_code` varchar(64) NOT NULL COMMENT '流程节点实例关联的流程节点编码'," +
							"  `node_state` varchar(8) NOT NULL COMMENT '流程节点实例关联的节点状态'," +
							"  `node_class` varchar(16) NOT NULL COMMENT '流程节点实例关联的节点类型'," +
							"  `config` varchar(5000) COMMENT '流程节点实例配置'," +
							"  `version` int unsigned NOT NULL default 0 COMMENT '流程节点实例版本'," +
							"  `instance_id` varchar(64) NOT NULL COMMENT '流程节点实例关联的流程实例ID'," +
							"  `root_instance_id` varchar(64) NOT NULL COMMENT '流程节点实例关联的根流程实例ID'," +
							"  PRIMARY KEY (`id`)," +
							"  KEY `ix_root_instance_id_node_code` (`root_instance_id`,`node_code`) USING BTREE," +
							"  KEY `ix_root_node_instance_id_node_id` (`root_instance_id`,`node_id`) USING BTREE" +
							") ");

			statement.execute("CREATE TABLE IF NOT EXISTS  `wf_instance_task` (" +
							"  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '数据库主键'," +
							"  `task_id` varchar(64) NOT NULL COMMENT '流程任务实例ID'," +
							"  `task_class` varchar(16) NOT NULL COMMENT '流程任务实例类型'," +
							"  `task_state` varchar(8) NOT NULL COMMENT '流程任务实例状态'," +
							"  `complete_user` varchar(255) DEFAULT NULL COMMENT '流程任务实例完成用户'," +
							"  `complete_time` bigint unsigned DEFAULT NULL COMMENT '流程任务实例完成时间'," +
							"  `config` varchar(5000) COMMENT '流程任务实例配置'," +
							"  `node_id` varchar(64) NOT NULL COMMENT '流程任务实例关联的流程节点ID'," +
							"  `node_code` varchar(64) NOT NULL COMMENT '流程任务实例关联的流程节点ID'," +
							"  `instance_id` varchar(64) NOT NULL COMMENT '流程任务实例关联的流程实例ID'," +
							"  `root_instance_id` varchar(64) NOT NULL COMMENT '流程任务实例关联的根流程实例ID'," +
							"  PRIMARY KEY (`id`)," +
							"  UNIQUE KEY `ix_task_id` (`task_id`)," +
							"  KEY `ix_root_instance_id_node_id` (`root_instance_id`,`node_id`) USING BTREE," +
							"  KEY `ix_root_instance_id_task_id` (`root_instance_id`,`task_id`) USING BTREE" +
							") ");

			statement.execute("CREATE TABLE IF NOT EXISTS `wf_instance_delay_task` (" +
							"  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '数据库主键'," +
							"  `item_id` varchar(64) NOT NULL COMMENT '流程延时任务ID'," +
							"  `delay` BIGINT NOT NULL COMMENT '流程延时任务时间'," +
							"  `state` varchar(8) NOT NULL COMMENT '流程延时任务状态'," +
							"  `task_id` varchar(64) NOT NULL COMMENT '流程任务实例ID'," +
							"  `root_instance_id` varchar(64) NOT NULL COMMENT '流程任务实例关联的根流程实例ID'," +
							"  PRIMARY KEY (`id`)," +
							"  UNIQUE KEY `ix_delay_task_id` (`task_id`)," +
							"  KEY `ix_root_instance_id_item_id` (`root_instance_id`,`item_id`) USING BTREE," +
							"  KEY `ix_delay_state` (`delay`,`state`) USING BTREE" +
							") ");
			statement.execute("CREATE TABLE IF NOT EXISTS `wf_instance_variable` (" +
							"  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '数据库主键'," +
							"  `name` varchar(255) NOT NULL COMMENT '变量名'," +
							"  `value` varchar(5000) NOT NULL COMMENT '变量值'," +
							"  `type` varchar(6) NOT NULL COMMENT '变量类型 GLOBAL：全局，LOCAL：局部'," +
							"  `root_instance_id` varchar(64) NOT NULL COMMENT '变量关联的根流程实例ID'," +
							"  PRIMARY KEY (`id`)" +
							") ");
		} catch (SQLException e) {
			throw e;
		}
		WorkFlowTransactionTest dbTransaction = new WorkFlowTransactionTest(ds);
		dbConfig.setDBTransaction(dbTransaction);
		dbConfig.setMapper(dbTransaction.supplier());
		dbConfig.setSupplier(new WorkFlowIdSupplierImpl());
		dbConfig.setDefinitionRepository(new WorkFlowDefinitionRepository());
		dbConfig.setDefinitionService(new WorkFlowDefinitionServiceImpl());
		dbConfig.setFactory(new WorkFlowFactory());
		dbConfig.setRepository(new WorkFlowRepository());
		dbConfig.setSchedulerRepository(new WorkFlowSchedulerRepository());
		//    WorkFlowSchedulerImpl scheduler = new WorkFlowSchedulerImpl();
		//    scheduler.start();
		//    dbConfig.setTaskScheduler(scheduler);
		dbConfig.setService(new WorkFlowServiceImpl());
		dbConfig.setCallback(new WorkFlowCallback.EmptyWorkFlowCallback());
	}

	public static class WorkFlowTransactionTest implements WorkFlowTransaction {

		private final DefaultSqlSessionFactory factory;

		private final Configuration configuration;

		private ThreadLocal<SqlSession> SESSION = new ThreadLocal<>();

		public WorkFlowTransactionTest(JdbcDataSource datasource) {
			Environment environment = new Environment("h2", new JdbcTransactionFactory(), datasource);
			this.configuration = new Configuration(environment);
			this.factory = new DefaultSqlSessionFactory(configuration);
			configuration.addMapper(WorkFlowMapper.class);
		}

		@Override
		public <T> T doInTransaction(WorkFlowTransactionOperate<T> operate) throws WorkFlowException {
			SqlSession session = factory.openSession();
			SESSION.set(session);
			T result = null;
			try {
				result = operate.apply();
				session.commit();
			} catch (Exception e) {
				session.rollback();
				throw e;
			} finally {
				session.close();
				SESSION.remove();
			}
			return result;
		}

		public Supplier<WorkFlowMapper> supplier() {

			return new Supplier<WorkFlowMapper>() {
				@Override
				public WorkFlowMapper get() {
					SqlSession session = SESSION.get();
					return configuration.getMapper(WorkFlowMapper.class, session);
				}
			};
		}

	}
}
