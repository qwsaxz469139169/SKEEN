package ac.uk.ncl.gyc.skeen.mysql;

import java.sql.*;

/**
 * Created by GYC on 2020/7/2.
 */
public class DBdriver {
    // 定义 数据库驱动
    private static String driverClass;
    // 定义 数据库的连接
    private static String url;
    // 定义 数据库用户
    private static String user;
    // 定义 数据库用户的密码
    private static String password;

    static {
        // 驱动程序名
        driverClass = "com.mysql.jdbc.Driver";
        // URL指向要访问的数据库名scutcs
        url = "jdbc:mysql://localhost:3306/raft";
        // MySQL配置时的用户名
        user = "root";
        // MySQL配置时的密码
        password = "root";

        try {
            // 类加载-->驱动
            Class.forName(driverClass);
        } catch (ClassNotFoundException e) {
            // 转换异常抛出
            throw new ExceptionInInitializerError("加载驱动失败");
        }

    }

    public static DBdriver getInstance() {
        return DefaultDBdriverLazyHolder.INSTANCE;
    }

    private static class DefaultDBdriverLazyHolder {

        private static final DBdriver INSTANCE = new DBdriver();
    }

    /**
     * 获取连接
     *
     * @return: conn
     */
    public static Connection getConnection() {
        try {
            //连接类型  连接对象  =  驱动管理中的获取连接(连接，用户名，密码)
            Connection conn = DriverManager.getConnection(url, user, password);
            // 将连接进行返回
            return conn;
        } catch (Exception e) {
            // 转换异常抛出
            throw new RuntimeException("链接数据库的url或用户名密码错误,请检查您的配置文件");
        }
    }

    public static void select(String message) throws SQLException {
        Connection conn = getConnection();
        Statement statement;
        ResultSet rs = null;
        try {
            statement = conn.createStatement();
            rs = statement.executeQuery(message);

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            conn.close();
            rs.close();

        }
    }


    public static void add(String message) {
        Connection conn = getConnection();
        PreparedStatement psql;

        try {
            psql = conn.prepareStatement("insert into message (name) " + "values(?)");

            psql.setString(1, message);

            psql.executeUpdate();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
    }

    public static void updateLatency(String message, String node, long latency) {
        Connection conn = getConnection();
        PreparedStatement psql;
        String colum = "";


        if (node.equals("localhost:8775")) {
            colum = "la";
        } else if (node.equals("localhost:8776")) {
            colum = "lb";
        } else if (node.equals("localhost:8777")) {
            colum = "lc";
        }

        try {
            psql = conn.prepareStatement("update message set " + colum + " = ? where name = ?");
            psql.setLong(1, latency);
            psql.setString(2, message);
            psql.executeUpdate();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }


    }

    public static void updateEM(String message, String node, int em) {
        Connection conn = getConnection();
        PreparedStatement psql;
        String colum = "";

        if (node.equals("localhost:8775")) {
            colum = "ma";
        } else if (node.equals("localhost:8776")) {
            colum = "mb";
        } else if (node.equals("localhost:8777")) {
            colum = "mc";
        }


        try {
            psql = conn.prepareStatement("update message set " + colum + " = ? where name = ?");
            psql.setInt(1, em);
            psql.setString(2, message);
            psql.executeUpdate();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}


