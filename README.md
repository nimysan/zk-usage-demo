### Zookeeper Program

[Barrier](https://zookeeper.apache.org/doc/r3.7.0/zookeeperTutorial.html)

[典型应用场景](https://zhuanlan.zhihu.com/p/67654401)

```
java -jar zookeeper-demo-1.0-SNAPSHOT-jar-with-dependencies.jar bTest localhost 2
```

### 使用规则 ###

1. 确保每个进程使用一个独立的Zookeeper Session(在使用多线程模拟的环境下一定要注意)

2. 这个实现是不可重入锁

