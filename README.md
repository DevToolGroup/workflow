### workflow 工作流引擎
[![](https://img.shields.io/badge/官网-DevTool-green)](http://devtoolgroup.github.io)
![](https://img.shields.io/badge/语言-Java-blue)
![](https://img.shields.io/badge/许可证-GPL-red)
![](https://img.shields.io/badge/版本-1.0_SNAPSHOT-orange)
![](https://img.shields.io/badge/代码-7.3K-green)

workflow是一个基于Java语言开发，轻量级的，非BPMN严格定义的工作流引擎框架，当前主要以嵌入Java应用程序中运行，后续迭代规划也可以作为服务器、集群中的服务运行。

### 简要说明
#### 许可证
    GPL 3.0

#### Java版本
    Java 8+

#### 功能特点
workflow是一个基于Java语言开发的流程引擎，其中数据存储采用Mybatis ORM，流程流转条件采用Spring El表达式，异步任务基于Java 并发线程库的ThreadPoolExecutor。

workflow主要节点类型支持如下：
  - 开始节点，有且仅有一个，不含子流程，每个子流程内部有且仅有一个开始节点
  - 用户节点，支持多个用户或者其他用户标识，支持通过条件
  - 事件节点，支持触发事件，当前事件触发需要调用接口
  - 任务节点，支持Java脚本，其他脚本可扩展
  - 延时任务节点，支持Java脚本，其他脚本可扩展
  - 条件子流程节点，支持配置多个子流程，每个子流程支持启动多个实例，支持启动条件
  - 结束节点，有且仅有一个，不含子流程，每个子流程内部有且仅有一个结束节点

节点类型扩展可参考任务节点 `TaskWorkFlowNode` 实现自定义配置。
> 扩展模型包括： WorkFlowNodeDefinition、 WorkFlowNode、 WorkFlowTask

### 快速开始
当前版本：SNAPSHOT，可直接下载代码，本地运行单元测试。

有问题非常欢迎提ISSUE，也可以进入[官网](http://devtoolgroup.github.io)加入交流群。

> 产投产前建议充分验证。

### 不同之处
在workflow中，只允许存在一个开始节点，结束节点，另外节点流转方向直接根据节点间连线的条件判断，没有引入网关（排他，包含，并行）的概念。

排他，包含，并行网关场景，通过以下方式实现：
1. 排他场景，通过约束两个节点之间只允许存在一条满足条件的连接线，当存在多条连接线满足条件时，直接抛出异常。
2. 包含/并行场景，通过并行的条件子流程实现，即在子流程的基础上，支持定义多个不同的子流程，每个子流程根据配置条件选择是否启动。


之所以不采用网关的概念，主要基于以下几点考虑：
1. 包含/并行网关，在长事务中并发操作时需要加锁（悲观/乐观）保证流程正常往下流转，如果不考虑外部锁（Redis锁），那么只能在流程实例的维度加锁，在流程实例加锁影响整体的并发性能，如果缩小加锁范围，可行的方案是在网关派生子流程实例，那么增加了一定的复杂度。
2. 在流程流转至包含网关汇聚节点时，由于无法直接计算汇聚节点的通过条件，需要遍历流程图，判断可达分支上是否还有活跃的节点，如果有则继续等待，直到所有活跃的分支节点到达汇聚节点，汇聚节点才继续往下流转，直观上，整体的性能不一定好。
3. 包含/并行网关，在部分分支往回流转，其他分支不受影响，整体流转逻辑不易理解，因此，在实践时往往要求分支上的节点不允许往回流转、要求网关对等出现等以降低理解的难度，同时在程序上又无法限制。

基于以上三点原因，舍弃了网关的概念，通过条件子流程，将并行分支约束在子流程内部，约束往回流转的情况，另外，因为子流程的原因，包含网关不再需要遍历流程图即可实现是否继续流转的判断，一定程度上提高了性能。

具体实现，请查看 [**说明文档**](http://devtoolgroup.github.io) 


### 沟通交流
[***交流地址***](http://devtoolgroup.github.io)

如果你也是一名热爱代码的朋友，非常非常欢迎你的加入一起讨论学习。

### 后续迭代
重点任务：
1. 递归改为迭代的方式。
2. 短事务改进。当前采用的长事务保证数据的一致性。
3. 缺陷修复支持。
4. 常用场景扩展支持

闲暇支持：
1. 前端设计页面，大部分项目都会换套自己喜欢的风格，这里不专门开发前端设计页面，后续有时间开发一个简易的设计页面
2. API设计
