package com.howard.www.db;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.List;

public class DuridDBUtil {

    private static final Logger logger = LogManager.getLogger("duridDBUtil");

    private static DruidDataSource druidDataSource;

    public static void insertBatch(List<String> insertBatchSql) throws SQLException {
        if(insertBatchSql==null&& insertBatchSql.size()>0){

        }else{
            Connection connection = null;
            PreparedStatement statement = null;
            ResultSet resultSet = null;
            try {
                connection = getDruidConnection();
                connection.setAutoCommit(false);
                for(String insertSql:insertBatchSql){
                    if(insertSql!=null&&!"".equals(insertSql)){
                        statement = connection.prepareStatement(insertSql);
                        statement.addBatch();
                    }
                }
                statement.executeBatch();
                connection.commit();
                statement.clearBatch();
            } finally {
                closeResource(connection, statement, resultSet);
            }
        }

    }


    private static DruidDataSource getDruidDataSource() throws SQLException {
        if (druidDataSource == null) {
            synchronized (DuridDBUtil.class) {
                if (druidDataSource == null) {
                    druidDataSource = createDruidDataSource();
                    return druidDataSource;
                }
            }
        }
        return druidDataSource;
    }

    public static DruidDataSource createDruidDataSource() throws SQLException {
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setUrl("jdbc:mysql://127.0.0.1:3306/log?characterEncoding=utf8&autoReconnect=true&serverTimezone=UTC");
        druidDataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        druidDataSource.setUsername("root");
        druidDataSource.setPassword("warriors");
        druidDataSource.setMaxActive(20);
        druidDataSource.setInitialSize(1);
        druidDataSource.setMinIdle(1);
        druidDataSource.setMaxWait(60000);
        druidDataSource.setValidationQuery("select 1 from dual");
        druidDataSource.setTimeBetweenEvictionRunsMillis(60000);
        druidDataSource.setMinEvictableIdleTimeMillis(300000);
        druidDataSource.setTestWhileIdle(true);
        druidDataSource.setTestOnBorrow(false);
        druidDataSource.setTestOnReturn(false);
        druidDataSource.setPoolPreparedStatements(true);
        druidDataSource.setMaxPoolPreparedStatementPerConnectionSize(20);
        druidDataSource.init();
        return druidDataSource;
    }

    public static DruidPooledConnection getDruidConnection() throws SQLException {
        DruidDataSource druidDataSource = getDruidDataSource();
        DruidPooledConnection connection = druidDataSource.getConnection();
        return connection;
    }

    public static void closeResource(Connection connection,
                                     Statement statement, ResultSet resultSet) throws SQLException {

        if (resultSet != null) {
            resultSet.close();
        }
        if (statement != null) {
            statement.close();
        }
        if (connection != null) {
            connection.close();
        }
    }
}
