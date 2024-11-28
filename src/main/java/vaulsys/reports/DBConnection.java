package vaulsys.reports;

import vaulsys.util.ConfigUtil;
import org.hibernate.Session;
import vaulsys.util.DBConfigUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by HP on 9/15/2017.
 */
public class DBConnection {
    private static String DB_DRIVER = null;
    private static String DB_CONNECTION = null;
    private static String DB_USER = null;
    private static String DB_PASSWORD = null;

    public static String ClassName = "DBConnection";
    public static String FunctionName = null;
    public static String Log_File_Prefix = null;
    //public static Logging Logger_DBConnection = new Logging();

    private Session session = null;
    private static Connection connection;


    public DBConnection()
    {

    }

    public static Connection getConnection()
    {
        return connection;
    }

    public void setConnection(Connection Conn)
    {
        this.connection = Conn;
    }

    public static Connection InitializeDBConn()
    {
        FunctionName = "InitializeDBConn";

        Log_File_Prefix = "Import-Main-Log-";

        try
        {
            //m.rehman: changing below to read db config from external file
            /*
            DB_DRIVER = ConfigUtil.getProperty(ConfigUtil.DB_DRIVER);
            DB_CONNECTION = ConfigUtil.getProperty(ConfigUtil.DB_URL);
            DB_USER = ConfigUtil.getProperty(ConfigUtil.DB_USERNAME);
            DB_PASSWORD = ConfigUtil.getProperty(ConfigUtil.DB_PASSWORD);
            */

            DB_DRIVER = ConfigUtil.getProperty(ConfigUtil.DB_DRIVER);
            DB_CONNECTION = DBConfigUtil.getDecProperty(DBConfigUtil.DB_URL);
            DB_USER = DBConfigUtil.getDecProperty(DBConfigUtil.DB_USERNAME);
            DB_PASSWORD = DBConfigUtil.getDecProperty(DBConfigUtil.DB_PASSWORD);
        }
        catch (Exception e)
        {
            /*Logger_DBConnection.Log(Log_File_Prefix, ClassName, FunctionName, "Error occurred during database connection initialization", Enums.LogEvent.Error_Message);
            Logger_DBConnection.Log(Log_File_Prefix, ClassName, FunctionName, GeneralUtil.getStackTrace(e), Enums.LogEvent.Error_Message);
            Logger_DBConnection.InsertLine();*/
        }

        connection = getDBConnection();

        return connection;
    }

    private static Connection getDBConnection() {

        Connection dbConnection = null;

        try {

            Class.forName(DB_DRIVER);

        } catch (ClassNotFoundException e) {

            System.out.println(e.getMessage());

        }

        try {

            dbConnection = DriverManager.getConnection(
                    DB_CONNECTION, DB_USER,DB_PASSWORD);
            return dbConnection;

        } catch (SQLException e) {

            System.out.println(e.getMessage());

        }

        return dbConnection;

    }
}
