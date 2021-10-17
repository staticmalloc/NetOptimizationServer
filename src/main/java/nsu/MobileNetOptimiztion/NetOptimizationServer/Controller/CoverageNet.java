package nsu.MobileNetOptimiztion.NetOptimizationServer.Controller;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import nsu.MobileNetOptimiztion.NetOptimizationServer.DB.ConnectionStatistic;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import nsu.MobileNetOptimiztion.NetOptimizationServer.RequestCoverageNet;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;



public class CoverageNet {
    static final double KILOMETERS_IN_ONE_DEGREE_MERIDIAN = 111.134861111/2;
    static final double KILOMETERS_IN_ONE_DEGREE_ECVATOR = 111.321377778/2;


    private Dao<ConnectionStatistic, Long> dao;
    JdbcConnectionSource connectionSource;
    private float longtitude;
    private float latitude;
    private String connectionType;
    private int netSizeM = 5000;
    private int netStepM = 50;

    public CoverageNet(RequestCoverageNet requestCoverageNet) throws SQLException {
        longtitude = Float.parseFloat(requestCoverageNet.getLongtitude());
        latitude = Float.parseFloat(requestCoverageNet.getLat());
        connectionType = requestCoverageNet.getConnectionType();
        connectionSource = new JdbcConnectionSource("jdbc:postgresql://localhost/netoptimization","postgres","1901Beta");
        dao = DaoManager.createDao(connectionSource, ConnectionStatistic.class);
    }

    public String createCoverageNet() throws JSONException, SQLException {
        JSONObject requestBody = new JSONObject();
        int[] data = new int[0];
        String type = "";
        final float dLat = findDeltaLat(netStepM);
        final float dLong = findDeltaLong(netStepM);
        final float startLat = latitude - findDeltaLat(netSizeM);
        final float startLong = longtitude - findDeltaLong(netSizeM);
        final float finalLat = latitude + findDeltaLat(netSizeM);
        final float finalLong = longtitude + findDeltaLong(netSizeM);
        final int arraySize = netSizeM/netStepM;
        requestBody.put("dlat",dLat);
        requestBody.put("dlong",dLong);
        requestBody.put("startlat",startLat);
        requestBody.put("endlat",finalLat);
        requestBody.put("startlong",startLong);
        requestBody.put("endlong",finalLong);
        List<ConnectionStatistic> dataList = dao.queryBuilder()
                .where().eq("connection_type", connectionType)
                .query();
        LinkedList<String> sigStrengthList = new LinkedList<String>();
        for(ConnectionStatistic cur:dataList){
            sigStrengthList.add(cur.getSig_strength());
        }

        switch (connectionType){
            case "GSM":
                type = "rssi";
                data = makeGSMNet(dataList);
                break;
            case "CDMA": break;
            case "LTE":break;
            case "WCDMA":break;
            default:break;
        }
        requestBody.put("index_type",type);
        requestBody.put("size",arraySize);
        JSONArray array = new JSONArray(data);
        requestBody.put("data", array);
        return requestBody.toString();
    }
    private int[] makeGSMNet(List<ConnectionStatistic> dataList) {
        final float dLat = findDeltaLat(netStepM);
        final float dLong = findDeltaLong(netStepM);
        final float startLat = latitude - findDeltaLat(netSizeM);
        final float startLong = longtitude - findDeltaLong(netSizeM);
        final float finalLat = latitude + findDeltaLat(netSizeM);
        final float finalLong = longtitude + findDeltaLong(netSizeM);
        System.out.println("DLAT: " + dLat);
        System.out.println("DLong: " + dLong);
        System.out.println("Start lat: " + startLat);
        System.out.println("Final lat: " + finalLat);
        System.out.println("Start long: " + startLong);
        System.out.println("Final long: " + finalLong);
        final int arraySize = netSizeM/netStepM;
        int counters[] = new int[arraySize*arraySize];
        int data[] = new int[arraySize*arraySize];
        for(ConnectionStatistic stat:dataList){
            final float curLat = Float.parseFloat(stat.getLat());
            final float curLong = Float.parseFloat(stat.getLongtitude());
            final int rssi = getGSMRSSI(stat);
            if(curLat > finalLat || curLat < startLat) {
                System.out.println("Cur lat: " + curLat);
                continue;
            }
            if(curLong > finalLong || curLong < startLong) {
                System.out.println("Cur long: " + curLong);
                continue;
            }
            final int latId = (int) (0.6*(curLat - startLat)/dLat);
            final int longId = (int)(0.6*(curLong - startLong)/dLong);
            if(latId >= arraySize || longId >= arraySize || latId < 0 || longId < 0) {
                System.out.println("LatID: " + latId);
                System.out.println("LongID: " + longId);
                continue;
            }
//            System.out.println("LatID: " + latId);
//            System.out.println("LongID: " + longId);
//            System.out.println("RSSI: " + rssi);
            int offset = latId * arraySize + longId;
            counters[offset]++;
            data[offset] = (data[offset] + rssi)/counters[offset];
            if((longId + 1) < arraySize){
                offset = latId * arraySize + longId + 1;
                counters[offset]++;
                data[offset] = (data[offset] + rssi)/counters[offset];
            }
            if((latId + 1) < arraySize) {
                offset = (latId + 1) * arraySize + longId;
                counters[offset]++;
                data[offset] = (data[offset] + rssi)/counters[offset];
            }
            if((latId + 1) < arraySize && (longId + 1) < arraySize){
                offset = (latId + 1) * arraySize + longId + 1;
                counters[offset]++;
                data[offset] = (data[offset] + rssi)/counters[offset];
            }
        }
//        for(int i = 0; i < arraySize; i++){
//            for(int j = 0; j < arraySize; j++){
//                System.out.print(" " + data[i*arraySize + j] + " ");
//            }
//            System.out.println();
//            System.out.println("____________________");
//        }
        return data;
    }

    private int getGSMRSSI(ConnectionStatistic statistic){
        if(!statistic.getSig_strength().contains("rssi="))
            return 0;
        String rssiString = statistic.getSig_strength().substring(statistic.getSig_strength().indexOf("rssi="));
        rssiString = rssiString.substring(rssiString.indexOf("=")+1,rssiString.indexOf(" "));
        return Integer.parseInt(rssiString);
    }

    private float findDeltaLat(int distInMeters){
        double degreesInKilometer = 0.6/KILOMETERS_IN_ONE_DEGREE_MERIDIAN;
        float result =  (float) (distInMeters*degreesInKilometer/1000);
        //System.out.println("Result dLat: " + result);
        return result;
    }

    private float findDeltaLong(int distInMeters){
        double degreesInKilometer = 0.6/(Math.cos(latitude *Math.PI/180)*KILOMETERS_IN_ONE_DEGREE_ECVATOR);
        float result = (float)(distInMeters*degreesInKilometer/1000);
        //System.out.println("Result dLong: " + result);
        return result;
    }
}
