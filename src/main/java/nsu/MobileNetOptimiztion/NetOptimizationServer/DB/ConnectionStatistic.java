package nsu.MobileNetOptimiztion.NetOptimizationServer.DB;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import nsu.MobileNetOptimiztion.NetOptimizationServer.ConnectionInfo;

@DatabaseTable(tableName = "statistic")
public class ConnectionStatistic {
    @DatabaseField(id = true)
    private long entryId;
    @DatabaseField
    private String lat;
    @DatabaseField
    private String longtitude;
    @DatabaseField
    private String date;
    @DatabaseField
    private String connection_type;
    @DatabaseField
    private String identity;
    @DatabaseField
    private String sig_strength;

    public void connectionStatisticFromInfo(ConnectionInfo info, long size){
        entryId = size+1;
        lat = info.getCoords().getLat();
        longtitude = info.getCoords().getLongtitude();
        date = info.getCoords().getTime();
        connection_type = info.getCell_info().getConnection_type();
        identity = info.getCell_info().getIdentity();
        sig_strength = info.getCell_info().getSig_strength();
    }

    public ConnectionStatistic(){
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getEntryId() {
        return entryId;
    }

    public void setEntryId(long entryId) {
        this.entryId = entryId;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLongtitude() {
        return longtitude;
    }

    public void setLongtitude(String longtitude) {
        this.longtitude = longtitude;
    }

    public String getConnection_type() {
        return connection_type;
    }

    public void setConnection_type(String connection_type) {
        this.connection_type = connection_type;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String getSig_strength() {
        return sig_strength;
    }

    public void setSig_strength(String sig_strength) {
        this.sig_strength = sig_strength;
    }
}
