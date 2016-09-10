package com.fartans.localme.Firebase;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.fartans.localme.MainActivity;
import com.fartans.localme.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


public class MyFirebaseMessagingService extends FirebaseMessagingService {


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


/*
        Bundle notification = remoteMessage.getNotification();
        type = remoteMessage.
        intType = Integer.parseInt(type);
        switch (intType) {
            case 1:
                verifyUserId = extras.getString("UserId");
                break;
            case 2:

                askedby = extras.getString("AskedBy");
                category = extras.getString("Category");
                repliedby = extras.getString("RepliedBy");
                replymessage = extras.getString("ReplyMessage");
                userid_questions = extras.getString("UserId");
                questionmessage = extras.getString("QuestionMessage");
                asked_time_questions = extras.getString("AskedTime");
                imagepath = extras.getString("UserProfilePhotoServerPath");
                questionid = extras.getString("QuestionId");
                userprofession_questions = extras.getString("UserProfession");
                break;
            case 3: //New Request Notification
                message = extras.getString("message");
                requestId = extras.getString("requestId");
                username = extras.getString("userName");
                break;
            case 4://New Response Notification
                message = extras.getString("ResponseMessage");
                username = extras.getString("ResponseUserName");
                requestId = extras.getString("RequestId");
                userId = extras.getString("ResponseUserId");
                responseId = extras.getString("ResponseId");
                responseUserProfession = extras.getString("Profession");
                responseUserProfilePath = extras.getString("ProfilePhotoUrl");
                break;
            case 5:
                chatChatId = extras.getString("ChatId");
                chatMessage = extras.getString("Message");
                chatMessageTime = extras.getString("SentOn");
                chatSenderId = extras.getString("SenderId");
                chatSenderName = extras.getString("UserName");
                break;
        }
*/


        //Calling method to generate notification
        sendNotification(remoteMessage.getNotification().getBody());

    }

    //This method is only generating push notification
    //It is same as we did in earlier posts
    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Firebase Push Notification")
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }

}
