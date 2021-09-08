package com.example.gaodemapdemo.overlay;

import android.content.Context;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.busline.BusStationItem;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusStep;
import com.amap.api.services.route.Doorway;
import com.amap.api.services.route.RailwayStationItem;
import com.amap.api.services.route.RouteBusLineItem;
import com.amap.api.services.route.RouteBusWalkItem;
import com.amap.api.services.route.RouteRailwayItem;
import com.amap.api.services.route.TaxiItem;
import com.amap.api.services.route.WalkStep;
import com.example.gaodemapdemo.util.MapUtil;

import java.util.ArrayList;
import java.util.List;

public class BusRouteOverlay extends RouteOverlay{
    private BusPath busPath;
    private LatLng latLng;

    /**
     * 通过此构造函数创建公交路线图层。
     * @param context 当前activity。
     * @param amap 地图对象。
     * @param path 公交路径规划的一个路段。详见搜索服务模块的路径查询包（com.amap.api.services.route）中的类<strong> <a href="../../../../../../Search/com/amap/api/services/route/BusPath.html" title="com.amap.api.services.route中的类">BusPath</a></strong>。
     * @param start 起点坐标。详见搜索服务模块的核心基础包（com.amap.api.services.core）中的类 <strong><a href="../../../../../../Search/com/amap/api/services/core/LatLonPoint.html" title="com.amap.api.services.core中的类">LatLonPoint</a></strong>。
     * @param end 终点坐标。详见搜索服务模块的核心基础包（com.amap.api.services.core）中的类 <strong><a href="../../../../../../Search/com/amap/api/services/core/LatLonPoint.html" title="com.amap.api.services.core中的类">LatLonPoint</a></strong>。
     * @since V2.1.0
     */

    public BusRouteOverlay(Context context , AMap amap, BusPath path, LatLonPoint start,LatLonPoint end){
        super(context);
        this.busPath = path;
        startPoint = MapUtil.convertToLatLng(start);
        endPoint = MapUtil.convertToLatLng(end);
        mAMap =amap;
    }

    //添加路线到地图

    public void addToMap(){

        //步行与公交交替

        try {
            List<BusStep> busSteps = busPath.getSteps();
            for (int i = 0;i<busSteps.size();i++){
                BusStep busStep = busSteps.get(i);
                if (i<busSteps.size()-1){
                    //获取下一个BusStep对象
                    BusStep busStep1 = busSteps.get(i+1);
                    if (busStep.getWalk()!=null&&busStep.getBusLines()!=null){
                        checkWalkToBusline(busStep);
                    }

                    if (busStep.getBusLine()!=null&&busStep1.getWalk()!=null&&busStep1.getWalk().getSteps().size()>0){
                        checkBusLineToNextWalk(busStep,busStep1);
                    }

                    if (busStep.getBusLine()!=null&&busStep1.getWalk()==null&&busStep1.getBusLine()!=null){
                        checkBusEndToNextBusStart(busStep,busStep1);
                    }


                    if (busStep.getBusLine() != null
                            && busStep1.getWalk() == null
                            && busStep1.getBusLine() != null) {
                        checkBusToNextBusNoWalk(busStep, busStep1);
                    }

                    if (busStep.getBusLine() != null
                            && busStep1.getRailway() != null ) {
                        checkBusLineToNextRailway(busStep, busStep1);
                    }
                    if (busStep1.getWalk() != null &&
                            busStep1.getWalk().getSteps().size() > 0 &&
                            busStep.getRailway() != null) {
                        checkRailwayToNextWalk(busStep, busStep1);
                    }

                    if ( busStep1.getRailway() != null &&
                            busStep.getRailway() != null) {
                        checkRailwayToNextRailway(busStep, busStep1);
                    }

                    if (busStep.getRailway() != null &&
                            busStep1.getTaxi() != null ){
                        checkRailwayToNextTaxi(busStep, busStep1);
                    }
                }

                if (busStep.getWalk() != null
                        && busStep.getWalk().getSteps().size() > 0) {
                    addWalkSteps(busStep);
                } else {
                    if (busStep.getBusLine() == null && busStep.getRailway() == null && busStep.getTaxi() == null) {
                        addWalkPolyline(latLng, endPoint);
                    }
                }
                if (busStep.getBusLine() != null) {
                    RouteBusLineItem routeBusLineItem = busStep.getBusLine();
                    addBusLineSteps(routeBusLineItem);
                    addBusStationMarkers(routeBusLineItem);
                    if (i == busSteps.size() - 1) {
                        addWalkPolyline(MapUtil.convertToLatLng(getLastBuslinePoint(busStep)), endPoint);
                    }
                }
                if (busStep.getRailway() != null) {
                    addRailwayStep(busStep.getRailway());
                    addRailwayMarkers(busStep.getRailway());
                    if (i == busSteps.size() - 1) {
                        addWalkPolyline(MapUtil.convertToLatLng(busStep.getRailway().getArrivalstop().getLocation()), endPoint);
                    }
                }
                if (busStep.getTaxi() != null) {
                    addTaxiStep(busStep.getTaxi());
                    addTaxiMarkers(busStep.getTaxi());
                }
            }
            addStartAndEndMarker();
            }catch (Throwable e){
            e.printStackTrace();
        }
    }

