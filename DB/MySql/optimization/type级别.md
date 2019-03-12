# type级别

 type类型 | 具体情况 | 结果表现 
-|-|-
system/const|当MySQL对查询某部分进行优化，并转换为一个常量时，使用这些类型访问。如将主键置于where列表中，MySQL就能将该查询转换为一个常量。system是const类型的特例，当查询的表只有一行的情况下， 使用system。 | 结果只有一条数据
eq_ref | 唯一性索引扫描，对于每个索引键，表中只有一条记录与之匹配。常见于主键或唯一索引扫描 | 结果多条, 但每条数据是唯一的
ref | 非唯一性索引扫描，返回匹配某个单独值的所有行。常见于使用非唯一索引即唯一索引的非唯一前缀进行的查找 | 结果多条, 每条数据是0条或多条
range | 检索指定范围的行, where 条件后是一个查询范围, 如: between, > , < , >= ..., 特殊: in 有时会失效, 从而转为 all级别 |  --
index | 查询全部索引中数据 | select tid from teacher; tid为teacher表的索引
all | 查询表中所有的数据 | select name from teacher; name不是teacher表的索引
