package com.fartans.localme.Firebase;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.fartans.localme.Chat.ChatActivity;
import com.fartans.localme.DBHandlers.ChatIdMapDBHandler;
import com.fartans.localme.DBHandlers.ChatInfoDBHandler;
import com.fartans.localme.MainActivity;
import com.fartans.localme.R;
import com.fartans.localme.TempDataClass;
import com.fartans.localme.models.ChatIdMap;
import com.fartans.localme.models.ChatInfo;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MyFirebaseMessagingService extends FirebaseMessagingService {


    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    String message;
    String type;
    String requestId;
    String username;
    String askedby;
    String repliedby;
    String replymessage;
    String userid_questions;
    String category;
    String questionmessage;
    String asked_time_questions;
    String imagepath;
    String questionid;
    String userprofession_questions;
    String userId;
    int intType;
    String responseId;

    String responseUserProfession;
    String responseUserProfilePath;

    String chatSenderId;
    String chatChatId;
    String chatSenderName;
    String chatMessage;
    String chatMessageTime;

    String verifyUserId;


    private static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //Displaying data in log
        //It is optional

        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());


        type = remoteMessage.getData().get("Type");
        intType = Integer.parseInt(type);
        switch (intType) {
            case 1:
                verifyUserId = remoteMessage.getData().get("UserId");
                break;
            case 2:

                askedby = remoteMessage.getData().get("AskedBy");
                category = remoteMessage.getData().get("Category");
                repliedby = remoteMessage.getData().get("RepliedBy");
                replymessage = remoteMessage.getData().get("ReplyMessage");
                userid_questions = remoteMessage.getData().get("UserId");
                questionmessage = remoteMessage.getData().get("QuestionMessage");
                asked_time_questions = remoteMessage.getData().get("AskedTime");
                imagepath = remoteMessage.getData().get("UserProfilePhotoServerPath");
                questionid = remoteMessage.getData().get("QuestionId");
                userprofession_questions = remoteMessage.getData().get("UserProfession");
                break;
            case 3: //New Request Notification
                message = remoteMessage.getData().get("message");
                requestId = remoteMessage.getData().get("requestId");
                username = remoteMessage.getData().get("userName");
                break;
            case 4://New Response Notification
                message = remoteMessage.getData().get("ResponseMessage");
                username = remoteMessage.getData().get("ResponseUserName");
                requestId = remoteMessage.getData().get("RequestId");
                userId = remoteMessage.getData().get("ResponseUserId");
                responseId = remoteMessage.getData().get("ResponseId");
                responseUserProfession = remoteMessage.getData().get("Profession");
                responseUserProfilePath = remoteMessage.getData().get("ProfilePhotoUrl");
                break;
            case 5:
                chatChatId = remoteMessage.getData().get("ChatId");
                chatMessage = remoteMessage.getData().get("Message");
                chatMessageTime = remoteMessage.getData().get("SentOn");
                chatSenderId = remoteMessage.getData().get("SenderId");
                chatSenderName = remoteMessage.getData().get("UserName");
                break;
        }


        //Calling method to generate notification
        sendNotification(remoteMessage.getNotification().getBody());

    }

    //This method is only generating push notification
    //It is same as we did in earlier posts
    private void sendNotification(String messageBody) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logo_notif);


        if (intType == 2) {
            Intent question = new Intent(this, MainActivity.class);
            question.putExtra("type", "Replies");
            question.putExtra("askedby", askedby);
            question.putExtra("userid_questions", userid_questions);
            question.putExtra("questionmessage", questionmessage);
            question.putExtra("asked_time_questions", asked_time_questions);
            question.putExtra("imagepath", imagepath);
            question.putExtra("Category", category);
            question.putExtra("questionid", questionid);
            question.putExtra("userprofession_questions", userprofession_questions);

            PendingIntent contentIntent = PendingIntent.getActivity(this, Integer.parseInt(questionid),
                    question, PendingIntent.FLAG_ONE_SHOT);


            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.logo_notif)
                            .setLargeIcon(bitmap)
                            .setContentTitle("New Answer")
                            .setStyle(new NotificationCompat.BigTextStyle()
                                    .bigText(repliedby + " says " + replymessage))
                            .setContentText(repliedby + " says " + replymessage)
                            .setAutoCancel(true);

            mBuilder.setContentIntent(contentIntent);
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        } else if (intType == 3) {
            Intent requestIntent = new Intent(this, MainActivity.class);
            requestIntent.putExtra("type", "request");
            requestIntent.putExtra("NotificationRequestId", requestId);

            PendingIntent contentIntent = PendingIntent.getActivity(this, Integer.parseInt(requestId),
                    requestIntent, PendingIntent.FLAG_ONE_SHOT);

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.logo_notif)
                            .setLargeIcon(bitmap)
                            .setContentTitle("New Request")
                            .setStyle(new NotificationCompat.BigTextStyle()
                                    .bigText(username + " : " + message))
                            .setContentText(username + " : " + message)
                            .setAutoCancel(true);

            mBuilder.setContentIntent(contentIntent);
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        } else if (intType == 4) {
            Intent responseIntent = new Intent(this, MainActivity.class);
            responseIntent.putExtra("type", "response");
            responseIntent.putExtra("NotificationResponseId", responseId);
            responseIntent.putExtra("NotificationRequestId", requestId);
            responseIntent.putExtra("NotificationResponseUserId", userId);
            responseIntent.putExtra("NotificationResponseUserName", username);
            responseIntent.putExtra("NotificationResponseMessage", message);
            responseIntent.putExtra("NotificationResponseUserProfession", responseUserProfession);
            responseIntent.putExtra("NotificationResponseUserProfilePhotoServerPath", responseUserProfilePath);

            PendingIntent contentIntent = PendingIntent.getActivity(this, Integer.parseInt(responseId),
                    responseIntent, PendingIntent.FLAG_ONE_SHOT);

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.logo_notif)
                            .setContentTitle("New Response")
                            .setLargeIcon(bitmap)
                            .setStyle(new NotificationCompat.BigTextStyle()
                                    .bigText(username + " : " + message))
                            .setContentText(username + " : " + message)
                            .setAutoCancel(true);

            mBuilder.setContentIntent(contentIntent);
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        } else if (intType == 5) {
            Intent chatIntent = new Intent(getApplicationContext(), ChatActivity.class);
            chatIntent.putExtra("ChatId", chatChatId);
            chatIntent.putExtra("Message", chatMessage);
            chatIntent.putExtra("SentOn", chatMessageTime);
            chatIntent.putExtra("SenderId", chatSenderId);
            chatIntent.putExtra("UserName", chatSenderName);
            chatIntent.putExtra("received", true);
            ChatIdMap chatIdMap = new ChatIdMap();
            chatIdMap.chatId = chatChatId;
            chatIdMap.userId = chatSenderId;
            chatIdMap.userName = chatSenderName;
            ChatIdMapDBHandler.InsertChatIdMap(getApplicationContext(), chatIdMap);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    getApplicationContext(),
                    Integer.parseInt(chatChatId),
                    chatIntent,
                    PendingIntent.FLAG_ONE_SHOT);


            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();
            String time = dateFormat.format(date);
            time = time.substring(11, time.lastIndexOf(':'));
            ChatInfo newMessage = new ChatInfo();
            newMessage.setMessage(chatMessage);
            newMessage.setSentBy(false);
            newMessage.setTimeStamp(time);
            newMessage.setChatId(chatChatId);

            ChatInfoDBHandler.InsertChatInfo(getApplicationContext(), newMessage);

            if (TempDataClass.alreadyAdded == true) {
                TempDataClass.alreadyAdded = false;
                return;
            }

            Notification mBuilder =
                    new NotificationCompat.Builder(getApplicationContext())
                            .setSmallIcon(R.drawable.logo_notif)
                            .setLargeIcon(bitmap)
                            .setContentTitle("Chat Notification")
                            .setContentIntent(pendingIntent)
                            .setStyle(new NotificationCompat.BigTextStyle()
                                    .bigText(chatSenderName + ":" + chatMessage))
                            .setContentText(chatSenderName + ":" + chatMessage)
                            .setAutoCancel(true)
                            .build();


            mNotificationManager =
                    (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder);
        } else {

        }
    }

}
