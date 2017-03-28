# Android RxBus

该项目基于[RxJava2](https://github.com/ReactiveX/RxJava) & [RxAndroid](https://github.com/ReactiveX/RxAndroid)，并且从[AndroidKnife/RxBus](https://github.com/AndroidKnife/RxBus)中学习而实现的。

使用annotation processing（注释处理）自动生成模板代码，避免了反射带来的性能影响。通过`@Subscribe`标记订阅方法，`@Rxthread`可设置订阅方法的运行线程，线程支持`RxJava`中提供的**6种线程**：`MainThread`、`IO`、`Computation`、`Single`、`NewThread`、`Trampoline`。

引用
---

在gradle中加入：

```groovy
dependencies {
  compile 'com.github.vitess:rxbus:2.0.0'
  annotationProcessor 'com.github.vitess:rxbus-compiler:2.0.0'
}
```

开发版本的快照可从[Sonatype's snapshots repository](https://oss.sonatype.org/content/repositories/snapshots/)中找到。

使用
---

在类的初始化处使用`RxBus.register`注册，并在类销毁的地方使用`RxBus.unregister`注销。注册后的类中的方法即可使用`@Subscribe`注释标记，此后在类以外的地方即可通过`RxBus.post`发射数据到指定方法中。

For example:

```java
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RxBus.register(this);
        //TODO something
        ...
        
        findViewById(R.id.button).setOnClickListenernew(View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RxBus.post("receiver1", 123);
                RxBus.post("This is post to receiver2");
                RxBus.post(new Object());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxBus.unregister(this);
    }

    @Subscribe("receiver1")
    @RxThread(ThreadType.IO)
    public void receiver1(int random) {
        Log.i("RxBus", "receiver1:" + Thread.currentThread().getName());
    }

    @Subscribe
    @RxThread(ThreadType.Single)
    public void receiver2(String str) {
        Log.i("RxBus", "receiver2:" + Thread.currentThread().getName());
    }
    
    @Subscribe
    public void receiver3(Object obj) {
    	Log.i("RxBus", "receiver3:" + Thread.currentThread().getName());
    }
}
```

TODO
---

**目前思路稍微有些瓶颈，如果有好点子或者有可改进的地方，欢迎pull request，thanks！**

* 增加单元测试
* 优化Processor性能
* 优化模板代码
* 优化Processor的缓存方式和生成模式
* 增加sticky事件支持
* 根据使用方式分别生成不同的Observable，使用频率较少的用`post`方法发射，每次独立生成`Single`完成操作；使用频率较高且生命周期较长的使用`continuePost`方法发射，仅生成`Processor`完成操作
* etc.

License
---

    Copyright 2015 Vincent Tam

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.