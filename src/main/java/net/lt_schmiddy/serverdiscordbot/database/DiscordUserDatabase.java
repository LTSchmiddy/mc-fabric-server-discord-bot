package net.lt_schmiddy.serverdiscordbot.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import net.lt_schmiddy.serverdiscordbot.config.ConfigHandler;

public class DiscordUserDatabase {
    public class SqlDefinitions {
        public static String CREATE_TABLE = """
            CREATE TABLE IF NOT EXISTS discord_users (
                minecraft_id TEXT PRIMARY KEY,
                discord_id TEXT NOT NULL,
                paired BOOLEAN NOT NULL,
                pair_request_timestamp DATETIME,
                pair_request_code text
            );
            """;
    }


    Connection conn;

    public DiscordUserDatabase() {
        try {
            // db parameters
            conn = DriverManager.getConnection("jdbc:sqlite:" + ConfigHandler.config.discordUserDb);
            System.out.println("Connection to SQLite has been established.");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    void setupDb() throws SQLException {
        Statement stmt = conn.createStatement();
        
        // Print SQL Version:
        stmt.execute("select sqlite_version();");
        System.out.println("SQLite Version: " + stmt.getResultSet().getString(1));

        // Create User Table:
        stmt.execute(SqlDefinitions.CREATE_TABLE);
    }

    public Connection getConnection() {
        return conn;
    }
}
