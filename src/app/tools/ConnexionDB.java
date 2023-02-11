package app.tools;

import config.Env;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

// todo find best way to open many connection to database  and fix lost connections
public class ConnexionDB {

    private static LinkedList<Connection> connectionsList = new LinkedList<>();
    private static LinkedList<Connection> connectionsList2 = new LinkedList<>();

    private static int ConIndex = -1;

    public static void start() {
        Console.printlnWithDate("connecting to " + Env.Database.db_host + " ...", Console.Colors.getCYAN());
        fillConnectionsList();
        Console.printlnWithDate("connected to database", Console.Colors.getGREEN());
        Console.println("################################################################################################", Console.Colors.getRED());
        Console.printlnWithDate("connecting to " + Env.Database2.db_host2 + " ...", Console.Colors.getCYAN());
        fillConnectionsList2();
        Console.printlnWithDate("connected to the ressource database", Console.Colors.getGREEN());
    }

    private static void fillConnectionsList() {
        ThreadPool threadPool = new ThreadPool();
        threadPool.newFixedThreadPool(10);
        for (int i = 0; i < Env.Database.REQUIRED_CONNECTIONS_ELEMENTS - connectionsList.size(); i++)
            threadPool.getRunnables().add(ConnexionDB::openConnection);
        threadPool.run();
        if (connectionsList.size() < Env.Database.REQUIRED_CONNECTIONS_ELEMENTS) {
            fillConnectionsList();
        } else {
            Console.printlnWithDate("Done . you have " + connectionsList.size() + " connections with database", Console.Colors.getGREEN());
        }
    }
    private static void fillConnectionsList2() {
        ThreadPool threadPool1 = new ThreadPool();
        threadPool1.newFixedThreadPool(10);
        for (int i = 0; i < Env.Database2.REQUIRED_CONNECTIONS_ELEMENTS2 - connectionsList.size(); i++)
            threadPool1.getRunnables().add(ConnexionDB::openConnection);
        threadPool1.run();
        if (connectionsList.size() < Env.Database2.REQUIRED_CONNECTIONS_ELEMENTS2) {
            fillConnectionsList2();
        } else {
            Console.printlnWithDate("Done . you have " + connectionsList.size() + " connections with Ressource database ", Console.Colors.getGREEN());
        }
    }

    public static Connection newConnection() throws SQLException {
        return DriverManager.getConnection(Env.Database.db_host, Env.Database.login, Env.Database.password);
    }
    public static Connection newConnection2() throws SQLException {
        return DriverManager.getConnection(Env.Database2.db_host2, Env.Database2.login2, Env.Database2.password2);
    }

    private static void openConnection() {
        try {
            connectionsList.add(newConnection());
            Console.printInSameLine("Processing: " + connectionsList.size() + "/" + Env.Database.REQUIRED_CONNECTIONS_ELEMENTS + "  " + Console.getAnimationChars()[connectionsList.size() % 4], Console.Colors.getCYAN());
            connectionsList2.add(newConnection2());
            Console.printInSameLine("Processing: " + connectionsList.size() + "/" + Env.Database2.REQUIRED_CONNECTIONS_ELEMENTS2 + "  " + Console.getAnimationChars()[connectionsList.size() % 4], Console.Colors.getCYAN());
        }
        catch (Exception e) {
            Console.printStackTrace(e);
            Logger.getLogger(ConnexionDB.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private static void incConIndex() {
        if (ConIndex >= connectionsList.size() - 1) {
            ConIndex = 0;
        } else {
            ConIndex++;
        }
    }

    static java.sql.Connection getDbConnect() {
        incConIndex();
        try {
            Connection con = connectionsList.get(ConIndex);
            if (con == null) {
                Console.print("con null <|> ");
                connectionsList.remove(ConIndex);
                connectionsList.add(newConnection());
                return getDbConnect();
            } else {
                return con;
            }
        } catch (Exception e) {
            Console.println("catch get con  ");
            try {
                connectionsList.remove(ConIndex);
                connectionsList.add(newConnection());
                return getDbConnect();
            } catch (Exception ee) {
                Console.print("   |||    catch get con  -> replace \n");
            }
            return getDbConnect();
        }
    }
    private static void incConIndex2() {
        if (ConIndex >= connectionsList2.size() - 1) {
            ConIndex = 0;
        } else {
            ConIndex++;
        }
    }

    static java.sql.Connection getDbConnect2() {
        incConIndex2();
        try {
            Connection con = connectionsList2.get(ConIndex);
            if (con == null) {
                Console.print("con null <|> ");
                connectionsList.remove(ConIndex);
                connectionsList.add(newConnection2());
                return getDbConnect2();
            } else {
                return con;
            }
        } catch (Exception e) {
            Console.println("catch get con2  ");
            try {
                connectionsList.remove(ConIndex);
                connectionsList.add(newConnection2());
                return getDbConnect2();
            } catch (Exception ee) {
                Console.print("   |||    catch get con2  -> replace \n");
            }
            return getDbConnect2();
        }
    }


}




