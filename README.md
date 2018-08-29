# EasyActivityLaunch
根据注解，自动生成Activity启动扩展方法

## 依赖
```
implementation 'com.tlz.easyactivitylaunch:easyactivitylaunch:0.0.2'
kapt 'com.tlz.easyactivitylaunch:easyactivitylaunch-processor:0.0.2'
```
## 使用
#### 在对应的Activity添加EasyLaunch注解
```
@EasyLaunch
class MainActivity : AppCompatActivity() {}

添加完注解重新Build下工程，就可以直接调用扩展方法：`launchMainActivity()`启动MainActivity.
```
#### EasyLaunch注解的可选参数:
```
nickName：生成的启动方法别名，默认为Activity名；
parameters：启动方法需要的参数数组；
parameterNames：参数名，如果填写该参数必须与parameters一一对应，默认名为param + 参数序号；
flags: Intent标记；
fragmentSupport：Fragment扩展支持，默认为false，如果为true，会生成一个Fragment的启动扩展方法；

@EasyLaunch(
        nickName = "CustomFuncName",
        parameters = [Int::class, String::class],
        parameterNames = ["customParam1", "customParam2"],
        fragmentSupport = true
)
class MainActivity : AppCompatActivity() {}
```
#### 如果需要Result，请使用`EasyLaunchForResult`注解
#### 如果要生成多个启动方法，还可以使用`EasyLaunch1`,`EasyLaunch2`注解
```
@EasyLaunch(parameters = [Int::class, String::class])
@EasyLaunchForResult(
        parameters = [Int::class, Array<String>::class],
        fragmentSupport = true
)
@EasyLaunch1(
        nickName = "CustomFuncName",
        parameters = [Intent::class, Bundle::class],
        parameterNames = ["customParam1", "customParam2"],
        fragmentSupport = true
)
@EasyLaunch2(
        parameters = [IntArray::class, Double::class],
        fragmentSupport = true
)
class MainActivity : AppCompatActivity() {}
```