    private void checkRailwayToNextTaxi(BusStep busStep,BusStep busStep1){
        LatLonPoint railwayLastPoint = busStep.getRailway().getArrivalstop().getLocation();
        LatLonPoint taxiFirstPoint = busStep1.getTaxi().getOrigin();
        if (!railwayLastPoint.equals(taxiFirstPoint)){
            addWalkPolyLineByLatLonPoints(railwayLastPoint,taxiFirstPoint);
        }
    }

    private void checkRailwayToNextRailway(BusStep busStep, BusStep busStep1) {
        LatLonPoint railwayLastPoint = busStep.getRailway().getArrivalstop().getLocation();
        LatLonPoint railwayFirstPoint = busStep1.getRailway().getDeparturestop().getLocation();
        if (!railwayLastPoint.equals(railwayFirstPoint)) {
            addWalkPolyLineByLatLonPoints(railwayLastPoint, railwayFirstPoint);
        }

    }

    private void checkBusLineToNextRailway(BusStep busStep, BusStep busStep1) {
        LatLonPoint busLastPoint = getLastBuslinePoint(busStep);
        LatLonPoint railwayFirstPoint = busStep1.getRailway().getDeparturestop().getLocation();
        if (!busLastPoint.equals(railwayFirstPoint)) {
            addWalkPolyLineByLatLonPoints(busLastPoint, railwayFirstPoint);
        }

    }

    private void checkRailwayToNextWalk(BusStep busStep,BusStep busStep1){
        LatLonPoint railwayLastPoint = busStep.getRailway().getArrivalstop().getLocation();
        LatLonPoint walkFirstPoint = getFirstWalkPoint(busStep1);
        if (!railwayLastPoint.equals(walkFirstPoint)){
            addWalkPolyLineByLatLonPoints(railwayLastPoint,walkFirstPoint);
        }
    }

    private void addRailwayStep(RouteRailwayItem railway){
        List<LatLng> railwaylistpoint = new ArrayList<LatLng>();
        List<RailwayStationItem> railwayStationItems = new ArrayList<RailwayStationItem>();
        railwayStationItems.add(railway.getDeparturestop());
        railwayStationItems.addAll(railway.getViastops());
        railwayStationItems.add(railway.getArrivalstop());
        for (int i = 0;i<railwayStationItems.size();i++){
            railwaylistpoint.add(MapUtil.convertToLatLng(railwayStationItems.get(i).getLocation()));
        }
        addRailwayPolyline(railwaylistpoint);
    }

    private void addTaxiStep(TaxiItem taxi){
        addPolyLine(new PolylineOptions().width(getRouteWidth()).color(getBusColor()).add(MapUtil.convertToLatLng(taxi.getOrigin())).add(MapUtil.convertToLatLng(taxi.getDestination())));
    }

