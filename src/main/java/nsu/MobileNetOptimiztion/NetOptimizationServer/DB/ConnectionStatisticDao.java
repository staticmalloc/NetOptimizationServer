package nsu.MobileNetOptimiztion.NetOptimizationServer.DB;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

public class ConnectionStatisticDao {
    JdbcPooledConnectionSource connectionSource;
    public Dao<ConnectionStatistic, Long> connectionStatisticsDao;

    public ConnectionStatisticDao() throws Exception {
        if(connectionStatisticsDao == null) {
            connectionSource = new JdbcPooledConnectionSource("jdbc:h2:mem:myDb");
            TableUtils.createTableIfNotExists(connectionSource, ConnectionStatistic.class);
            connectionStatisticsDao = DaoManager.createDao(connectionSource, ConnectionStatistic.class);
            connectionSource.close();
        }
    }

    public void addNewEntry(ConnectionStatistic statistic) throws SQLException {
        connectionStatisticsDao.create(statistic);
    }

    public long getSizeOfConnectionInfoTable() throws SQLException{
        return connectionStatisticsDao.countOf();
    }

}
