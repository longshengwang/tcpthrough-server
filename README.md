# Tcp Through Server

## 1. 说明
该项目只是`server`端，还有client端，对应的地址是 [https://github.com/longshengwang/tcpthrough-client](https://github.com/longshengwang/tcpthrough-client`)。

这两个项目的主要功能是可以从一个公网IP地址来访问很多内网下服务(NAT后面的网络)，比如 `ssh`、`scp`、`http`、`vnc`等 ( 只要是tcp协议就可以 )

## 2. 编译

#### 2.1. gradle和java版本 
```
$ gradle -v

------------------------------------------------------------
Gradle 4.6
------------------------------------------------------------

Build time:   2018-02-28 13:36:36 UTC
Revision:     8fa6ce7945b640e6168488e4417f9bb96e4ab46c

Groovy:       2.4.12
Ant:          Apache Ant(TM) version 1.9.9 compiled on February 2 2017
JVM:          1.8.0_121 (Oracle Corporation 25.121-b13)
OS:           Mac OS X 10.15 x86_64
 
$ java -version
java version "1.8.0_121"
Java(TM) SE Runtime Environment (build 1.8.0_121-b13)
```

#### 2.2. 编译命令
```
gradle build
```

#### 2.3. 运行
>说明：采用了`gradle`的`application plugin`，所以可以生成用命令方式运行的zip和tar(目录是在 build/distribution 下)。
选择其中一种压缩包，解压直接直接运行 bin目录下的可执行文件(其实是脚本，windows是bat文件，unix是另一个shell文件)

在解压后的目录下运行如下命令
```
bin/server
```

- data server默认端口是 9009
- manage server默认端口是 9000
- http server默认端口是 8080

> 也可以指定端口，具体信息可以运行 ```bin/server -h```

## 3. HTTP接口

**注意**：HTTP服务目前运行在`127.0.0.1`上，所以如果该服务部署在公网上，用公网IP地址是无法访问的。如果想用公网访问，可以用nginx、haproxy等反向代理，不过目前http没有做用户验证，需要注意。

#### 3.1. 获取所有client的信息

1. 访问接口`GET http://localhost:8080/tcpth/list`
2. 返回信息
```
[
  {
    "proxy_port": "2242",         # 对外的代理端口
    "is_remote_manage": "true",   # client是否允许server端控制(下面的两个接口)
    "read_speed": "0KB/s",        # 读速度
    "name": "wls_home",           # client name(不可以重复)
    "out_connection_count": "0",  # 连接到 2242端口的连接数
    "local": "localhost:22",      # client的host，这个host也可以是client网络环境中的其他主机
    "write_speed": "0KB/s"        # 写速度
  }
]

```


#### 3.2. 增加代理信息
1. 访问接口
```
POST  http://localhost:8080/tcpth/register/add
{
	"name":"wls_home",
	"proxy_port" : "22112",
	"local_host": "localhost", # 这个host也可以是client网络环境中的其他主机
	"local_port": 223
}
```

2. 返回信息
true -> 成功
false -> 失败

#### 3.3. 删除代理信息
1. 访问接口
```
POST  http://localhost:8080/tcpth/register/delete
{
	"name":"wls_home",
	"proxy_port":"22112"
}
```
2. 返回信息
true -> 成功
false -> 失败

#### 3.4. 安全模式下增加信任IP
1. 访问接口
```
GET  http://localhost:8080/tcpth/auth/addtrustip/{client_name}/{ip_address}
```
2. 返回信息
true -> 成功
false -> 失败

#### 3.5. 安全模式下删除信任IP
1. 访问接口
```
GET  http://localhost:8080/tcpth/auth/rmtrustip/{client_name}/{ip_address}
```
2. 返回信息
true -> 成功
false -> 失败


## 4. 测试结果 
 在 2015款 macbook pro i7 16g 上用`iperf3`测试
```
 上行: 13Gb/s
 下行: 13Gb/s
```
后来加了流量统计以后的速度有所下降，基本也能保证在 `10Gb/s` 上下。

>注：这里是小b哦。

## 5. 亮点功能

1. 支持 http api 动态添加和删除代理信息(支持一个client可以代理多个内网端口)
2. 支持**安全模式**。只有在信任列表的IP地址才可以访问client (防止攻击的最好方式)
3. 支持速度限制，功能已经测试可用。只是目前在代码中没有限速，可自己修改代码，具体代码在server库中的`OuterServer`中，注释中有说明。
4. client可设定不允许server控制 (client端的`isRemoteManage`参数)
5. 支持查看实时速率(不需要用总量来计算)
6. 管理通道进行了SSL加密，防止注册信息被抓包
7. 数据平面和控制平面分离，提高性能。
8. 管理平面和控制平面都进行了安全性校验，不正确的连接会被kill掉，拒绝攻击。
9. Server可以增加密码校验，不允许其他的client注册。Server启动时候加上 `-s` 参数
10. 后续可开发总量控制，traffic shaping counter中可以获取总量，所以想要进行总量控制，也是很easy的。


## 6. 可再完善的功能
##### 6.1. 自己开发网页调用 http api 来显示代理信息
目前Server端的http server没有增加cookie和密码之类的校验，所以只允许运行在 localhost。
可以开发html 然后通过nginx反向代理来访问localhost http api。
安全性校验可以用base-auth。当然要记得开启 https(http的basic auth可是明文) 

##### 6.2. server增加用户登录管理，以及每个用户的登录页面。
这样就可以用管理员来设定每个用户的限额，每个用户也可以自己操作自己的client，比如增加代理和查看详情，以及 6.1 中提到的安全设定





