# HuYanSession3

**dev分支**

这里不做想详细的解释和介绍，

---

## 参数

对于部分参数，有默认值，可省略不填。

`|`  指可选，左边或右边选择一个即可
`+` 指可填写多个，一般中间用` `隔开。
`(xx)` 指必填，括号内内容必须填写。
`[xxx]`指可选填写。

#### 匹配方式

* `1|精准` 精准匹配(默认)
* `2|模糊` 模糊匹配，包含触发词即匹配成功。
* `3|头部` 头部匹配，从前往后匹配触发词即匹配成功。
* `4|尾部` 尾部匹配，从后往前匹配触发词即匹配成功。
* `5|正则` 正则匹配，按照正则匹配结果。

#### 重写

对于部分只能存在一条的信息，在参数集里面添加重写状态即可重写写入该信息。

* `%|rewrite` 启用重写。

#### 动态消息

不同于壶言会话2的自动识别动态消息，壶言会话3这边需要手动指定是否启用动态消息。

* `dt|动态|dynamic` 启用动态消息。

#### 本地缓存

由于腾讯服务器储存图片有概率过期，因此设计本地缓存功能，在config中有默认开启配置，默认不开启。
也可以根据单个消息默认设定开启与否。

* `ca|缓存|cache` 启用本地缓存。

#### 转换方式

消息在匹配时的转换方式

* `MIRAICODE` miraiCode(默认)。
* `STRING` String。
* `CONTENT` content。
* `JSON` json。

#### 作用域

* `global` 全局。
* `global-(id)|global-(at)` 全局的某个用户，可填号码，可at人。
* `group` 当前群(默认)。
* `member-(id)|member-(at)` 某个群友。
* `list-(id)` 自定义群列表。
* `users-(id)` 自定义用户列表。
* `members-(id)` 自定义某个群的群用户列表。

#### 触发概率

* `probability-(0.0~1.0)` 消息的触发概率。

#### 是否随机

* `random` 用于多词条的开启随机触发功能

---

## 动态消息类型

针对上一代的动态消息使用体验，这个版本也进行了一次优化

现格式为：

`${prefix.suffix}`

|   前缀    |    后缀    | 含义                        | 案例                     |
|:-------:|:--------:|---------------------------|------------------------|
|   at    |   this   | at当前发消息的这个人               | `${at.this}`           |
|         |   all    | at全体(慎用!)                 | `${at.all}`            |
|         |   (qq)   | at这个qq用户                  | `${at.572490972}`      |
|  user   |    id    | 获取发送者的QQ号                 | `${user.id}`           |
|         |   name   | 发送者的名称                    | `${user.name}`         |
|         |  avatar  | 发送者的头像                    | `${user.avatar}`       |
|         |  title   | 发送者的头衔(仅限群)               | `${user.title}`        |
|  group  |    id    | 群号(仅限群)                   | `${group.id}`          |
|         |   name   | 群名称(仅限群)                  | `${group.name}`        |
|         |  avatar  | 群头像(仅限群)                  | `${group.avatar}`      |
|         |  owner   | 群主名称(仅限群)                 | `${group.owner}`       |
|  time   |   now    | 当前时间(默认格式)                | `${time.now}`          |
|         |  timer   | 当前的时间戳                    | `${time.timer}`        |
|         | (format) | 当前时间的自定义格式                | `${time.HH:mm:ss.SSS}` |
|  mate   | (number) | 正则回流编号<br/>(仅限正则匹配中包含匹配块) | `${mate.1}`            |
| message |   this   | 当前的消息                     | `${message.this}`      |
|         | reverse  | 反转的消息(按照消息顺序反转)           | `${message.reverse}`   |
|  owner  |    id    | 主人的id(得与机器人是好友)           | `${owner.id}`          |
|         |   name   | 主人的名称(得与机器人是好友)           | `${owner.name}`        |
|         |  avatar  | 主人的头像(得与机器人是好友)           | `${owner.avatar}`      |

对于一对一消息回复，如果想启用动态消息的匹配，得才参数中添加`dt`来启用动态消息识别！

## 指令

### 消息功能

权限id：`session`

#### 一对一消息

权限id：`hh`

| 指令                      | 含义          | 案例                |
|-------------------------|-------------|-------------------|
| `%xx (a) (b) [params]+` | 学习一个一对一回复   | `%xx a b`         |
| `%xx [a]`               | 对话的方式学习     | `%xx`,`%xx a`     |
| `学习 (a) (b) [params]+`  | 同上          | `学习 啊啊啊 哦哦哦`      |
| `-xx (a) [scope\|id]`   | 删除一个一对一回复^1 | `-xx a`,`-xx a 5` |
| `删除 (啊啊啊) [scope\|id]`  | 同上          | `删除 啊啊啊`          |
| `%%xx`                  | 刷新单一消息缓存    | `%%xx`            |

##### 参数

params 支持列表：

* 匹配方式
* 重写
* 动态消息
* 本地缓存
* 转换方式
* 作用域
* 触发概率

动态消息支持列表：

* 全部

#### 一对多消息

权限id：`dct`

| 指令                              | 含义               | 案例                        |
|---------------------------------|------------------|---------------------------|
| `%dct [trigger]`                | 进入多词条学习状态(3分钟超时) | `%dct`,`%dct nova的错`      |
| `学习多词条`                         | 同上               | `学习多词条`                   |
| `-dct (trigger\|id-(id)) [id]`  | 删除多词条            | `-dct 群典`,`-dct id-2 5`   |
| `删除多词条 (trigger\|id-(id)) [id]` | 同上               | `删除多词条 群典`,`删除多词条 id-2 5` |
| `%%dct`                         | 刷新多词条缓存          | `%%dct`                   |


params 支持列表：

* 匹配方式
* 重写
* 本地缓存
* 作用域
* 触发概率

多词条默认自动识别动态消息！

动态消息支持列表：

* 全部

##### 群典功能

这是一个非常有意思的功能！

在配置中启动该功能后，对着机器人在线期间的消息回复一个`批准入典`， 将会默认生成一个触发词为`群典`(在配置中自定义)的多词条集

这个`群典`只属于当前群，意味着对于不同的群，可以有不同的群典！

+=[记录群友的丢人瞬间！]=+

#### 定时消息

权限id：`ds`

---

### 权限功能

权限id：`admin`

| 指令                                    | 含义          | 案例                         |
|---------------------------------------|-------------|----------------------------|
| `+(scope)-(at\|qq) (权限id) [权限id]`     | 为该作用域添加一个权限 | `+global-@572490972 admin` |
| `添加权限(scope)-(at\|qq)  (权限id) [权限id]` | 同上          | `添加权限@572490972 amind`     |
| `-(scope)-(at\|qq)  (权限id) [权限id]`    | 为该作用域删除一个权限 | `-global-@572490972 admin` |
| `删除权限(scope)-(at\|qq)  (权限id) [权限id]` | 同上          | `删除权限@572490972 admin `    |

#### 分组功能

### 插件指令

* `hys v` 查询当前壶言会话3的版本。

