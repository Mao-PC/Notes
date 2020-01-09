- 检查是否已经安装过mysql
    ```shell
    rpm -qa | grep mysql
    ```
- 如果已经安装就执行卸载
    ```shiell
    rpm -e --nodeps xxx
    ```
- 查询所有Mysql对应的文件夹
    ```shell
    whereis mysql
    ```
    如果有就使用 `rm` 删除 
    校验是否删除完毕 `whereis mysql`, `find / -name mysql`

- 检查mysql用户组和用户是否存在，如果没有，则创建
    ```shell
    cat /etc/group | grep mysql
    cat /etc/passwd | grep mysql
    groupadd mysql
    useradd -r -g mysql mysql
    ```

---

docker

- 配置文件
    ```shell
    mkdir -p /usr/local/etc/db/mysql/conf.d
    ```

- 编辑`conf.d`
    ```conf
    [mysqld]
    # 表名不区分大小写
    lower_case_table_names=1 
    #server-id=1
    datadir=/var/lib/mysql
    #socket=/var/lib/mysql/mysqlx.sock
    #symbolic-links=0
    # sql_mode=NO_ENGINE_SUBSTITUTION,STRICT_TRANS_TABLES 
    [mysqld_safe]
    log-error=/var/log/mysqld.log
    pid-file=/var/run/mysqld/mysqld.pid
    ```

- 数据卷
    ```shell
    mkdir -p /usr/local/etc/db/mysql/dockerV
    ```

- 启动, 设置密码 `123456`

    ```shell
    docker run -it --name mysql \
    --restart=always --privileged=true \
    -p 3306:3306 \
    -v /usr/local/etc/db/mysql/my.cof:/etc/my.cnf  \
    -v /usr/local/etc/db/mysql/dockerV:/var/lib/mysql \
    -v /usr/local/etc/db/mysql/log/err.log:/var/log/mysqld.log \
    -e MYSQL_ROOT_PASSWORD=123456 \
    -d mysql
    ```
