# AEF
虚拟网络映射算法

--帮助文档

1. 生成测试用例, 使用类vnreal.algorithms.myAEF.util.ProduceCase

   参数配置如下

   ```java
   Properties properties = new Properties();
   properties.put("snNodes", "100"); // 底层节点数
   properties.put("minVNodes", "5"); 
   properties.put("maxVNodes", "10"); // 上面参数和这个参数确定虚拟节点数U[5,10]
   properties.put("snAlpha", "0.5"); // 底层网络waxman模型的alpha参数，该值越大，边越多
   properties.put("vnAlpha", "0.5"); // 虚拟网络waxman模型的alhpa参数
   properties.put("snBeta", "0.1"); // 底层网络waxman模型的beta参数, 该值越大，长边越多
   properties.put("vnBeta", "0.25"); // 底层网络waxman模型的beta参数
   properties.put("bandwithResource", "20"); 
   properties.put("substrateBaseBandwithResource", "50"); // 上面参数和这个参数确定底层带宽资源U[50,70]
   return properties;
   ```

   虚拟网络有多个，到达时间和持续时间可通过如下配置

   ```java
   double arrive_lambda = 10.0 / 100; // 虚拟请求到达率，平均每100个时间单位到达10个请求
   double preserve_lambda = 1.0 / 1000; // 虚拟请求在底层网络持续时间，平均一个请求持续1000个时间单位
   int end = 2000; // 总的仿真时间，和测试相关
   ```

   产生的拓扑结构保存在results/file目录下，文件名以时间后缀命名，这里假设为c

2. 仿真实验，使用类vnreal.algorithms.myAEF.simulation.Run

   指定目标文件results/file/c为filename

   设置运行参数为paramter

   ```JAVA
   AlgorithmParameter algorithmParameter = new AlgorithmParameter();
   algorithmParameter.put("linkMapAlgorithm", "bfs"); // 链路映射使用算法，bfs或者dijkstra，默认是bfs
   algorithmParameter.put("distanceConstraint", "70.0"); // AEF算法的软距离约束，默认是70
   algorithmParameter.put("advanced", "false"); // 是否使用加强的子图同构算法，默认是false
   algorithmParameter.put("eppstein", "false"); // 链路映射是否采用eppstein算法，还可选ksp算法
   algorithmParameter.put("AEFAdvanced", "true"); // 是否是AEFAdance算法，还可选AEFBaseline算法
   return algorithmParameter;
   ```

   调用不同算法进行比较

   | 算法                  | 实现                                       |
   | ------------------- | ---------------------------------------- |
   | AEFBaseLine         | process(new AEFAlgorithm(parameter, false), filename) |
   | AEFAdvance          | process(new AEFAlgorithm(parameter, true), filename) |
   | SubgraphIsomorphism | process(new SubgraphIsomorphismStackAlgorithm(parameter), filename) |
   | Greedy              | process(new CoordinatedMapping(parameter), filename) |
   | NRM                 | process(new NRMAlgorithm(parameter), filename) |

运算结果保存在results/output目录下, 假设文件名为xxxaefBaseline.txt。对于保存的结果，可以使用该目录下自带的python脚本进行绘图分析, 比如python plotFor xxx rt|ar|r2c|ll