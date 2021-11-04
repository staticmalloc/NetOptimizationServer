package nsu.MobileNetOptimiztion.NetOptimizationServer.Controller;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.table.TableUtils;
import nsu.MobileNetOptimiztion.NetOptimizationServer.ConnectionInfo;
import nsu.MobileNetOptimiztion.NetOptimizationServer.DB.ConnectionStatistic;
import nsu.MobileNetOptimiztion.NetOptimizationServer.DB.ConnectionStatisticDao;
import nsu.MobileNetOptimiztion.NetOptimizationServer.RequestCoverageNet;
import nsu.MobileNetOptimiztion.NetOptimizationServer.RequestEntities.CoverageRequestEntity;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.sql.SQLException;

@RestController
public class APIController {
    private Dao<ConnectionStatistic, Long> dao;
    JdbcConnectionSource connectionSource;
    @GetMapping("/test")
    String hello(){
        System.out.println("Hello is called");
        return "Hello";}

    @PostMapping(path = "/data", consumes = "application/json")
    void AcceptData(@RequestBody ConnectionInfo connectionInfo) throws Exception {
        System.out.println(connectionInfo.getCoords().getTime());
        System.out.println(connectionInfo.getCoords().getLat());
        System.out.println(connectionInfo.getCoords().getLongtitude());
        System.out.println(connectionInfo.getCell_info().getConnection_type());
        System.out.println(connectionInfo.getCell_info().getIdentity());
        System.out.println(connectionInfo.getCell_info().getSig_strength());

        if(dao == null) {
            connectionSource = new JdbcConnectionSource("jdbc:postgresql://localhost/netoptimization","postgres","1901Beta");
            TableUtils.createTableIfNotExists(connectionSource, ConnectionStatistic.class);
            dao = DaoManager.createDao(connectionSource, ConnectionStatistic.class);
        }
        ConnectionStatistic statistic = new ConnectionStatistic();
        statistic.connectionStatisticFromInfo(connectionInfo, dao.countOf());
        dao.create(statistic);
        System.out.println("Current table size: " + dao.countOf());
    }

    @PostMapping(path = "/coverage", consumes = "application/json")
    String SendCoverageNet(@RequestBody CoverageRequestEntity request) throws JSONException, SQLException {
        return new CoverageNet(request).createCoverageNet();
    }
}
