package com.kii.sample.chat;

/**
 * KiiChat�A�v���P�[�V�������ʂŎg�p����萔���Ǘ����܂��B
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public final class ApplicationConst {
	
	// TODO:APP_ID��APP_KEY��KiiCloud�ŃA�v���P�[�V�������쐬�����ۂɔ��s���ꂽ�l�ɏ��������Ă��������B
	public static final String APP_ID = "5db741d4";
	public static final String APP_KEY = "7b9a135db251fc849b21237d3ccc3ab9";
	// TODO:SENDER_ID��GCM�̐ݒ��L���ɂ����ۂɔ��s���ꂽ�l�ɏ��������Ă��������B
	public static final String SENDER_ID = "1012419078893";

	public static final String TOPIC_INVITE_NOTIFICATION = "InviteNotification";
	public static final String ACTION_CHAT_STARTED = "com.kii.sample.chat.ACTION_CHAT_STARTED";
	public static final String ACTION_MESSAGE_RECEIVED = "com.kii.sample.chat.ACTION_MESSAGE_RECEIVED";
	public static final String EXTRA_MESSAGE = "com.kii.sample.chat.EXTRA_MESSAGE";
	
	private ApplicationConst() {
	}
}
