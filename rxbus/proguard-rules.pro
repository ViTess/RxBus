# keep RxBus Proxy
-keep public class * extends vite.rxbus.BusProxy { public <init>(); }

# keep RxBus Library
-keep class vite.rxbus.*

# keep the method of annotating tags
-keepclasseswithmembernames class * { @vite.rxbus.* <methods>; }