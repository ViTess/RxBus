# Android RxBus

使用注解(`@Subscribe`)标记事件接收方法，调用`RxBus.post()`发送值到接收方法。

支持无参和单参数形式。
支持基本类型和自定义类型传参。

使用
--------

* **注册和注销接收对象**

    在程序的入口处加入`RxBus.register(this)`注册对象
    
    在程序的销毁处加入`RxBus.unregister(this)`销毁对象
    
    例如，在`MainActivity`中：
    
    ```java
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RxBus.register(this);
        }
   
    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxBus.unregister(this);
    }
    ```
    
* **标记事件接收方法**
    
    只需要在已经注册的类中的方法上加上`@Subscribe`，并在其中加入区分用的tag
    
    例如：
    
    ```java
    @Subscribe("Test")
    public void test(int random) {
        Toast.makeText(this, "random:" + random, Toast.LENGTH_SHORT).show();
    }
    ```
    
    不设置tag，无参方法也可以：
    
    ```java
    @Subscribe
    public void test() {
        Toast.makeText(this, "void", Toast.LENGTH_SHORT).show();
    }
    ```
    
* **设置接收方法的运行线程**
    
    在标记了`@Subscribe`的方法再加上相应的注解标记线程即可，如：
    
    ```java
    @Subscribe
    @RxIO
    public void test() {
        //TODO
    }
    ```
    
    则该方法将运行在IO线程
    
    用于标记线程的注解如下：
    
    * RxMainThread
    * RxIO
    * RxComputation
    * RxNewThread
    * RxTrampoline
    * RxImmediate
    
    
* **发送消息**

    使用`RxBus.post(Tag)`和`RxBus.post(Tag,Value)`即可。其中`RxBus.post(Tag)`用于发送到无参方法。
    
Update
--------

#####16.11.22
    
1. 使用LruCache作缓存
2. 增加线程注解
3. 添加基本类型支持