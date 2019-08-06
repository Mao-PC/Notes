[TOC]

# checkout 和 stash

## checkout

**checkout** 的作用:

- 切换分支(参考: [分支基础操作](分支操作.md))
- 工作区取消修改
- 版本穿梭

### 工作区取消修改

操作过程:

1. 编辑 a.txt 文件, 内容为 aaa, 然后 add+commit
2. 在 a.txt 文件中增加一行, 内容为 bbb, 执行 add
3. 在 a.txt 文件中增加一行, 内容为 ccc

脚本执行如下:

```sh
mao@maopeichun MINGW64 ~/Dev/test/mygit (master)
$ echo aaa > a.txt

mao@maopeichun MINGW64 ~/Dev/test/mygit (master)
$ cat a.txt
aaa

mao@maopeichun MINGW64 ~/Dev/test/mygit (master)
$ git add .
warning: LF will be replaced by CRLF in a.txt.
The file will have its original line endings in your working directory

mao@maopeichun MINGW64 ~/Dev/test/mygit (master)
$ git commit -m init
[master (root-commit) 57ad08b] init
 1 file changed, 1 insertion(+)
 create mode 100644 a.txt

mao@maopeichun MINGW64 ~/Dev/test/mygit (master)
$ echo bbb >>a.txt

mao@maopeichun MINGW64 ~/Dev/test/mygit (master)
$ cat a.txt
aaa
bbb

mao@maopeichun MINGW64 ~/Dev/test/mygit (master)
$ git add .
warning: LF will be replaced by CRLF in a.txt.
The file will have its original line endings in your working directory

mao@maopeichun MINGW64 ~/Dev/test/mygit (master)
$ echo ccc >> a.txt

mao@maopeichun MINGW64 ~/Dev/test/mygit (master)
$ git status
On branch master
Changes to be committed:
  (use "git reset HEAD <file>..." to unstage)

        modified:   a.txt

Changes not staged for commit:
  (use "git add <file>..." to update what will be committed)
  (use "git checkout -- <file>..." to discard changes in working directory)

        modified:   a.txt


```

会出现如下的情况:

![](res/checkout.png)

执行 checkout

```sh
mao@maopeichun MINGW64 ~/Dev/test/mygit (master)
$ git checkout -- a.txt

mao@maopeichun MINGW64 ~/Dev/test/mygit (master)
$ cat a.txt
aaa
bbb
```

这里的 `checkout`是**取消工作区的修改**

再次修改 a.txt 文件, add, 可以使用 `git reset HEAD <file>`撤销

```sh
$ echo xxx > a.txt

mao@maopeichun MINGW64 ~/Dev/test/mygit (master)
$ git add .
warning: LF will be replaced by CRLF in a.txt.
The file will have its original line endings in your working directory

mao@maopeichun MINGW64 ~/Dev/test/mygit (master)
$ git reset head a.txt
Unstaged changes after reset:
M       a.txt

mao@maopeichun MINGW64 ~/Dev/test/mygit (master)
$ git status
On branch master
Changes not staged for commit:
  (use "git add <file>..." to update what will be committed)
  (use "git checkout -- <file>..." to discard changes in working directory)

        modified:   a.txt

no changes added to commit (use "git add" and/or "git commit -a")
```

注意, 这里的 `git reset head a.txt` 并没有还原 a.txt 文件, 而是 **取消了`add`** 操作

### 版本穿梭

当前的提交了 3 次

```sh
$ git log --pretty=oneline
8db41eb41c22321139e441221d36dc9c9e4f0754 (HEAD -> master) commit3
9630605871f121df58c282f98fd8f0ece878d9b9 commit2
57ad08b65c77c539ed01e866808a44e254d219c2 init
```

使用 `git checkout <sha1>`修改版本

