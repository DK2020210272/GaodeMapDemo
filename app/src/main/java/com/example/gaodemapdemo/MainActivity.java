package com.example.gaodemapdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.animation.Animation;
import com.amap.api.maps.model.animation.RotateAnimation;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RidePath;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.example.gaodemapdemo.overlay.BusRouteOverlay;
import com.example.gaodemapdemo.overlay.DrivingRouteOverlay;
import com.example.gaodemapdemo.overlay.RideRouteOverlay;
import com.example.gaodemapdemo.overlay.WalkRouteOverlay;
import com.example.gaodemapdemo.util.MapUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements AMapLocationListener, LocationSource, PoiSearch.OnPoiSearchListener,
        AMap.OnMapClickListener, AMap.OnMapLongClickListener, GeocodeSearch.OnGeocodeSearchListener, View.OnKeyListener, AMap.OnMarkerClickListener,
        AMap.OnMarkerDragListener, AMap.OnInfoWindowClickListener, RouteSearch.OnRouteSearchListener {

    //    请求权限码
    private static final int REQUEST_PERMISSIONS = 9527;

    //    声明AMAPLocationClient类对象
    public AMapLocationClient mLocationClient = null;

    //    声明AMapLocationClientOption对象
    public AMapLocationClientOption mLocationOption = null;

    //显示地图
    private MapView mapView;

    //    地图控制器
    private AMap aMap = null;

    //    位置更改监听
    private OnLocationChangedListener mListener;

    //定位样式
    private MyLocationStyle myLocationStyle = new MyLocationStyle();

    //定义一个UiSetting对象，对地图控件进行设置
    private UiSettings mUiSettings;

    //POI操作
    //查询对象
    private PoiSearch.Query query;
    //搜索对象
    private PoiSearch poiSearch;
    //城市码
    private String cityCode = null;
    //浮动按钮
    private FloatingActionButton fabPOI;

    //逆地理编码，将坐标转为地址
    //地理编码搜索
    private GeocodeSearch geocodeSearch;
    //解析成功标识码
    private static final int PARSE_SUCCESS_CODE = 1000;

    //修改标题栏为搜索栏
    //输入框
    private EditText etStarAddress,etEndAddress;

    //城市
    private String city;

    //浮动按钮，用于清空地图标点Marker
    private FloatingActionButton fabClearMarker;
    //标点列表
    private List<Marker> markerList = new ArrayList<>();

    //打印日志
    public static final String TAG = "MainActivity-拖拽事件";

    //起点
    private LatLonPoint mStartPoint;
    //终点
    private LatLonPoint mEndPoint;

    //路线搜索对象
    private RouteSearch routeSearch;

    //出行方式数组
    private static final String[] travalModeArray = {"步行出行", "骑行出行", "驾车出行", "公交出行"};

    //出行方式值,当我们点击下拉框选择类型之后，通过位置赋值给这个TRAVEL_MODE 变量
    private static int TRAVEL_MODE = 0;

    //数组适配器
    private ArrayAdapter<String> arrayAdapter;

    //判断是否进行路线搜索
    private int judge=0;

    //    检查安卓版本

    private void checkingAndroidVersion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            Android6.0及以上先获取权限再定位
            requestPermission();
        } else {
//            Android6.0以下直接定位
            mLocationClient.startLocation();
        }
    }

