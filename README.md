# Jerry
## 使用
```
new Jerry()
                .service()
                .mapping(new Object() {
                    //do anything
                })
                .build()
                .start();
```
请求和对象方法之间的映射关系通过注解指定：
```
new Jerry()
                .service()
                .mapping(new Object() {
                    //将当前成员注解为静态资源根目录设置
                    @root
                    private String root="/home/sxy/static";
                    
                    //建立被注解方法与请求之间的映射关系
                    @url(value ="/",method = "post",type = "text/json")
                    public String hello(){
                        return "{\"info\":\"hello world\"}";
                    }
                })
                .build()
                .start();
```
可以省略method和type参数：
```
new Jerry()
                .service()
                .mapping(new Object() {
                    @url("/")
                    public String hello(){
                        return "{\"info\":\"hello world\"}";
                    }
                })
                .build()
                .start();
```
通过方法参数获取请求和响应对象：
```
new Jerry()
                .service()
                .mapping(new Object() {
                    @url("/")
                    public String hello(HttpRequest request){
                        return request.requestLine();
                    }
                })
                .build()
                .start();
```
## 完整示例
```
new Jerry()
                .service()
                .mapping(new Object(){
                    @url("/")
                    public String hello(){
                        return "hello world";
                    }
                })
                .apply()
                .apply()
                .maxThreads(5)
                .setKeepAlive(true)
                .backlog(150)
                .port(4000)
                .build()
                .service()
                .mapping(new Object(){
                    @url("/show")
                    public String show(HttpRequest request){
                        return request.requestLine();
                    }
                })
                .build()
                .logStyle(new SimpleLogStyle(){
                    @Override
                    public Level level() {
                        return Level.FINE;
                    }

                    @Override
                    public Formatter format() {
                        return new LogFormatter();
                    }
                })
                .commandPort(8080)
                .charset(StandardCharsets.UTF_8)
                .start();
```
## 联系作者
    QQ: 305352505
    Mail: TSOTHsxy.163.com