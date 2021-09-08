@[TOC](高德Demo)

# 安装
首先安装Demo

然后配置环境 将手机中的定位权限打开

>权限设置
![在这里插入图片描述](https://img-blog.csdnimg.cn/4f1ae352ae0d4b9487e850066852eaeb.jpg?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBATlA4MO-8hQ==,size_20,color_FFFFFF,t_70,g_se,x_16#pic_center)


# 使用方法
# 发展
## 一、创建应用
首先，我们需要一个key，类似于获得使用高德api的资格，因此要去官网注册，点击高德地图官网进入

点击右上角头像进入应用管理 点击我的应用，创建新应用

新建应用

点击添加key

你会需要三个值，发布版安全码SHA1、调试版安全码SHA1、PackageName。

**①获得packageName**

这个在你的android工程中可以找到

**②获得调试版安全码**

- 按window+R 输入cmd 进入控制台

- 在弹出的控制台窗口中输入 cd .android 定位到 .android 文件夹 

- 调试版本使用 debug.keystore，命令为：keytool -list -v -keystore debug.keystore 发布版本使用 apk 对应的 keystore，命令为：keytool -list -v -keystore

- 提示输入密钥库密码，调试版本默认密码是 android，发布版本的密码是为 apk 的 keystore 设置的密码。输入密钥后回车（如果没设置密码，可直接回车），此时可在控制台显示的信息中获取 Sha1 值

**③获得发布版安全码SHA1**

- 和调试版本不一样的是 发布版还需要一个jks文件 需要在android工程里新建

- 点击Build 选择点击Generate Signed Bundle / APK… 

- 选择APK，然后Next 

- 然后是配置，这里需要填写jks的路径，如果没有这个jks，就点击Create new按钮去创建一个。 

- 首先要指定这个jks的文件存放路径和文件名。 

- 这里我存放在D盘下的APK文件夹中，然后设置jks的名字为GaodeMapDemo，然后点击OK。 

- 会弹出这样一个窗口，不用管它，点击OK。 

- 勾选记住密码，然后点击Next。 

- 选择release，然后两个都勾选上，最后点击Finish。 

- 在你的AS中查看这个apk，你可以复制它通过电脑QQ发给你的手机，然后在手机上直接打开安装 

- 然后点击确认后 会显示你的key 
## 二、配置Android Studio工程
**①导入SDK**

首先要下载SDK，点击SDK下载 复制这些文件到你的libs下。然后进行工程的资源配置同步。 你可以看到你的这个jar现在是可以打开的。 然后打开你的app下的build.gradle文件，在android闭包下添加

```
sourceSets {
        main{
            jniLibs.srcDirs = ['libs']
        }
    }
```
② 配置AndroidManifest.xml

打开AndroidManifest.xml，首先在application标签下添加定位服务

```
<service android:name="com.amap.api.location.APSService"/>
```
然后添加在manifest标签下添加如下权限。

```
<!--用于访问网络，网络定位需要上网-->
    <uses-permission android:name="android.permission.INTERNET" />
    <!--用于读取手机当前的状态-->
    <uses-permission android:name="android.permission.READ_PHONE_STATE" />
    <!--用于写入缓存数据到扩展存储卡-->
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <!--用于申请调用A-GPS模块-->
    <uses-permission android:name="android.permission.ACCESS_LOCATION_EXTRA_COMMANDS" />
    <!--用于进行网络定位-->
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <!--用于访问GPS定位-->
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <!--用于获取运营商信息，用于支持提供运营商信息相关的接口-->
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <!--用于访问wifi网络信息，wifi信息会用于进行网络定位-->
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    <!--用于获取wifi的获取权限，wifi信息会用来进行网络定位-->
    <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
```
最后在application标签下添加高德的访问key

```
<meta-data android:name="com.amap.api.v2.apikey" android:value="d3347ee0f2928f9a0c199cae009ae7f7"/>
这个值和你创建的key的值一致
```
## 三、获取当前定位信息
首先得先判断当前是否需要动态请求权限，所以要根据Android的版本来判断。

**① 版本判断**
在MainActivity中写入这样一个checkingAndroidVersion()方法。

```
//检查Android版本
private void checkingAndroidVersion() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            //Android6.0及以上先获取权限再定位
            
        }else {
            //Android6.0以下直接定位
            
        }
    }
```
**① 动态权限请求**
打开app下的build.gradle，在dependencies闭包下添加如下依赖：

```
implementation 'pub.devrel:easypermissions:3.0.0'
```
**② 初始化定位**
首先在newActivity中新增两个成员变量

```
//声明AMapLocationClient类对象
public AMapLocationClient mLocationClient = null;
//声明AMapLocationClientOption对象
public AMapLocationClientOption mLocationOption = null;
```
然后新增一个initLocation()方法

```
private void initLocation() {
        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(this);
        //初始化AMapLocationClientOption对象
        mLocationOption = new AMapLocationClientOption();
        //设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //获取最近3s内精度最高的一次定位结果
        mLocationOption.setOnceLocationLatest(true);
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //设置定位请求超时时间，单位是毫秒，默认30000毫秒，建议超时时间不要低于8000毫秒。
        mLocationOption.setHttpTimeOut(20000);
        //关闭缓存机制，高精度定位会产生缓存。
        mLocationOption.setLocationCacheEnable(false);
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
    }

```
**③ 获取定位结果**

## 四、显示地图
下面改变一下activity_main.xml，添加MapView

```
<com.amap.api.maps.MapView
        android:id="@+id/map_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>
```
然后增加地图生命周期的管理方法。

## 五、显示当前定位地图
>将定位结果和地图结合

现在newActivity中新增两个成员变量

```
private AMap aMap = null;
private OnLocationChangedListener mListener;
```
然后新增一个initMap()方法，用于初始化地图

```
private void initMap(Bundle savedInstanceState) {
        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        //初始化地图
        aMap = mapView.getMap();

        // 设置定位监听
        aMap.setLocationSource(this);
        aMap.setMyLocationEnabled(true);
    }
```
然后重写里面的两个方法在activate()和deactivate()。

 

```
@Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
        if (mLocationClient == null) {
            mLocationClient.startLocation();//启动定位
        }
    }
    @Override
    public void deactivate() {
        mListener = null;
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
        mLocationClient = null;
    }
```




## 六、地图设置

主要是设置一些自定义图标 比例尺 缩放比例

## 七、获取POI数据
首先先在app的build.gradle中添加依赖

```
implementation 'com.google.android.material:material:1.2.0'
```
在activity_main.xml中添加浮动按钮

```
    <!--浮动按钮-->
    <com.google.android.material.floatingactionbutton.FloatingActionButton
        android:id="@+id/fab_poi"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_alignParentRight="true"
        android:layout_alignParentBottom="true"
        android:layout_margin="20dp"
        android:clickable="true"
        android:onClick="queryPOI"
        android:src="@drawable/icon_favorite_red"
        android:visibility="gone"
        app:backgroundTint="#FFF"
        app:backgroundTintMode="screen"
        app:hoveredFocusedTranslationZ="18dp"
        app:pressedTranslationZ="18dp" />
```

添加一个queryPOI()方法，这个方法对应了xml中浮动按钮的onClick的值

```
    //实现点击事件：浮动按钮点击查询附近POI

    public void queryPOI(View view) {
        //构造query对象
        query = new PoiSearch.Query("购物", "", cityCode);
//        设置每页最多返回多少条poiitem
        query.setPageSize(10);
//        设置查询页码
        query.setPageNum(1);
//        构造PoiSearch对象
        poiSearch = new PoiSearch(this, query);
//        设置搜索回调监听
        poiSearch.setOnPoiSearchListener(this);
//        发起搜索附近POI异步请求
        poiSearch.searchPOIAsyn();
    }
```
在这个方法里面对query和poiSearch进行配置，然后发起搜索附近POI异步请求。

下面就要实现PoiSearch.OnPoiSearchListener

然后重写里面的onPoiSearched和onPoiItemSearched

```
//    POI搜索返回

    @Override
    public void onPoiSearched(PoiResult poiResult, int i) {
        //解析result获取POI信息

        //获取POI组数列表
        ArrayList<PoiItem> poiItems = poiResult.getPois();
        for (PoiItem poiItem : poiItems) {
            Log.d("MainActivity", "Title:" + poiItem.getTitle() + "Snippet:" + poiItem.getSnippet());
        }
    }

    //POI中的项目搜索返回

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }
```

## 八、地图点击长按事件
**①逆地理编码**
逆地理编码就是将坐标转为地址，坐标刚才已经拿到了，就是经纬度 首先在newActivity中创建两个对象。

```
    private GeocodeSearch geocodeSearch;
    private static final int PARSE_SUCCESS_CODE = 1000;
```
然后在initMap()中构建对象，然后设置监听。之后实现GeocodeSearch.OnGeocodeSearchListener接口

重写里面的两个方法。一个是地址转坐标，一个是坐标转地址

```
    //坐标转为地址

    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int rCode) {
        if (rCode == PARSE_SUCCESS_CODE) {
            RegeocodeAddress regeocodeAddress = regeocodeResult.getRegeocodeAddress();
            //显示解析后的地址
            showMsg("地址：" + regeocodeAddress.getFormatAddress());
        } else {
            showMsg("获取地址失败");
        }
    }

    //地址转坐标

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int rCode) {
        if (rCode == PARSE_SUCCESS_CODE) {
            List<GeocodeAddress> geocodeAddressList = geocodeResult.getGeocodeAddressList();
            if (geocodeAddressList != null && geocodeAddressList.size() > 0) {
                mEndPoint = geocodeAddressList.get(0).getLatLonPoint();

                starRouteSearch();
            }
        } else {
            showMsg("获取坐标失败");
        }
    }

```
通过经纬度构建LatLonPoint对象，然后构建RegeocodeQuery时，传入，并且输入另外两个参数，范围和坐标系。最后通过geocodeSearch发起一个异步的地址获取请求。

```
    //通过经纬度获取地址

    private void latlonToAddress(LatLng latLng) {
        //位置点 通过经纬度进行构建
        LatLonPoint latLonPoint = new LatLonPoint(latLng.latitude, latLng.longitude);
        //逆编码查询 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 20, GeocodeSearch.AMAP);
        //异步获取地址信息
        geocodeSearch.getFromLocationAsyn(query);
    }

```
**② 地理编码**
同理 进入到onGeocodeSearched方法

```
if (rCode == PARSE_SUCCESS_CODE) {
            List<GeocodeAddress> geocodeAddressList = geocodeResult.getGeocodeAddressList();
            if (geocodeAddressList != null && geocodeAddressList.size() > 0) {
                mEndPoint = geocodeAddressList.get(0).getLatLonPoint();

                starRouteSearch();
            }
        }
```

## 九、添加标点marker和改变中心点
**① 添加标点Marker**

```
aMap.addMarker(new MarkerOptions().position(latLng).snippet("DefaultMarker"));
```
**② 删除标点Marker**

```
marker.remove();
```
**③ 改变中心点**
改变中心点 我们还要用到camera

```
    //改变地图中心位置

    private void updateMapCenter(LatLng latLng) {
        CameraPosition cameraPosition = new CameraPosition(latLng, 16, 30, 0);
        //位置变更
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        //修改位置
        aMap.animateCamera(cameraUpdate);
    }

```
平滑的切换中心点，这个其实SDK中也提供了，你只需要把moveCamera改成animateCamera就可以了做到平滑移动，而不会显得很突兀

```
//带动画的移动
aMap.animateCamera(cameraUpdate);
```

## 十、出行规划

>出行规划 让用户知道如何到达目的地

这个功能需要我们提供两个点 起点和终点 利用官方接口完成
# 感想
