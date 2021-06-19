# My Words

## 基本信息
- 当前版本：5.10
- 入坑时间：2021-06-17

## 进度表


|       时间        |       进度        |
|       ---         |       ---         |
|   2021年6月17日   | 跑通例子；摸索结构中  |
|   2021年6月18日   | 大致摸清结构；发现pf动图功能  |
|   2021年6月19日   |-|


## 惊喜榜
1. 运行效率高。跑NSGAIIStudy能吃满CPU（100%）；
2. Lab的“一键跑实验”很方便，最后以HTML生成的统计信息很直观，甚至有tex文件生成；
3. 我错了，在Examples中的<algorithm>WithChartsRunner有动图。用XCharts实现的，看上去很香。虽然不是一键API类型但看上去要用也不难。


## 失望榜
1. ~~本身好像没有生成pf动图和idg过程图的功能。~~

## 已探索的坑
1. AbstractAlgorithmRunner的printQualityIndicators函数，不能自动识别分隔符（已知有单空格' '、双空格'  '、逗号','）；目测也不能转换科学计算格式的字符串。