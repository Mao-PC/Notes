[toc]

# MVCC多版本并发控制



MVCC, Multi-Version Concurrency Control, 即多版本并发控制



MVCC是一种并发控制的方法, 用于支持多事务并发操作和事务回滚等特性的机制



## 多版本设计



InnoDB MVCC 的实现基于 Undo log, 通过回滚段来构建需要的版本记录



InnoDB中每行记录都会有 3 个隐式字段, 分别为`DB_TRX_ID`, `DB_ROLL_PTR`, `DB_ROW_ID`, 如果在新建表时这些字段时会提示

```
1166 - Incorrect column name 'db_trx_id'
```

<img src="res/隐藏字段.png" alt="隐藏字段" style="zoom:67%;" />



这 3 个字段就是用于保存数据的版本记录

- insert 语句会在`undolog` 中记录 `undo_no table_id 数据的唯一键信息 事务ID`
- update 语句会在 `undolog` 中记录 `undo_no table_id 数据的唯一键信息 字段修改前的值 旧记录的事物ID 事务ID`
- delete 操作会在数据行上标记改数据要被删除



## 二级索引的多版本设计



当更新辅助索引列时, 旧的辅助索引记录将被删除标记, 插入新记录

如果二级索引被标记为删除, 或者二级索引被更新了, 则不会使用 **索引覆盖** 技术



