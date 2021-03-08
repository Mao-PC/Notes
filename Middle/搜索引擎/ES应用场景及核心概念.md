[toc]

# ES 应用场景及核心概念



## 应用场景



- 给网站 / APP 添加搜索功能。
- 存储、分析数据。
- 管理、交互、分析空间信息，将 ES 用于 GIS。



## ES 简介



- Elasticsearch 是一个基于 Lucene 构建的开源、分布式、RESTful 接口全文检索引擎。
- Elasticsearch 也是一个分布式文档数据库。
- Elasticsearch 可以在很短的时间内存储、搜索大量数据。
- Elasticsearch 有很强的水平扩展能力。



架构图

<img src="https://cdn.nlark.com/yuque/0/2020/png/1206640/1600229789963-e465c942-4c5b-4b9d-b62f-982afa4fdf03.png?x-oss-process=image%2Fwatermark%2Ctype_d3F5LW1pY3JvaGVp%2Csize_14%2Ctext_5Y2O5aSP57Sr56m5%2Ccolor_FFFFFF%2Cshadow_50%2Ct_80%2Cg_se%2Cx_10%2Cy_10" style="zoom:50%;" />



### 安装 ES

- 新建 es 用户

  使用 root 用户执行

  ```shell
  useradd es
  passwd es
  ```

- 修改配置文件

  - 防止出现 

    `max virtual memory areas vm.max_map_count [65530] is too low, increase to at least [262144]`

    ```shell
    vi /etc/sysctl.conf
    # 在文件最后添加
    vm.max_map_count=262144
    # 生效
    sysctl -p
    ```

  - 修改进程参数

    ```shell
    vi /etc/security/limits.conf
    ## 添加如下内容:
    hard nofile 65536
    soft nofile 65536
    
    soft nproc 2048
    hard nproc 4096
    ```

    nproc  : 是操作系统级别对每个用户创建的进程数的限制

    nofile : 是每个进程可以打开的文件数的限制

      ```shell
    vi /etc/security/limits.d/90-nproc.conf
    # 将 soft nproc 1024 修改为
    soft nproc 4096
      ```

- 使用 es 用户登录

  - 安装并配置 Java 环境 (略)
  - 下载ES安装包  https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-6.3.2.tar.gz

  - 上传并解压安装包 

    ```shell
    tar -xvf elasticsearch-6.3.2.tar.gz
    ```

  - 修改配置文件

    ```shell
    vi config/elasticsearch.yml
    ```

    修改内容 (没有就添加) :

    ```yml
    # 集群名
    cluster.name: my-application
    # 当前节点名
    node.name: node-1
    # ip
    network.host: 0.0.0.0 
    # 端口
    http.port: 9200
    # 防止出现 `ERROR: bootstrap checks failed`
    bootstrap.memory_lock: false
    bootstrap.system_call_filter: false
    ```

- 启动 es

  ```shell
  bin/elasticsearch
  ```

  在浏览器中输入 访问 localhost:9200 能看到当前节点信息 json 说明启动成功

