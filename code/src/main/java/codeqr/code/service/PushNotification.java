package codeqr.code.service;

import org.springframework.stereotype.Service;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

@Service
public class PushNotification {

    public void sendNotificationToUser(String fcmToken, String title, String body, String notifId, String userId) throws FirebaseMessagingException {

        Message message = Message.builder()
                .setToken(fcmToken)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putData("notifId", notifId)
                .putData("userId", userId)
                .putData("actions", "[{\"title\":\"Marquer lu\",\"action\":\"mark_read\"}]")
                .setAndroidConfig(AndroidConfig.builder()
                        .setPriority(AndroidConfig.Priority.HIGH) // priorité haute
                        .setNotification(AndroidNotification.builder()
                                .setClickAction("FCM_PLUGIN_ACTIVITY")
                                .setChannelId("default") // Assurez-vous que le channel existe sur Android
                                .build())
                        .build())
                .build();

        String response = FirebaseMessaging.getInstance().send(message);
        System.out.println("Notification envoyée avec succès : " + response);
    }
}
