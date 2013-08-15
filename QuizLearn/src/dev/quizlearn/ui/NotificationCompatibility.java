package dev.quizlearn.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

public class NotificationCompatibility {
	public static QuizListSpinner def = null;

	public static void makeNotification(CharSequence cs) {
		if (Build.VERSION.SDK_INT > 11) {
			if (Build.VERSION.SDK_INT > 16) {
				makeNotificationv16(cs);
			} else {
				makeNotificationv11(cs);
			}
		} else {
			makeNotificationv1(cs);
		}
	}

	@SuppressWarnings("deprecation")
	private static void makeNotificationv1(CharSequence cs) {
		// Build notification
		Notification noti = new Notification(R.drawable.ic_launcher, cs, System.currentTimeMillis());
		NotificationManager notificationManager = (NotificationManager) def.getSystemService(Activity.NOTIFICATION_SERVICE);
		// Hide the notification after its selected
		noti.flags |= Notification.FLAG_AUTO_CANCEL;
		notificationManager.notify(0, noti);
	}

	@SuppressWarnings("deprecation")
	@TargetApi(11)
	private static void makeNotificationv11(CharSequence cs) {
		// Prepare intent which is triggered if the
		// notification is selected
		Intent intent = new Intent(def, QuizListSpinner.class);
		PendingIntent pIntent = PendingIntent.getActivity(def, 0, intent, 0);
		// Build notification
		// Actions are just fake
		Notification.Builder nb = new Notification.Builder(def);
		nb = nb.setContentTitle(cs);
		nb = nb.setContentText(cs);
		nb = nb.setSmallIcon(R.drawable.ic_launcher);
		nb = nb.setContentIntent(pIntent);
		// nb = nb.addAction(R.drawable.ic_launcher, "Call", pIntent);
		// nb = nb.addAction(R.drawable.ic_launcher, "More", pIntent);
		// nb = nb.addAction(R.drawable.ic_launcher, "And more", pIntent);
		Notification noti = nb.getNotification();
		NotificationManager notificationManager = (NotificationManager) def.getSystemService(Activity.NOTIFICATION_SERVICE);
		// Hide the notification after its selected
		noti.flags |= Notification.FLAG_AUTO_CANCEL;
		notificationManager.notify(0, noti);
	}

	@TargetApi(16)
	private static void makeNotificationv16(CharSequence cs) {
		// Prepare intent which is triggered if the
		// notification is selected
		Intent intent = new Intent(def, QuizListSpinner.class);
		PendingIntent pIntent = PendingIntent.getActivity(def, 0, intent, 0);
		// Build notification
		// Actions are just fake
		Notification.Builder nb = new Notification.Builder(def);
		nb = nb.setContentTitle(cs);
		nb = nb.setContentText(cs);
		nb = nb.setSmallIcon(R.drawable.ic_launcher);
		nb = nb.setContentIntent(pIntent);
		nb = nb.addAction(R.drawable.ic_launcher, "Call", pIntent);
		nb = nb.addAction(R.drawable.ic_launcher, "More", pIntent);
		nb = nb.addAction(R.drawable.ic_launcher, "And more", pIntent);
		Notification noti = nb.build();
		NotificationManager notificationManager = (NotificationManager) def.getSystemService(Activity.NOTIFICATION_SERVICE);
		// Hide the notification after its selected
		noti.flags |= Notification.FLAG_AUTO_CANCEL;
		notificationManager.notify(0, noti);
	}

}
