package com.example.gaodemapdemo.overlay;

//地图服务类

import android.graphics.Bitmap;

import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.LatLonPoint;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AMapServicesUtil {
    public static int BUFFER_SIZE = 2048;

    public static byte[] inputStreamToByte(InputStream in) throws IOException{
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] data = new byte[BUFFER_SIZE];
        int count = -1;
        while((count = in.read(data,0,BUFFER_SIZE))!=-1){
            outputStream.write(data,0,count);
        }

        data=null;
        return outputStream.toByteArray();
    }
    //把LatLng对象转化为LatLonPoint对象

    public static LatLonPoint convertToLatLonPoint(LatLng latLng){
        return new LatLonPoint(latLng.latitude,latLng.longitude);
    }

    //把LatLonPoint对象转化为LatLng对象

    public static LatLng convertToLatLng(LatLonPoint latLonPoint){
        return new LatLng(latLonPoint.getLatitude(),latLonPoint.getLongitude());
    }

    public static ArrayList<LatLng> convertArrList(List<LatLonPoint> shapes){
        ArrayList<LatLng> lineShapes = new ArrayList<LatLng>();
        for (LatLonPoint point:shapes){
            LatLng latLngTemp = AMapServicesUtil.convertToLatLng(point);
            lineShapes.add(latLngTemp);
        }
        return lineShapes;
    }
    public static Bitmap zoomBitmap(Bitmap bitmap,float res){
        if (bitmap == null){
            return null;
        }
        int width,height;
        width=(int)(bitmap.getWidth()*res);
        height=(int)(bitmap.getHeight()*res);
        Bitmap newbmp = Bitmap.createScaledBitmap(bitmap,width,height,true);
        return newbmp;
    }
}
