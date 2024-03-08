package group.devtool.workflow.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.Before;

import group.devtool.workflow.core.WorkFlowCallback;

public abstract class WorkFlowBeforeTest {
  
  public WorkFlowConfigurationImpl dbConfig = WorkFlowConfigurationImpl.CONFIG;

  @Before
  public void init() throws SQLException {
    JdbcDataSource ds = new JdbcDataSource();
    ds.setURL("jdbc:h2:mem:workflow;MODE=MySQL;DB_CLOSE_DELAY=-1;");

    try (Connection connection = ds.getConnection()) {
      Statement statement = connection.createStatement();
      statement.execute(
          "create table if not exists wf_id_increment (id int not null auto_increment primary key, time bigint)Engine=InnoDB CHARSET=utf8mb4");
      // 流程定义
      statement.execute(
          "create table if not exists wf_definition (id int not null auto_increment primary key, code varchar(64) not null, name varchar(255) not null, version int not null, state varchar(1) not null, node_code varchar(64), root_code varchar(64) not null, CONSTRAINT ix_definition_code_version UNIQUE KEY (code,version))Engine=InnoDB CHARSET=utf8mb4");
      // 流程节点定义
      statement.execute(
          "create table if not exists wf_node_definition (id int not null auto_increment primary key, code varchar(64) not null, name varchar(255) not null, type varchar(16) not null, config blob, version int not null, definition_code varchar(64) not null, root_definition_code varchar(64) not null)Engine=InnoDB CHARSET=utf8mb4");
      // 流程连线定义
      statement.execute(
          "create table if not exists wf_link_definition (id int not null auto_increment primary key, source varchar(64) not null, target varchar(64) not null, parser varchar(32) not null, expression varchar(3000) not null, version int not null, definition_code varchar(64) not null, root_definition_code varchar(64) not null)Engine=InnoDB CHARSET=utf8mb4");

      statement.execute(
          "CREATE TABLE if not exists wf_instance_task (id INT UNSIGNED auto_increment NOT NULL primary key, task_id varchar(64) NOT NULL, task_class varchar(16) NOT NULL,task_state varchar(8) NOT NULL, complete_user varchar(255) NULL, complete_time BIGINT UNSIGNED NULL, config blob, node_code varchar(32) NOT NULL, node_class varchar(16) not null,node_state varchar(8) not NULL, instance_id varchar(64) NOT NULL,root_instance_id varchar(64) NOT NULL, CONSTRAINT wf_instance_task_id unique KEY (task_id)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;");

      statement.execute(
          "CREATE TABLE if not exists wf_instance_variable (id INT UNSIGNED auto_increment NOT NULL primary key, name varchar(255) NOT NULL, `value` BLOB NOT NULL, task_id varchar(64), node varchar(64) not null, root_instance_id varchar(64) NOT NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;");

      statement.execute(
          "CREATE TABLE if not exists wf_instance (id int unsigned auto_increment not null primary key, instance_id varchar(64) not null, state varchar(8) not null, root_instance_id varchar(64) not null, parent_task_id varchar(64), definition_code varchar(32) not null, definition_version int not null) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;");

      statement.execute(
          "CREATE TABLE if not exists wf_instance_delay_task (id int unsigned NOT NULL AUTO_INCREMENT primary key,item_id varchar(64) NOT NULL, delay BIGINT NOT NULL,state varchar(8) NOT NULL,task_id varchar(64) NOT NULL,root_instance_id varchar(64) NOT NULL, UNIQUE KEY ix_task_id (task_id),KEY ix_root_instance_id_item_id (root_instance_id,item_id) USING BTREE ,KEY ix_delay_state (delay,state) USING BTREE) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci");
      statement.execute(
          "create table if not exists wf_transaction_operation (id int unsigned auto_increment not null primary key, tx_id varchar(64) not null, type varchar(16) not null, state varchar(8) not null, instance_id varchar(64), task_id varchar(1000), variable_id varchar(1000), node_code varchar(64), root_instance_id varchar(64) not null, tx_timestamp bigint not null) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;");
    } catch (SQLException e) {
      throw e;
    }
    dbConfig.setDbTransaction(new MybatisSimpleTransaction(ds));
    dbConfig.setSupplier(new WorkFlowIdSupplierImpl());
    dbConfig.setDefinitionRepository(new WorkFlowDefinitionRepository());
    dbConfig.setDefinitionService(new WorkFlowDefinitionServiceImpl());
    dbConfig.setFactory(new WorkFlowFactory());
    dbConfig.setRepository(new WorkFlowRepository());
    dbConfig.setSchedulerRepository(new WorkFlowSchedulerRepository());
    WorkFlowSchedulerImpl scheduler = new WorkFlowSchedulerImpl();
    scheduler.start();
    dbConfig.setTaskScheduler(scheduler);
    dbConfig.setService(new WorkFlowServiceImpl());
    dbConfig.setTransaction(new MybatisWorkFlowTransaction());
    dbConfig.setCallback(new WorkFlowCallback.EmptyWorkFlowCallback());
  }
}
