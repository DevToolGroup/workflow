package group.devtool.workflow.impl;


/**
 * 流程节点间连线实体
 */
public class WorkFlowLinkDefinitionEntity {

  /**
   * 数据库主键
   */
  private Long id;

  /**
   * 来源节点编码
   */
  private String source;

  /**
   * 目标节点编码
   */
  private String target;

  /**
   * 流转条件
   */
  private String expression;

  /**
   * 条件执行器
   * 1. EPEL
   * 2. NORMAL
   */
  private String parser;

  /**
   * 版本号
   * 
   * 默认版本号：0
   */
  private Integer version;

  /**
   * 关联流程定义的流程定义编码
   */
  private String definitionCode;

  /**
   * 关联根流程定义的流程定义编码
   */
  private String rootDefinitionCode;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getTarget() {
    return target;
  }

  public void setTarget(String target) {
    this.target = target;
  }

  public String getExpression() {
    return expression;
  }

  public void setExpression(String expression) {
    this.expression = expression;
  }

  public String getParser() {
    return parser;
  }

  public void setParser(String parser) {
    this.parser = parser;
  }

  public String getDefinitionCode() {
    return definitionCode;
  }

  public void setDefinitionCode(String parentCode) {
    this.definitionCode = parentCode;
  }

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  public String getRootDefinitionCode() {
    return rootDefinitionCode;
  }

  public void setRootDefinitionCode(String rootDefinitionCode) {
    this.rootDefinitionCode = rootDefinitionCode;
  }
}
