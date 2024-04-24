
###使用手册

#### 1.运行第一个MainServer 设置port=6789
#### 2.运行第二个MainServer 设置port=6790
#### 3.运行第一个MyProxyServer


请求
```
http://localhost:8808/bmi
```
json参数
```
{
    "height": 3000,
    "weight": 41290
}
```
观察选择的后台服务请求落在哪一台MainServer上。