    private void addWalkSteps(BusStep busStep){
        RouteBusWalkItem routeBusWalkItem =busStep.getWalk();
        List<WalkStep> walkSteps =routeBusWalkItem.getSteps();
        for (int j =0;j<walkSteps.size();j++){
            WalkStep walkStep =walkSteps.get(j);
            if (j==0){
                LatLng latLng = MapUtil.convertToLatLng(walkStep.getPolyline().get(0));
                //道路名称
                String road = walkStep.getRoad();
                //步行导航信息
                String instruction = getWalkSnippet(walkSteps);
                addWalkStationMarkers(latLng,road,instruction);
            }
            List<LatLng> listWalkPolyline = MapUtil.convertArrList(walkStep.getPolyline());
            this.latLng = listWalkPolyline.get(listWalkPolyline.size()-1);

            addWalkPolyline(listWalkPolyline);

            //避免断线
            if (j<walkSteps.size()-1){
                LatLng lastLatLng = listWalkPolyline.get(listWalkPolyline.size()-1);
                LatLng fitstLatLng = MapUtil.convertToLatLng(walkSteps.get(j+1).getPolyline().get(0));
                if (!(lastLatLng.equals(fitstLatLng))){
                    addWalkPolyline(lastLatLng,fitstLatLng);
                }
            }
        }
    }

    private void addBusLineSteps(RouteBusLineItem routeBusLineItem){
        addBusLineSteps(routeBusLineItem.getPolyline());
    }

    private void addBusLineSteps(List<LatLonPoint> listPoints){
        if (listPoints.size()<1){
            return;
        }
        addPolyLine(new PolylineOptions().width(getRouteWidth()).color(getBusColor()).addAll(MapUtil.convertArrList(listPoints)));
    }

    private void addWalkStationMarkers(LatLng latLng,String title,String snippet){
        addStationMarker(new MarkerOptions().position(latLng).title(title).snippet(snippet).anchor(0.5f,0.5f).visible(nodeIconVisible).icon(getDriveBitmapDescriptor()));
    }

    private void addBusStationMarkers(RouteBusLineItem routeBusLineItem){
        BusStationItem startBusStation = routeBusLineItem.getDepartureBusStation();
        LatLng position = MapUtil.convertToLatLng(startBusStation.getLatLonPoint());
        String title = routeBusLineItem.getBusLineName();
        String snippet = getBusSnippet(routeBusLineItem);

        addStationMarker(new MarkerOptions().position(position).title(title).snippet(snippet).anchor(0.5f,0.5f).visible(nodeIconVisible).icon(getDriveBitmapDescriptor()));
    }

    private void addTaxiMarkers(TaxiItem taxiItem){
        LatLng position = MapUtil.convertToLatLng(taxiItem.getOrigin());
        String title = taxiItem.getmSname()+"打车";
        String snippet = "到终点";

        addStationMarker(new MarkerOptions().position(position).title(title).snippet(snippet).anchor(0.5f,0.5f).visible(nodeIconVisible).icon(getDriveBitmapDescriptor()));
    }

    private void addRailwayMarkers(RouteRailwayItem railway){
        LatLng Departureposition = MapUtil.convertToLatLng(railway.getDeparturestop().getLocation());
        String Departuretitle = railway.getDeparturestop().getName()+"上车";
        String Departuresnippet = railway.getName();

        addStationMarker(new MarkerOptions().position(Departureposition).title(Departuretitle).snippet(Departuresnippet).anchor(0.5f,0.5f).visible(nodeIconVisible).icon(getBusBitmapDescriptor()));

        LatLng Arrivalposition = MapUtil.convertToLatLng(railway.getArrivalstop().getLocation());
        String Arrivaltitle = railway.getArrivalstop().getName()+"下车";
        String Arrivalsnippet = railway.getName();

        addStationMarker(new MarkerOptions().position(Arrivalposition).title(Arrivaltitle).snippet(Arrivalsnippet).anchor(0.5f,0.5f).visible(nodeIconVisible).icon(getBusBitmapDescriptor()));
    }

    //换乘没有步行 检查公交最后一点和下一个公交起点是否一致

    private void checkBusToNextBusNoWalk(BusStep busStep,BusStep busStep1){
        LatLng endbusLatLng = MapUtil.convertToLatLng(getLastBuslinePoint(busStep));
        LatLng startbusLatLng = MapUtil.convertToLatLng(getFirstBuslinePoint(busStep1));
        if (startbusLatLng.latitude-endbusLatLng.latitude>0.0001||startbusLatLng.longitude-endbusLatLng.longitude>0.0001){
            drawLineArrow(endbusLatLng,startbusLatLng);
        }
    }

    private void checkBusEndToNextBusStart(BusStep busStep,BusStep busStep1){
        LatLonPoint busLastPoint=getLastBuslinePoint(busStep);
        LatLng endbusLatLng = MapUtil.convertToLatLng(busLastPoint);
        LatLonPoint busFirstPoint=getFirstBuslinePoint(busStep1);
        LatLng startbusLatLng = MapUtil.convertToLatLng(busFirstPoint);
        if (!endbusLatLng.equals(startbusLatLng)){
            drawLineArrow(endbusLatLng,startbusLatLng);
        }
    }

