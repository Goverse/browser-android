# Browser
> Browser是对android webview的封装，基于简单的接口配置就可以实现H5的加载和交互。

## 主进程
代码实现在*module browser_core*中

### 1. 初始化

```
    /**
    * Context context
    * UserAgent webview的useragent
    **/
    Browser.init(Context, UserAgent);
```
### 2. 启动Browser
```
// 继承BaseBrowserActivity
public class BrowserActivity extends BaseBrowserActivity {

    @Override
    public Browser onCreateBrowser(BrowserView browserView) {
        return Browser.with(this)
                .setView(browserView)
                .theme(Browser.Theme.FULL_SCREEN) 
                .supportDarkMode(true) 
                .enableJavaScript(true)
                .addJavaScriptExecutor(new IJsExecutor() {
                    @Override
                    public JsResult onMethodCall(Context context, String method, String param) {
                        return JsResult.NOT_INVOKED;
                    }
                })
                .build();
    }

    @Override
    public String url() {
        return url;
    }

}
```

实现方法onCreateBrowser，可以通过Builder模式为Browser设置属性，比如主题，暗色模式，开启Js接口等。
目前支持的主题有：NORMAL(Titlebar为原生)， FULL_SCREEN（全屏包含状态栏），SCREEN（全屏不包含状态栏），还可以实现Presentation接口自定义样式。

此外，可以通过addJavaScriptExecutor接口添加对应的Js的本地接口，具体的对象方法定义在JsScheduler中，由call(String method, String param)方法进行分发。

```
// 构造函数中可以传入js接口可以访问的安全域名
public IJsExecutor(String safeDomain) {
        this.safeDomain = safeDomain;
    }
```

## 子进程
代码实现在*module browser_process*中，基于browser_core

### 1. 初始化
```
    /**
    * Context context
    * UserAgent webview的useragent
    **/
    BpBrowser.getInstance().init(Context, UserAgent);
```

### 2. 启动Browser
> BpBrowser是Browser的代理对象主要负责其他进程与Browser的交互。

```
    /**
    * url 打开H5的地址
    * BrowserSetting browser属性设置
    * JsFunction js接口回调
    * PageInterceptListener 页面拦截的回调
    **/
    BpBrowser.getInstance().startBrowser(Url, BrowserSetting, JsFunction,PageInterceptListener);
```

