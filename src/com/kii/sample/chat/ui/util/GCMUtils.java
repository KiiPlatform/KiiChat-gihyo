package com.kii.sample.chat.ui.util;

import java.io.IOException;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.kii.sample.chat.ApplicationConst;
import com.kii.sample.chat.KiiChatApplication;

/**
 * 
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public class GCMUtils {
	private GCMUtils() {
	}
	public static String register() throws Exception {
		String registrationId = null;
		int retry = 0;
		while (retry < 3) {
			try {
				GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(KiiChatApplication.getContext());
				registrationId = gcm.register(ApplicationConst.SENDER_ID);
				break;
			} catch (IOException ignore) {
				// java.io.IOException: SERVICE_NOT_AVAILABLE‚ª‚½‚Ü‚É”­¶‚·‚é‚Ì‚ÅƒŠƒgƒ‰ƒC‚·‚é
				Thread.sleep(1000);
				retry++;
				Logger.w("failed to register GCM. retry " + retry + " times  reason=" + ignore.getMessage());
			}
		}
		if (registrationId == null || retry >= 3) {
			throw new IOException("failed to register GCM");
		}
		return registrationId;
	}
}