    //检查公交最后一步和下一个步行起点是否一致

    private void checkBusLineToNextWalk(BusStep busStep,BusStep busStep1){
        LatLonPoint busLastPoint = getLastBuslinePoint(busStep);
        LatLonPoint walkFirstPoint = getFirstBuslinePoint(busStep1);

        if (!busLastPoint.equals(walkFirstPoint)){
            addWalkPolyLineByLatLonPoints(busLastPoint,walkFirstPoint);
        }
    }

    //检查步行最后一点和公交起点是否一致

    private void checkWalkToBusline(BusStep busStep){
        LatLonPoint walkLastPoint = getLastWalkPoint(busStep);
        LatLonPoint buslineFirstPoint = getFirstBuslinePoint(busStep);

        if (!walkLastPoint.equals(buslineFirstPoint)){
            addWalkPolyLineByLatLonPoints(walkLastPoint,buslineFirstPoint);
        }
    }

    private LatLonPoint getFirstWalkPoint(BusStep busStep1){
        return busStep1.getWalk().getSteps().get(0).getPolyline().get(0);
    }

    private void addWalkPolyLineByLatLonPoints(LatLonPoint pointFrom,LatLonPoint pointTo){
        LatLng latLngFrom = MapUtil.convertToLatLng(pointFrom);
        LatLng latLngTo = MapUtil.convertToLatLng(pointTo);

        addWalkPolyline(latLngFrom,latLngTo);
    }

    private void addWalkPolyline(LatLng latLngFrom,LatLng latLngTo){
        addPolyLine(new PolylineOptions().add(latLngFrom,latLngTo).width(getRouteWidth()).color(getWalkColor()).setDottedLine(true));
    }

    private void addWalkPolyline(List<LatLng> listWalkPolyline){
        addPolyLine(new PolylineOptions().addAll(listWalkPolyline).color(getWalkColor()).width(getRouteWidth()).setDottedLine(true));
    }

    private void addRailwayPolyline(List<LatLng> listPolyline){
        addPolyLine(new PolylineOptions().addAll(listPolyline).color(getDriveColor()).width(getRouteWidth()));
    }

    private String getWalkSnippet(List<WalkStep> walkSteps){
        float disNum = 0;
        for (WalkStep step:walkSteps){
            disNum+=step.getDistance();
        }
        return "\u6B65\u884C"+disNum+"\u7C73";
    }

    private void drawLineArrow(LatLng latLngFrom,LatLng latLngTo){
        //绘制直线
        addPolyLine(new PolylineOptions().add(latLngFrom,latLngTo).width(3).color(getBusColor()).width(getRouteWidth()));
    }

    private String getBusSnippet(RouteBusLineItem routeBusLineItem){
        return "("+routeBusLineItem.getDepartureBusStation().getBusStationName()+"-->"+routeBusLineItem.getArrivalBusStation().getBusStationName()+") \u7ECF\u8FC7"+(routeBusLineItem.getPassStationNum()+1)+"\u7AD9";
    }

    private LatLonPoint getLastWalkPoint(BusStep busStep){
        List<WalkStep> walkSteps = busStep.getWalk().getSteps();
        WalkStep walkStep = walkSteps.get(walkSteps.size()-1);
        List<LatLonPoint> lonPoints = walkStep.getPolyline();
        return lonPoints.get(lonPoints.size()-1);
    }

    private LatLonPoint getExitPoint(BusStep busStep){
        Doorway doorway =busStep.getExit();

        if (doorway==null){
            return null;
        }
        return doorway.getLatLonPoint();
    }

    private LatLonPoint getLastBuslinePoint(BusStep busStep){
        List<LatLonPoint> lonPoints = busStep.getBusLine().getPolyline();

        return lonPoints.get(lonPoints.size()-1);
    }

    private LatLonPoint getEntrancePoint(BusStep busStep){
        Doorway doorway = busStep.getEntrance();
        if (doorway==null){
            return null;
        }
        return doorway.getLatLonPoint();
    }

    private LatLonPoint getFirstBuslinePoint(BusStep busStep){
        return busStep.getBusLine().getPolyline().get(0);
    }
}