//    动态请求权限

    @AfterPermissionGranted(REQUEST_PERMISSIONS)
    private void requestPermission() {
        String[] permissions = {
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        if (EasyPermissions.hasPermissions(this, permissions)) {
//            true 有权限 开始定位
            showMsg("已获得权限，可以定位了！");
            //启动定位
            mLocationClient.startLocation();
        } else {
//            false 无权限
            EasyPermissions.requestPermissions(this, "需要权限", REQUEST_PERMISSIONS, permissions);
        }
    }

    //Toast提示
//    @param msg提示内容

    private void showMsg(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    //请求权限的结果

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        设置权限请求结果
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

//    初始化定位

    private void initLocation() {
//        初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
//        设置定位回调监听
        mLocationClient.setLocationListener(this);
//        初始化AMapLocationClientOption对象
        mLocationOption = new AMapLocationClientOption();
//        设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
//        获取最近3s内精度最高的一次定位结果:
//        设置setOnceLocationLatest(boolean b)接口为true,启动定位时SDK会返回最近3s内精度最高的一次定位结果
//        如果设置其为true，setOnceLoction(boolean b)接口也会被设置为true，反之不会，默认为false
        mLocationOption.setOnceLocationLatest(true);
//        设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
//        设置定位请求超时时间，单位是毫秒
        mLocationOption.setHttpTimeOut(20000);
//        关闭缓存机制，高精度定位会产生缓存
        mLocationOption.setLocationCacheEnable(false);
//        给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
    }

//    接收异步返回的定位结果

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null) {
            if (aMapLocation.getErrorCode() == 0) {
                //显示的地址信息
                StringBuffer stringBuffer = new StringBuffer();
                double latitude = aMapLocation.getLatitude();
                double longtitude = aMapLocation.getLongitude();
                String address = aMapLocation.getAddress();

                stringBuffer.append("纬度：" + latitude + "\n");
                stringBuffer.append("经度：" + longtitude + "\n");
                stringBuffer.append("地址：" + address + "\n");

                Log.d("MainActivity", stringBuffer.toString());
                showMsg(address);

                //设置起点
                mStartPoint = MapUtil.convertToLatLonPoint(new LatLng(latitude, longtitude));

                //停止定位(本地定位服务并未被销毁)
                mLocationClient.stopLocation();

                //显示地图定位结果
                if (mListener != null) {
                    //显示系统图标
                    mListener.onLocationChanged(aMapLocation);
                }

                //显示浮动按钮
                fabPOI.show();
                //赋值
                cityCode = aMapLocation.getCityCode();

                //设置当前所在地
                etStarAddress.setText(address);
                etEndAddress.setEnabled(true);

            } else {
                //定位失败时，通过ErrCode信息来确定失败原因，errInfo是错误信息，详见错误码表
                Log.e("AmapError", "location Error,ErrCode:"
                        + aMapLocation.getErrorCode() + ",errInofo:"
                        + aMapLocation.getErrorInfo());
            }

            //获得所在城市的名称
            city = aMapLocation.getCity();

        }
    }

//    初始化地图

    private void initMap(Bundle savedInstanceState) {
        mapView = findViewById(R.id.map_view);
        //创建地图
        mapView.onCreate(savedInstanceState);
        //初始化地图控制器对象
        aMap = mapView.getMap();

        //设置缩放等级
        aMap.setMinZoomLevel(12);

        //开启室内地图
        aMap.showIndoorMap(true);

        //自定义定位图标

        //定位蓝点图标
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_roundpoint));
        //精度范围的圆形边框透明
        myLocationStyle.strokeColor(Color.argb(0, 0, 0, 0));
        //精度范围的圆形边框宽度
        myLocationStyle.strokeWidth(0);
        //精度范围的圆形边框填充透明
        myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));
        //完成设置
        aMap.setMyLocationStyle(myLocationStyle);

        //设置定位监听
        aMap.setLocationSource(this);
        //显示定位层，并触发定位
        aMap.setMyLocationEnabled(true);

        //地图控件的设置
        mUiSettings = aMap.getUiSettings();
        //隐藏缩放按钮
        mUiSettings.setZoomControlsEnabled(false);
        //显示比例尺
        mUiSettings.setScaleControlsEnabled(true);

        //设置地图点击事件
        aMap.setOnMapClickListener(this);
        //设置地图长按事件
        aMap.setOnMapLongClickListener(this);
        //设置地图标点Marker点击事件
        aMap.setOnMarkerClickListener(this);
        //设置地图标点Marker拖拽事件
        aMap.setOnMarkerDragListener(this);
        //设置InofoWindow点击事件
        aMap.setOnInfoWindowClickListener(this);

        //构造GeocodeSearch对象
        geocodeSearch = new GeocodeSearch(this);
        //设置监听
        geocodeSearch.setOnGeocodeSearchListener(this);
    }

    //激活定位

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
        if (mLocationClient == null) {
            mLocationClient.startLocation();
        }
    }

    //停止定位

    @Override
    public void deactivate() {
        mListener = null;
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
        mLocationClient = null;
    }

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

    //地图点击事件

    @Override
    public void onMapClick(LatLng latLng) {
        if (judge==0){
            //添加标点Marker
            addMarker(latLng);
            //修改地图中心位置
            updateMapCenter(latLng);
        }else{
            //终点
            mEndPoint = MapUtil.convertToLatLonPoint(latLng);
            //开始搜索路线
            starRouteSearch();
        }
    }

    //地图长按事件

    @Override
    public void onMapLongClick(LatLng latLng) {
        //通过经纬度获取地址
        latlonToAddress(latLng);
    }

    //通过经纬度获取地址

    private void latlonToAddress(LatLng latLng) {
        //位置点 通过经纬度进行构建
        LatLonPoint latLonPoint = new LatLonPoint(latLng.latitude, latLng.longitude);
        //逆编码查询 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 20, GeocodeSearch.AMAP);
        //异步获取地址信息
        geocodeSearch.getFromLocationAsyn(query);
    }

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

