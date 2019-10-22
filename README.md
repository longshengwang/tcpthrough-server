# Tcp Through Server

## 1. 说明
该项目只是`server`端，还有client端，对应的地址是 ``.

这两个项目的主要功能是可以从一个公网IP地址来访问很多内网下服务(NAT后面的网络)，比如 `ssh`、`scp`、`http`等 ( 只要是tcp协议就可以 )

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




## 4. 测试结果 
 在 2015款 macbook pro i7 16g 上用`iperf3`测试
```
 上行: 13Gb/s
 下行: 13Gb/s
```
后来加了流量统计以后的速度有所下降，基本也能保证在 `10Gb/s` 上下。

>注：这里是小b哦。
