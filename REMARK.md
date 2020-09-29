## 同 key 优先级

默认

云端 > 本地

"指定绝对路径" > """file:./config/" > "file:./" > "classpath:/config/" > "classpath:/"




```
/**
 * 配置文件路径集合，格式为 [$key:][#priority:][path/]name[.extension]，
 * 其中 $key、#priority、path、extension 部分可选，name 部分必须，
 * 运行时会被转换为 {@link CaramelConfigPosition}。
 * $key: 用于为该配置文件指定一个为标识，必须以 $ 号开头，以 : 号结束，若不指定则以 name 为唯一标识，不同配置文件可以指定一样的 key，同 key 文件内容将合并；
 * #priority: 用于指定配置文件的优先级，必须以 # 号开头，以 : 号结束，若不指定则使用【内置默认优先级】；
 * path 用于指定配置文件所在路径，可以是绝对路径或 classpath 下的路径，若不指定则从约定路径中查找；
 * extension 用于指定配置文件扩展名，若不指定则使用约定扩展名匹配查找。
 */
private String[] locations;
```