//    键盘点击

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        //当键盘状态为抬起及按下Enter
        if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
            //获取输入框的值
            String endAddress = etEndAddress.getText().toString().trim();
            if (endAddress == null || endAddress.isEmpty()) {
                showMsg("请输入地址");
            } else {
                //判断输入是否为空，如果有输入则隐藏键盘
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                //隐藏软键盘
                imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);

                //查询
                GeocodeQuery query = new GeocodeQuery(endAddress, city);
                geocodeSearch.getFromLocationNameAsyn(query);
            }
            return true;
        }
        return false;
    }

//    添加地图标点Marker
//    @param latLng

    private void addMarker(LatLng latLng) {
        //显示浮动按钮
        fabClearMarker.show();
        //添加标点
        Marker marker = aMap.addMarker(new MarkerOptions().draggable(true).position(latLng).title("标题").snippet("详细信息"));

        //绘制标点的动画效果
        Animation animation = new RotateAnimation(marker.getRotateAngle(), marker.getRotateAngle() + 360, 0, 0, 0);
        long duration = 1000L;
        //设置持续时间
        animation.setDuration(duration);
        //设置插值器
        animation.setInterpolator(new LinearInterpolator());
        marker.setAnimation(animation);
        marker.startAnimation();

        markerList.add(marker);
    }

//    清空地图Marker
//    @param view

    public void clearAllMarker(View view) {
        if (markerList != null && markerList.size() > 0) {
            for (Marker markerItem : markerList) {
                markerItem.remove();
            }
        }
        fabClearMarker.hide();
    }

