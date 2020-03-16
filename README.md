<<<<<<< HEAD
## alevin2

1. summaryResult的输出结果不显示, 在测试用例中已经可以调出结果 Finish
2. summaryResult中的数据不符合预期, 使用小规模网络进行测试 Finish
	I) 为什么多次映射不会累计资源 -> 这是因为对于一个对象 其状态变量curNetIt没有重置所致使的
	II) 为什么影视不成功 还在占用资源呢 -> 这和上面是一个原因，需要重置processedLinks和mappedLinks两个变量，因为mappedLinks这个变量受到前面没有映射成功的虚拟网络影响，这样mappedLinks这个变量会比processedLinks要小，从整体上来看
	III) 断言出错 图中没有找到起点和终点 -> 这是因为在节点映射的时候没有加入因为规则不满足的节点(筛选后未加入)
3. 在链路映射方面还需要改进，比如使用断路法结合BFS来找最短路径 Finish
	I) 链路计算超时 -> 这是因为初始化时候没有将source加入visited集合中 出现循环指向
4. 加入上下文保存和恢复内容，比如底层网络和虚拟网络，以及时间序列 Finish
4. 加入其他算法比较 Finish
	I) Greedy算法为什么报错npe -> 因为将节点映射部分改动了 
	II) Greedy算法节点映射不成功 -> 由于距离因素，而虚拟节点的坐标没有设置，显得距离偏大
	III) 子图同构算法也出现没有重组变量的问题
	IV) 为什么修改distance会影响Greedy算法  -> 因为distance也是Greedy算法的一个参数
5. 咨询下nodeMapping那段代码是否可以那么更改 Finish
	I) 可以那么修改
	
6. 完成算法的分析阶段 包括复杂度分析和性能分析，最好有较多的数学表达公式 Finish
	I) 已经完成了复杂度分析 (T1)
7. 完成仿真实验部分的书写 Finish
8. 画出节点映射和链路映射的图分析 (T2)
9. 图形展示时候subgraph or subgraphisomorphism (T3)
10. 实验只是一次结果 是否要多几次，还有关于acceptance ration为1的情况 (T4)

## 完善alevin仿真实验
1. 进行实验的调整
=======
# AEF
铏氭嫙缃戠粶鏄犲皠绠楁硶
>>>>>>> f7e1e78e3ddd298f58ac202d1c4d6bdd93032b8a
