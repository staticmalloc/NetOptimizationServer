package nsu.MobileNetOptimiztion.NetOptimizationServer.Controller

import com.google.gson.Gson
import com.j256.ormlite.dao.Dao
import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.jdbc.JdbcConnectionSource
import nsu.MobileNetOptimiztion.NetOptimizationServer.DB.ConnectionStatistic
import nsu.MobileNetOptimiztion.NetOptimizationServer.RequestEntities.CoverageNetRequest
import nsu.MobileNetOptimiztion.NetOptimizationServer.RequestEntities.CoverageRequestEntity
import nsu.MobileNetOptimiztion.NetOptimizationServer.Utils.GeoUtils
import nsu.MobileNetOptimiztion.NetOptimizationServer.Utils.GeoVec
import nsu.MobileNetOptimiztion.NetOptimizationServer.Utils.Location
import org.json.JSONException
import java.sql.SQLException
import kotlin.math.abs

class CoverageNet(private val netProperties: CoverageRequestEntity) {
    private val dao: Dao<ConnectionStatistic, Long>
    var connectionSource: JdbcConnectionSource

    @Throws(JSONException::class, SQLException::class)
    fun createCoverageNet(): String {
        var data = IntArray(0)
        var type = ""
        val dataList = dao.queryBuilder()
            .where().eq("connection_type", netProperties.connectionType)
            .query()
        return when (netProperties.connectionType) {
            "GSM" -> {
                Gson().toJson(makeGSMNet(dataList))
            }
            "CDMA" -> {
                ""
            }
            "LTE" -> {
                ""
            }
            "WCDMA" -> {
                ""
            }
            else -> {
                ""
            }
        }
    }

    private fun <T> create2DArray(sizeX: Int, sizeY: Int): ArrayList<ArrayList<T>> {
        val arrayOfArray = ArrayList<ArrayList<T>>(sizeY)
        for (i in 0..sizeY - 1) {
            arrayOfArray[i] = ArrayList(sizeX)
        }
        return arrayOfArray
    }

    private fun interpolateColumn(net: ArrayList<ArrayList<Location>>, index: Int) {
        val lastIndex = net.size - 1
        val first = net[0][index]
        val last = net[lastIndex][index]
        val dlat = (first.latitude - last.latitude) / net.size
        val dlon = (first.latitude - last.latitude) / net.size
        for (i in 0..lastIndex) {

        }
    }

    private fun interpolateNet(
        net: ArrayList<ArrayList<Location>>,
        p00: Location,
        p01: Location,
        p10: Location,
        p11: Location,
    ) {
        val sizeY = net.size
        val sizeX = net[0].size
        net[0][0] = p00
        net[sizeY - 1][0] = p01
        net[0][sizeX - 1] = p10
        net[sizeY - 1][sizeX - 1] = p11

    }

    private fun makeGSMNet(dataList: List<ConnectionStatistic>): CoverageNetRequest {
        val loc00 = Location(netProperties.lat00.toFloat(), netProperties.lon00.toFloat())
        val loc11 = Location(netProperties.lat11.toFloat(), netProperties.lon11.toFloat())
        val geoVec00 = GeoVec(loc00)
        val geoVec01 = GeoVec(Location(loc11.latitude, loc00.longtitude))
        val geoVec10 = GeoVec(Location(loc00.latitude, loc11.longtitude))

        val sizeX = abs((GeoUtils.distanceCartesian(geoVec00, geoVec01) / netProperties.step).toInt())
        val sizeY = abs((GeoUtils.distanceCartesian(geoVec00, geoVec10) / netProperties.step).toInt())
        val dLat = (loc11.latitude - loc00.latitude) / sizeY
        val dLong = (loc11.longtitude - loc00.longtitude) / sizeX

        val counters = IntArray(sizeX * sizeY) { 0 }
        val data = IntArray(sizeX * sizeY) { 0 }
        dataList.filter {
            it.lat.toFloat() > loc00.latitude && it.lat.toFloat() < loc11.latitude &&
                    it.longtitude.toFloat() > loc00.longtitude && it.longtitude.toFloat() < loc11.longtitude
        }.forEach { stat ->
            val curLat = stat.lat.toFloat()
            val curLong = stat.longtitude.toFloat()
            val rssi = getGSMRSSI(stat)
            val latId = ((curLat - loc00.latitude) / dLat).toInt()
            val longId = ((curLong - loc00.longtitude) / dLong).toInt()
            var offset = latId * sizeX + longId
            counters[offset]++
            data[offset] = (data[offset] + rssi)
            /*if (longId + 1 < arraySize) {
                offset = latId * arraySize + longId + 1
                counters[offset]++
                data[offset] = (data[offset] + rssi) / counters[offset]
            }
            if (latId + 1 < arraySize) {
                offset = (latId + 1) * arraySize + longId
                counters[offset]++
                data[offset] = (data[offset] + rssi) / counters[offset]
            }
            if (latId + 1 < arraySize && longId + 1 < arraySize) {
                offset = (latId + 1) * arraySize + longId + 1
                counters[offset]++
                data[offset] = (data[offset] + rssi) / counters[offset]
            }*/
        }
        for (i in 0 until sizeX * sizeY) {
            if (counters[i] > 0)
                data[i] = data[i] / counters[i]
        }
        return CoverageNetRequest(sizeX, sizeY, data, "rssi")
    }

    private fun getGSMRSSI(statistic: ConnectionStatistic): Int {
        if (!statistic.sig_strength.contains("rssi=")) return 0
        var rssiString = statistic.sig_strength.substring(statistic.sig_strength.indexOf("rssi="))
        rssiString = rssiString.substring(rssiString.indexOf("=") + 1, rssiString.indexOf(" "))
        return rssiString.toInt()
    }

    init {
        connectionSource = JdbcConnectionSource("jdbc:postgresql://localhost/netoptimization", "postgres", "1901Beta")
        dao = DaoManager.createDao(
            connectionSource,
            ConnectionStatistic::class.java
        )
    }
}