import utils.DBHelper;
import twitter4j.*;

import java.util.*;

public class Main {

    private static Twitter twitter = TwitterFactory.getSingleton();
    private static int sleepTime                = 240000; // 4 minutes in milliseconds

    public static void main(String[] args) {

    while (true) {
         for (String mentionID : findMentions().keySet()){
             String tweetWithMedia = mentionID;
             String mentionTweet = findMentions().get(mentionID);


             try {

                 String user = twitter.showStatus(Long.parseLong(mentionTweet)).getUser().getScreenName();

                if (DBHelper.getMention(mentionTweet).equals(mentionTweet)) {
                    System.out.println("Record already exist.");
                 }
                 else {
                    System.out.println("Mention: " + mentionID + " - TweetWithMedia: " + tweetWithMedia);
                    replyTweet(tweetMessage(user, mentionID), mentionTweet);
                    DBHelper.saveTweet(user, mentionTweet, tweetWithMedia, fetchVideo(tweetWithMedia));
                }

             } catch (Exception e) {
                 e.printStackTrace();
             }
         }

         try {
             System.out.println("Application is Sleeping for 4 minutes.");
             Thread.sleep(sleepTime);
         } catch (Exception e) {
             System.out.println(e.getMessage());
         }
     }
    }


    private static void replyTweet(String text, String id) {
        try {
            Status status = twitter.updateStatus(
                    new StatusUpdate(text)
                            .inReplyToStatusId(Long.parseLong(id)));

            System.out.println("Replied to: " + status.getInReplyToScreenName());

        } catch (TwitterException e) {
              e.printStackTrace();
        }
    }

    private static String fetchVideo(String statusID) {
        String videoURl = "";
        try{
            Status status = twitter.showStatus(Long.parseLong(statusID));

            for (MediaEntity media : status.getMediaEntities()) {
                MediaEntity.Variant[] variants = media.getVideoVariants();

                List<Integer> bitrate = new ArrayList<>();

                for (int i =0; i < variants.length;i++) {
                    if (variants[i].getContentType().equals("video/mp4")) {
                       if (variants[i].getBitrate() == maxVariant(bitrate, variants[i].getBitrate())) {
                           videoURl = variants[i].getUrl();
                       }
                    }
                }
            }
        } catch (TwitterException e) {
            e.printStackTrace();
        }
        return videoURl;
    }

    private static int maxVariant(List<Integer> bitrate, int variant){
        bitrate.add(variant);
        Collections.sort(bitrate);
        return bitrate.get(bitrate.size()-1);
    }

    private static Map<String, String> findMentions() {

        Map<String,String> statusID = new HashMap<>();
        Paging paging = new Paging();
        paging.count(200);
        paging.setSinceId(Long.parseLong(DBHelper.lastSearchID()));
        try {
            User user = twitter.verifyCredentials();
            List<Status> mentionList = twitter.getMentionsTimeline(paging);

                for (Status tweet : mentionList) {

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

        } catch (TwitterException e) {
            e.printStackTrace();
        }

        return statusID;
    }

    private static String tweetMessage(String username, String tweetID){
        String userMention = "@" + username;
        String[] messages = {"Yes! video, it's here: ", "Alright, i got this: ", "Video? Here we go: ", "Yes! video is ready: ", "Yes, Video! At your service: "};
        int rand = (int)(messages.length * Math.random());

        return userMention + " " + messages[rand] + fetchVideo(tweetID);
    }

}