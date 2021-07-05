package net.lt_schmiddy.serverdiscordbot.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import java.util.Date;

import com.mojang.authlib.GameProfile;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.lt_schmiddy.serverdiscordbot.config.ConfigHandler;

public class DiscordUserDatabase {
    public class SqlQueries {
        public static String CREATE_USER_TABLE = """
            CREATE TABLE IF NOT EXISTS discord_users (
                minecraft_id TEXT PRIMARY KEY,
                discord_id TEXT NOT NULL
            );""";

        public static String CREATE_PAIRING_TABLE = """
            CREATE TABLE IF NOT EXISTS discord_pair_requests (
                pair_request_code TEXT PRIMARY KEY,
                pair_request_timestamp DATETIME,
                minecraft_id TEXT NOT NULL,
                discord_id TEXT NOT NULL
            );
            """;

        // I'm aware that the organization of this section is a but... unusual.
        // I'm placing each SQL strings with the function that uses it. It's not the usual way one
        // organises a class definition, but it's very useful here.

        public static String ADD_PAIR_REQUEST = """
        INSERT INTO discord_pair_requests (pair_request_code, pair_request_timestamp, minecraft_id, discord_id)
        VALUES (?, ?, ?, ?);
        """;
        public static PreparedStatement addPairRequest(
            Connection c, 
            String pair_request_code, 
            Timestamp pair_request_timestamp, 
            String minecraft_id, 
            String discord_id
        ){
            PreparedStatement p = null;
            try {
                p = c.prepareStatement(ADD_PAIR_REQUEST);
                p.setString(1, pair_request_code);
                p.setTimestamp(2, pair_request_timestamp);
                p.setString(3, minecraft_id);
                p.setString(4, discord_id);

                return p;
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }
        
        public static String COUNT_PAIR_REQUESTS = """
        SELECT COUNT(*) FROM discord_pair_requests WHERE pair_request_code == ?;
        """;
        public static PreparedStatement countPairRequests(
            Connection c, 
            String pair_request_code
        ){
            PreparedStatement p = null;
            try {
                p = c.prepareStatement(COUNT_PAIR_REQUESTS);
                p.setString(1, pair_request_code);
                
                return p;
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }
        
        public static String GET_PAIR_REQUEST = """
        SELECT * FROM discord_pair_requests WHERE pair_request_code == ?;
        """;
        public static PreparedStatement getPairRequest(
            Connection c, 
            String pair_request_code
        ){
            PreparedStatement p = null;
            try {
                p = c.prepareStatement(GET_PAIR_REQUEST);
                p.setString(1, pair_request_code);
                
                return p;
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }

        public static String DELETE_PAIR_REQUEST = """
            DELETE FROM discord_pair_requests WHERE pair_request_code == ?;
            """;
        public static PreparedStatement deletePairRequest(
            Connection c, 
            String pair_request_code
        ){
            PreparedStatement p = null;
            try {
                p = c.prepareStatement(DELETE_PAIR_REQUEST);
                p.setString(1, pair_request_code);
                
                return p;
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }


        public static String ADD_PAIRED_USER = """
            INSERT INTO discord_users (minecraft_id, discord_id)
            VALUES (?, ?);
            """;
        
        public static PreparedStatement addPairedUser(
            Connection c, 
            String minecraft_id, 
            String discord_id
        ){
            PreparedStatement p = null;
            try {
                p = c.prepareStatement(ADD_PAIRED_USER);
                p.setString(1, minecraft_id);
                p.setString(2, discord_id);

                return p;
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        } 


        public static String GET_PAIRED_USER = """
            SELECT minecraft_id, discord_id FROM discord_users
            WHERE minecraft_id = ?;
            """;
        public static PreparedStatement getPairedDiscordUser(
            Connection c, 
            String minecraft_id
        ){
            PreparedStatement p = null;
            try {
                p = c.prepareStatement(GET_PAIRED_USER);
                p.setString(1, minecraft_id);

                return p;
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }

    }

    Connection conn;

    public DiscordUserDatabase() {
        try {
            // db parameters
            conn = DriverManager.getConnection("jdbc:sqlite:" + ConfigHandler.config.discordUserDb);
            setupDb();
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
        stmt.execute(SqlQueries.CREATE_USER_TABLE);
        stmt.execute(SqlQueries.CREATE_PAIRING_TABLE);
    }

    public Connection getConnection() {
        return conn;
    }

    public String createPairRequest(GameProfile profile, String discordId, int length) {
        String code;
        try {
            while (true) { 
                code = Utils.generatePairCode(length);
                // PreparedStatement ps = SqlQueries.countPairRequests(conn, code);
                // ps.execute();
                // ResultSet r = ps.getResultSet();

                ResultSet r = SqlQueries.countPairRequests(conn, code).executeQuery();
                if (r.getInt(1) == 0) {break;}
            }

            SqlQueries.addPairRequest(conn, code, new Timestamp(new Date().getTime()), profile.getId().toString(), discordId).execute();
            return code;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean confirmPairRequest(GameProfile profile, String discordId, String authCode) {
        try {
            
            ResultSet count_r = SqlQueries.countPairRequests(conn, authCode).executeQuery();
            if (count_r.getInt(1) == 0) {
                System.out.println("Auth Code " + authCode + " not found...");
                return false;
            }

            ResultSet r = SqlQueries.getPairRequest(conn, authCode).executeQuery();
            // r.first();

            String r_authCode = r.getString("pair_request_code");
            String r_discordId = r.getString("discord_id");
            String r_minecraftId = r.getString("minecraft_id");

            // confirming match:
            if (
                r_authCode.equals(authCode)
                && r_discordId.equals(discordId)
                && r_minecraftId.equals(profile.getId().toString())
            ) {
                SqlQueries.addPairedUser(conn, r_minecraftId, r_discordId).execute();
                SqlQueries.getPairRequest(conn, authCode).executeQuery();
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getDiscordIDFromMinecraft(GameProfile profile){
        ResultSet r = null;
        try {
            r = SqlQueries.getPairedDiscordUser(conn, profile.getId().toString()).executeQuery();
            if (!r.next()) {return null;}
            
            return r.getString("discord_id");

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        
    }

    public User getDiscordUserFromMinecraft(JDA jda, GameProfile profile){
        ResultSet r = null;
        try {
            r = SqlQueries.getPairedDiscordUser(conn, profile.getId().toString()).executeQuery();
            if (!r.next()) {return null;}
            
            return jda.retrieveUserById(r.getString("discord_id")).complete();

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Member getDiscordGuildMemberFromMinecraft(JDA jda, Guild guild, GameProfile profile){
        ResultSet r = null;
        try {
            r = SqlQueries.getPairedDiscordUser(conn, profile.getId().toString()).executeQuery();
            if (!r.next()) {return null;}
            
            User u = jda.retrieveUserById(r.getString("discord_id")).complete();
            if (u == null) {return null;}

            return guild.getMember(u);

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

    }
}