//    Marker点击事件

    @Override
    public boolean onMarkerClick(Marker marker) {
        //显示InfoWindow
        if (!marker.isInfoWindowShown()) {
            //显示
            marker.showInfoWindow();
        } else {
            //隐藏
            marker.hideInfoWindow();
        }
        return true;
    }

    //Marker拖拽事件

    //开始拖动
    //@param marker
    @Override
    public void onMarkerDragStart(Marker marker) {
        Log.d(TAG, "开始拖动");
    }

    //拖动中
    //@param marker
    @Override
    public void onMarkerDrag(Marker marker) {
        Log.d(TAG, "拖动中");
    }

    //拖动完成
    //@param marker
    @Override
    public void onMarkerDragEnd(Marker marker) {
        Log.d(TAG, "拖动结束");
    }

    //InfoWindow点击事件

    @Override
    public void onInfoWindowClick(Marker marker) {
        showMsg("弹窗内容：" + marker.getTitle() + "\n" + marker.getSnippet());
    }

    //改变地图中心位置

    private void updateMapCenter(LatLng latLng) {
        CameraPosition cameraPosition = new CameraPosition(latLng, 16, 30, 0);
        //位置变更
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        //修改位置
        aMap.animateCamera(cameraUpdate);
    }

    //初始化路线

    private void initRoute() {
        routeSearch = new RouteSearch(this);
        routeSearch.setRouteSearchListener(this);
    }

    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {
        aMap.clear();// 清理地图上的所有覆盖物
        if (i== AMapException.CODE_AMAP_SUCCESS) {
            if (busRouteResult != null && busRouteResult.getPaths() != null) {
                if (busRouteResult.getPaths().size() > 0) {
                    final BusPath busPath = busRouteResult.getPaths().get(0);
                    if (busPath == null) {
                        return;
                    }
                    BusRouteOverlay busRouteOverlay = new BusRouteOverlay(
                            this, aMap, busPath,
                            busRouteResult.getStartPos(),
                            busRouteResult.getTargetPos());
                    busRouteOverlay.removeFromMap();
                    busRouteOverlay.addToMap();
                    busRouteOverlay.zoomToSpan();

                    int dis = (int) busPath.getDistance();
                    int dur = (int) busPath.getDuration();
                    String des = MapUtil.getFriendlyTime(dur) + "(" + MapUtil.getFriendlyLength(dis) + ")";
                    Log.d(TAG, des);
                } else if (busRouteResult.getPaths() == null) {
                    showMsg("对不起，没有搜索到相关数据！");
                }
            } else {
                showMsg("对不起，没有搜索到相关数据！");
            }
        } else {
            showMsg("错误码；" +i);
        }
    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {
        //清理地图上的所有覆盖物
        aMap.clear();

        if (i == AMapException.CODE_AMAP_SUCCESS) {
            if (driveRouteResult != null && driveRouteResult.getPaths() != null) {
                if (driveRouteResult.getPaths().size() > 0) {
                    final DrivePath drivePath = driveRouteResult.getPaths()
                            .get(0);
                    if (drivePath == null) {
                        return;
                    }
                    DrivingRouteOverlay drivingRouteOverlay = new DrivingRouteOverlay(
                            this, aMap, drivePath,
                            driveRouteResult.getStartPos(),
                            driveRouteResult.getTargetPos(), null);
                    drivingRouteOverlay.removeFromMap();
                    drivingRouteOverlay.addToMap();
                    drivingRouteOverlay.zoomToSpan();

                    int dis = (int) drivePath.getDistance();
                    int dur = (int) drivePath.getDuration();
                    String des = MapUtil.getFriendlyTime(dur) + "(" + MapUtil.getFriendlyLength(dis) + ")";
                    Log.d(TAG, des);
                } else if (driveRouteResult.getPaths() == null) {
                    showMsg("对不起，没有搜索到相关数据！");
                }
            } else {
                showMsg("对不起，没有搜索到相关数据！");
            }
        } else {
            showMsg("错误码；" + i);
        }
    }

    @Override
    public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {
        //清理地图上的所有覆盖物
        aMap.clear();

        if (i == AMapException.CODE_AMAP_SUCCESS) {
            if (walkRouteResult != null && walkRouteResult.getPaths() != null) {
                if (walkRouteResult.getPaths().size() > 0) {
                    final WalkPath walkPath = walkRouteResult.getPaths().get(0);
                    if (walkPath == null) {
                        return;
                    }
                    //绘制路线
                    WalkRouteOverlay walkRouteOverlay = new WalkRouteOverlay(this, aMap, walkPath, walkRouteResult.getStartPos(), walkRouteResult.getTargetPos());
                    walkRouteOverlay.removeFromMap();
                    walkRouteOverlay.addToMap();
                    walkRouteOverlay.zoomToSpan();

                    int dis = (int) walkPath.getDistance();
                    int dur = (int) walkPath.getDuration();
                    String des = MapUtil.getFriendlyTime(dur) + "(" + MapUtil.getFriendlyLength(dis) + ")";
                    Log.d(TAG, des);
                } else if (walkRouteResult.getPaths() == null) {
                    showMsg("对不起，没有搜索到相关数据！");
                }
            } else {
                showMsg("对不起，没有搜索到相关数据！");
            }
        } else {
            showMsg("错误码" + i);
        }
    }

    @Override
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {
        //清理地图上的所有覆盖物
        aMap.clear();

        if (i == AMapException.CODE_AMAP_SUCCESS) {
            if (rideRouteResult != null && rideRouteResult.getPaths() != null) {
                if (rideRouteResult.getPaths().size() > 0) {
                    final RidePath ridePath = rideRouteResult.getPaths().get(0);
                    if (ridePath == null) {
                        return;
                    }
                    //绘制路线
                    RideRouteOverlay rideRouteOverlay = new RideRouteOverlay(this, aMap, ridePath, rideRouteResult.getStartPos(), rideRouteResult.getTargetPos());
                    rideRouteOverlay.removeFromMap();
                    rideRouteOverlay.addToMap();
                    rideRouteOverlay.zoomToSpan();
                    int dis = (int) ridePath.getDistance();
                    int dur = (int) ridePath.getDuration();
                    String des = MapUtil.getFriendlyTime(dur) + "(" + MapUtil.getFriendlyLength(dis) + ")";
                    Log.d(TAG, des);
                } else if (rideRouteResult.getPaths() == null) {
                    showMsg("对不起，没有搜索到相关数据！");
                }
            } else {
                showMsg("对不起，没有搜索到相关数据！");
            }
        } else {
            showMsg("错误码" + i);
        }
    }

    //开始路线搜索

    private void starRouteSearch() {
        //在地图上添加起点Marker
        aMap.addMarker(new MarkerOptions().position(MapUtil.convertToLatLng(mStartPoint)).icon(BitmapDescriptorFactory.fromResource(R.drawable.amap_start)));
        //在地图上添加终点Marker
        aMap.addMarker(new MarkerOptions().position(MapUtil.convertToLatLng(mEndPoint)).icon(BitmapDescriptorFactory.fromResource(R.drawable.amap_end)));

        //搜索路线 构建路径的起终点
        final RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(mStartPoint, mEndPoint);
        switch (TRAVEL_MODE) {
            //步行
            case 0:
                //构建步行路线搜索对象
                RouteSearch.WalkRouteQuery query = new RouteSearch.WalkRouteQuery(fromAndTo, RouteSearch.WalkDefault);
                //异步路径规划步行模式查询
                routeSearch.calculateWalkRouteAsyn(query);
                break;
            //骑行
            case 1:
                //构建骑行路线搜索对象
                RouteSearch.RideRouteQuery rideQuery = new RouteSearch.RideRouteQuery(fromAndTo, RouteSearch.WalkDefault);
                //异步路径规划骑行模式查询
                routeSearch.calculateRideRouteAsyn(rideQuery);
                break;
            //驾车
            case 2:
                //构建驾车路线搜索对象
                RouteSearch.DriveRouteQuery driveQuery = new RouteSearch.DriveRouteQuery(fromAndTo, RouteSearch.WalkDefault, null, null, "");
                //异步路径规划驾车模式查询
                routeSearch.calculateDriveRouteAsyn(driveQuery);
                break;
            //公交
            case 3:
                //构建公交路线搜索对象
                RouteSearch.BusRouteQuery busQuery = new RouteSearch.BusRouteQuery(fromAndTo, RouteSearch.BUS_LEASE_WALK, city, 0);
                routeSearch.calculateBusRouteAsyn(busQuery);
                break;
            default:
                break;
        }
    }

    //进入路线规划

    public void chooseRouteSearch(View view) {
        if (judge==0){
            judge=1;
        }else {
            judge=0;
        }
        aMap.clear();
    }

    //初始化出行方式

    private void initTravelMode() {
        Spinner spinner = findViewById(R.id.spinner);

        //将可选内容与ArrayAdapter连接起来
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, travalModeArray);
        //设置下拉列表的风格
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //将adapter添加到spinner中
        spinner.setAdapter(arrayAdapter);
        //添加Spinner事件监听
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TRAVEL_MODE = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //键盘按键监听
        etStarAddress = findViewById(R.id.et_start_point);
        etEndAddress = findViewById(R.id.et_end_point);
        etEndAddress.setOnKeyListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //POI
        fabPOI = findViewById(R.id.fab_poi);

        //Clear
        fabClearMarker = findViewById(R.id.fab_clear_marker);

//        初始化定位
        initLocation();

        //初始化地图
        initMap(savedInstanceState);

//        检查Android版本
        checkingAndroidVersion();

        //初始化路线
        initRoute();

        //初始化出行方式
        initTravelMode();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //销毁本地定位服务
        mLocationClient.onDestroy();
        //销毁地图
        mapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //重新绘制加载地图
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //暂停地图的绘制
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        //保存地图当前的状态
        mapView.onSaveInstanceState(outState);
    }
}