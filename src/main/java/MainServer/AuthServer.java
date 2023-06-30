package MainServer;

import java.sql.*;

public class AuthServer {
    private static Connection connection;
    private static Statement statement;
    private static PreparedStatement ps;

    public static void connect() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:users.db");
            connection.setAutoCommit(false);

            statement = connection.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS users(id INTEGER PRIMARY KEY AUTOINCREMENT, login TEXT UNIQUE NOT NULL, password TEXT NOT NULL, nickname TEXT UNIQUE NOT NULL);");
            connection.commit();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            connection.rollback();
        }
    }

    public static String getNickByLoginPass(String login, String password){
        String sql = String.format("select nickname from users where login = '%s' and password = '%s';", login, password);

        try {
            ResultSet resultSet = statement.executeQuery(sql);

            if (resultSet.next()){
                return resultSet.getString(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
        return null;
    }

    public static void disconnect(){
        try {
            connection.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void signUpUser(String login, String password, String nickname){
        String insert = String.format("insert into users (login, password, nickname) values ('%s', '%s', '%s');", login, password, nickname);

        try {
            connect();
            statement.execute(insert);
        } catch (NullPointerException | SQLException e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
    }

    public static boolean checkUser(String login, String nickname) throws SQLException{
        String sql = String.format("select count(*) from users where login = '%s' or nickname = '%s';", login, nickname);

        connect();
        ResultSet resultSet = statement.executeQuery(sql);

        if (resultSet.next()) {
            int result = Integer.parseInt(resultSet.getString(1));
            if (result >= 1) {
                disconnect();
                return false;
            }
        }

        disconnect();
        return true;
    }

    public static boolean checkUser(String newNick) throws SQLException{
        String sql = String.format("select count(*) from users where nickname = '%s';", newNick);

        connect();
        ResultSet resultSet = statement.executeQuery(sql);

        if (resultSet.next()) {
            int result = Integer.parseInt(resultSet.getString(1));
            if (result == 1) {
                disconnect();
                return false;
            }
        }

        disconnect();
        return true;
    }

    public static boolean checkUserForChange(String login, String password) throws SQLException{
        String sql = String.format("select count(*) from users where login = '%s' and password = '%s';", login, password);

        connect();
        ResultSet resultSet = statement.executeQuery(sql);

        if (resultSet.next()) {
            int result = Integer.parseInt(resultSet.getString(1));
            if (result == 1) {
                disconnect();
                return true;
            }
        }

        disconnect();
        return false;
    }

    public static boolean changeNickname(String newNick, String oldNick) throws SQLException {
        /*String update = String.format("UPDATE users SET nickname = '%s' WHERE nickname = '%s';", newNick, oldNick);

        try {
            connect();
            return statement.execute(update); // на этой строке выскакивает ошибка [SQLITE_BUSY]  The database file is locked (database is locked)
        } catch (NullPointerException | SQLException e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
        return false;*/
        try {
            connect();
            ps = connection.prepareStatement("UPDATE users SET nickname = ? WHERE nickname = ?;");
            ps.setString(1, newNick);
            ps.setString(2, oldNick);
            ps.execute(); //на этой строке такая же ошибка [SQLITE_BUSY]  The database file is locked (database is locked)

            connection.commit();
            disconnect();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            connection.rollback();
            disconnect();
            return false;
        }

    }
}
