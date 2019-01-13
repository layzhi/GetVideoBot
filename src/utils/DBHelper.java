package utils;

import twitter4j.*;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class DBHelper {
    private static Connection connection;
    private static final String DATABASE = "jdbc:sqlite:C:\\Users\\Lite\\Desktop\\Java\\TwitterChatBot\\src\\resources\\tweet-records.db";

    public static void createDatabase(){
        try {
            connection = DriverManager.getConnection(DATABASE);
            if (connection != null){
                System.out.println("Database created.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void createNewTable() {
        String sql = "CREATE TABLE TweetRecords (\n" +
                " ID integer PRIMARY KEY AUTOINCREMENT NOT NULL,\n" +
                " Username text NOT NULL,\n" +
                " MentionID text NOT NULL UNIQUE,\n" +
                " VideoTweetID text NOT NULL,\n" +
                " VideoURL text NOT NULL,\n" +
                "TimeStamp DATETIME DEFAULT CURRENT_TIMESTAMP\n" +
                ");";
        try {
            connection = DriverManager.getConnection(DATABASE);
            Statement stmt = connection.createStatement();

            stmt.execute(sql);
            connection.close();
            System.out.println("Database table created successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void saveTweet(String username, String mentionID, String videoTweetID, String videoURL) {
        String sql = "INSERT INTO TweetRecords(Username, MentionID, VideoTweetID, VideoURL) VALUES(?,?,?,?)";
        try {
            connection = DriverManager.getConnection(DATABASE);
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, mentionID);
            ps.setString(3, videoTweetID);
            ps.setString(4, videoURL);
            ps.executeUpdate();
            connection.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static String getMention(String mentionID) {
        String mention = "";
        try {
            connection = DriverManager.getConnection(DATABASE);
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM TweetRecords WHERE MentionID =" + mentionID + ";");

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                mention += rs.getString("MentionID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mention;
    }

    public static String lastSearchID() {
        String sinceID = "";
        try {
            connection = DriverManager.getConnection(DATABASE);
            PreparedStatement ps = connection.prepareStatement("SELECT MentionID FROM TweetRecords ORDER BY ID DESC LIMIT 1;");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                sinceID += rs.getString("MentionID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sinceID;
    }
 /*   private static Map<String, String> findMentions() {

        Map<String,String> statusID = new HashMap<>();
        Query query = new Query(botHandle + "-filter:retweets");
        query.count(100);
        query.setSince(DBHelper.lastSearchID());

        try {

            Twitter twitterAuth = new TwitterFactory(OAuth2Token()).getInstance();
            twitterAuth.getOAuth2Token();

            QueryResult searchResult;
            do {
                searchResult = twitterAuth.search(query);

                for (Status tweet : searchResult.getTweets()) {

                    String tweetReferenced = String.valueOf(tweet.getInReplyToStatusId());
                    ResponseList<Status> statuses = twitter.lookup(Long.parseLong(tweetReferenced));

                    for (Status mediaFilter : statuses) {
                        MediaEntity[] mediaEntities = mediaFilter.getMediaEntities();

                        for (int i = 0; i < mediaEntities.length; i++) {
                            MediaEntity.Variant[] variants = mediaEntities[i].getVideoVariants();

                            for (int j = 0; j < variants.length; j++) {
                                if ((!tweetReferenced.equals("-1")) && variants[j].getContentType().equals("video/mp4")) {
                                    String tweetID = String.valueOf(tweet.getId());
                                    statusID.put(tweetReferenced, tweetID);
                                }
                            }
                        }
                    }
                }
            }
            while ((query = searchResult.nextQuery()) != null) ;

        } catch (TwitterException e) {
            e.printStackTrace();
        }

        return statusID;
    }*/


}