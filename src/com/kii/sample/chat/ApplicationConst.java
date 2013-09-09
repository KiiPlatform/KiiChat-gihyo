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

	/**
	 * �S�Ẵ`���b�g���[�U�������p�ɕێ����Ă���TOPIC�̖��O�ł��B
	 * ���̃g�s�b�N�̓��[�U���ʂɎ����[���{�b�N�X�Ɏ��Ă��܂��B
	 * ���̃��[�U�����̃g�s�b�N�Ƀ��b�Z�[�W�𑗐M����ƁA���[�U�Ƀv�b�V���ʒm�����M����܂��B
	 * ��̓I�ɂ́A�`���b�g���J�n�������ɁA�`���b�g���J�n���ꂽ���Ƃ𑊎�ɓ`����ׂɎg�p���܂��B
	 * ���̒ʒm���󂯂�����́A�`���b�g�p�ɍ쐬���ꂽ�O���[�v�X�R�[�v��chat_room�o�P�c���w�ǂ��ĊĎ�����悤�ɂ��܂��B
	 * 
	 * @see http://documentation.kii.com/ja/guides/android/managing-push-notification/push-to-user/
	 */
	public static final String TOPIC_INVITE_NOTIFICATION = "invite_notify";
	/**
	 * �`���b�g���J�n���ꂽ���Ƃ�\���A�N�V�����ł��B
	 */
	public static final String ACTION_CHAT_STARTED = "com.kii.sample.chat.ACTION_CHAT_STARTED";
	/**
	 * ���b�Z�[�W����M�������Ƃ�\�������̂ł��B
	 */
	public static final String ACTION_MESSAGE_RECEIVED = "com.kii.sample.chat.ACTION_MESSAGE_RECEIVED";
	/**
	 * BroadcastReceiver����M����Push�ʒm��Activity�ɑ��鎞�Ɏg�p����L�[�ł��B
	 */
	public static final String EXTRA_MESSAGE = "com.kii.sample.chat.EXTRA_MESSAGE";
	
	private ApplicationConst() {
	}
}
