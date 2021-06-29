# My Words

## 基本信息
- 当前版本：5.10
- 入坑时间：2021-06-17

## 进度表


|       时间        |       进度        |
|       ---         |       ---         |
|   2021年6月17日   | 跑通例子；摸索结构中  |
|   2021年6月18日   | 大致摸清结构；发现pf动图功能  |
|   2021年6月20日   | 完成基本多任务支持；MFEA/D-DRA算法迁移基本完成 |
|   2021年6月29日   | 在自定义算法(MFEA/D-DRA)中实现measure runner(实时动图) |


## 惊喜榜
1. 运行效率高。跑NSGAIIStudy能吃满CPU（100%）；
2. Lab的“一键跑实验”很方便，最后以HTML生成的统计信息很直观，甚至有tex文件生成；
3. 我错了，在Examples中的<algorithm>WithChartsRunner有动图。用XCharts实现的，看上去很香。虽然不是一键API类型但看上去要用也不难。


## 失望榜
1. ~~本身好像没有生成pf动图和idg过程图的功能。~~
2. 有好多Derecated的类（如计算igd用到的Front类），而且暂时没找到有用代替的例子。
3. 内部API还是很混乱，标记了deprecated的类的耦合度还是很高（说的就是你Front）。
4. 各api对resource内文件的访问方式不统一。

## 已探索的坑
1. AbstractAlgorithmRunner的printQualityIndicators函数，不能自动识别分隔符（已知有单空格' '、双空格'  '、逗号','）；目测也不能转换科学计算格式的字符串。
   1. 尽量使用csv格式来记录pf；
   2. 这函数好像也deprecated了，可以不用管。但没找到代替的函数。
2. VectorUtils.readVectors函数读取的路径好像是以整个项目的根目录为起点，可直接以相对路径访问根目录下的resources文件夹，与框架内默认大量使用的ArrayFront不同。