```sh
$ git checkout 9630605871f121df58c282f98fd8f0ece878d9b9
Note: checking out '9630605871f121df58c282f98fd8f0ece878d9b9'.

You are in 'detached HEAD' state. You can look around, make experimental
changes and commit them, and you can discard any commits you make in this
state without impacting any branches by performing another checkout.

If you want to create a new branch to retain commits you create, you may
do so (now or later) by using -b with the checkout command again. Example:

  git checkout -b <new-branch-name>

HEAD is now at 9630605 commit2

```

根据 git 提示要注意:

1. 修改必须提交
2. 可以创建分支

测试修改:

```sh
$ cat a.txt
aaa
bbb

mao@maopeichun MINGW64 ~/Dev/test/mygit ((9630605...))
$ echo cc >> a.txt

mao@maopeichun MINGW64 ~/Dev/test/mygit ((9630605...))
$ git checkout 8db41e
error: Your local changes to the following files would be overwritten by checkout:
        a.txt
Please commit your changes or stash them before you switch branches.
Aborting

mao@maopeichun MINGW64 ~/Dev/test/mygit ((9630605...))
$ git commit -am 穿越提交
warning: LF will be replaced by CRLF in a.txt.
The file will have its original line endings in your working directory
[detached HEAD 5462b52] 穿越提交
 1 file changed, 1 insertion(+)

mao@maopeichun MINGW64 ~/Dev/test/mygit ((5462b52...))
$ git checkout 8db41e
Warning: you are leaving 1 commit behind, not connected to
any of your branches:

  5462b52 穿越提交

If you want to keep it by creating a new branch, this may be a good time
to do so with:

 git branch <new-branch-name> 5462b52

HEAD is now at 8db41eb commit3

mao@maopeichun MINGW64 ~/Dev/test/mygit ((8db41eb...))
$ git log --pretty=oneline
8db41eb41c22321139e441221d36dc9c9e4f0754 (HEAD, master) commit3
9630605871f121df58c282f98fd8f0ece878d9b9 commit2
57ad08b65c77c539ed01e866808a44e254d219c2 init
```

上述过程:

- 在过去的版本中修改 a.txt
- checkout 到最新版本
- git 提示必须提交过去版本的修改
- commit 修改
- checkout 到最新版本
- git log 发现并没有记录, 说明没有修改现在版本的文件

额, 这里有点像你穿越到过去后不论做了说明都不会对现在造成影响的赶脚啊..., 但是, 请注意在旧版本提交后执行`checkout`时的提示 :

```sh
mao@maopeichun MINGW64 ~/Dev/test/mygit ((5462b52...))
$ git checkout 8db41e
Warning: you are leaving 1 commit behind, not connected to
any of your branches:

  5462b52 穿越提交

If you want to keep it by creating a new branch, this may be a good time
to do so with:

 git branch <new-branch-name> 5462b52

HEAD is now at 8db41eb commit3
```

这里提示: `如果你想要5462b52新建分支, 可以使用 git branch <new-branch-name> 5462b52`

```sh
$ git branch parallelBranch 5462b52

mao@maopeichun MINGW64 ~/Dev/test/mygit ((9630605...))
$ git checkout  parallelBranch
Previous HEAD position was 9630605 commit2
Switched to branch 'parallelBranch'

mao@maopeichun MINGW64 ~/Dev/test/mygit (parallelBranch)
$ git log --pretty=oneline
5462b525f44670dbb9b4bdace3fd26a4cec74411 (HEAD -> parallelBranch) 穿越提交
9630605871f121df58c282f98fd8f0ece878d9b9 commit2
57ad08b65c77c539ed01e866808a44e254d219c2 init
```

执行后会产生一个新分支, 使用 `git log`查看发现这个分支上有了在旧版本的提交

这个能说什么呢... 你在过去做的事情虽然不能改变现在, 但是会产生一个平行宇宙 (分支) ?

好吧, 骚话不多说, checkout 穿梭版本主要用于创建新的分支

## stash

stash 保存现场

1. 建议: 在功能没有开发完成前不要 commit
2. (必须) 在没有 commit 之前, 不能 checkout 分支
