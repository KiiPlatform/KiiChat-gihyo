package com.kii.sample.chat.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.net.Uri;
import android.text.TextUtils;

import com.kii.cloud.storage.KiiBucket;
import com.kii.cloud.storage.KiiGroup;
import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.query.KiiQuery;
import com.kii.sample.chat.ui.util.Logger;

/**
 * �`���b�g���[����\���܂��B
 * �`���b�g���[����KiiObject�Ƃ��ĕۑ������̂ł͂Ȃ��AKiiGroup��chat_room�Ƃ������O�ō쐬�����o�P�c�ŕ\������܂��B
 * ���[�U���`���b�g���J�n����ƁA�����ƃ`���b�g�F�B��������KiiGroup���쐬����܂��B
 * ����ɂ��̃O���[�v�X�R�[�v�̃o�P�c�Ƃ���chat_room���쐬����A�����Ƀ��b�Z�[�W��ۑ����܂��B
 * �`���b�g�����o�[�͂���chat_room�o�P�c���Ď����Ă��邽�߁A�N����chat_room�o�P�c�Ƀ��b�Z�[�W��ۑ�����ƁA�`���b�g�����o�[�ɒʒm����܂��B
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public class ChatRoom {
	
	private static final String BUCKET_NAME = "chat_room";
	public static final String CAHT_GROUP_URI = "chat_group_uri";
	
	public static KiiBucket getBucket(KiiGroup kiiGroup) {
		return kiiGroup.bucket(BUCKET_NAME);
	}
	/**
	 * �`���b�g���[���̖��O���擾���܂��B
	 * ���O�̓`���b�g�����o�[�̖��O���J���}��؂�ŘA������������ł��B
	 * 
	 * @param user
	 * @param chatFriend
	 * @return
	 * @throws Exception
	 */
	public static String getChatRoomName(KiiUser user, ChatFriend chatFriend) throws Exception {
		ChatUser me = ChatUser.findByUri(user.toUri());
		List<String> members = new ArrayList<String>();
		members.add(me.getUsername());
		members.add(chatFriend.getUsername());
		Collections.sort(members);
		return TextUtils.join(",", members);
	}
	/**
	 * �`���b�g���[������ӂɎ��ʂ���L�[�𐶐����܂��B
	 * �L�[�̓`���b�g�����o�[�S����URI��"_"�ŘA������������ł��B
	 * ���̃L�[���r���邱�ƂŁA���ɑ��݂���`���b�g���ǂ����𔻒�ł��܂��B
	 * 
	 * @param kiiGroup
	 * @return
	 */
	public static String getUniqueKey(KiiGroup kiiGroup) {
		try {
			kiiGroup.refresh();
			List<KiiUser> members = kiiGroup.listMembers();
			return getUniqueKey(members);
		} catch (Exception e) {
			return null;
		}
	}
	public static String getUniqueKey(KiiUser user, ChatFriend friend) {
		List<KiiUser> members = new ArrayList<KiiUser>();
		members.add(user);
		members.add(KiiUser.createByUri(Uri.parse(friend.getUri())));
		return getUniqueKey(members);
	}
	public static String getUniqueKey(List<KiiUser> members) {
		List<String> memberUri = new ArrayList<String>();
		for (KiiUser member : members) {
			memberUri.add(member.toUri().toString());
		}
		Collections.sort(memberUri);
		return TextUtils.join("_", memberUri);
	}
	
	private final KiiBucket kiiBucket;
	
	public ChatRoom(KiiGroup kiiGroup) {
		this.kiiBucket = getBucket(kiiGroup);
	}
	
	/**
	 * �`���b�g���[�����̑S�Ẵ��b�Z�[�W���擾���܂��B
	 * 
	 * @return�@�����Ƀ\�[�g���ꂽ���b�Z�[�W���X�g
	 */
	public List<ChatMessage> getMessageList() {
		return this.queryMessageList(ChatMessage.createQuery());
	}
	/**
	 * �w�肵�������ȍ~�ɍ쐬���ꂽ�`���b�g���[�����̃��b�Z�[�W���擾���܂��B
	 * 
	 * @param modifiedSinceTime
	 * @return �����Ƀ\�[�g���ꂽ���b�Z�[�W���X�g
	 */
	public List<ChatMessage> getMessageList(long modifiedSinceTime) {
		return this.queryMessageList(ChatMessage.createQuery(modifiedSinceTime));
	}
	private List<ChatMessage> queryMessageList(KiiQuery query) {
		List<ChatMessage> messages = new ArrayList<ChatMessage>();
		try {
			List<KiiObject> results = this.kiiBucket.query(query).getResult();
			for (KiiObject o : results) {
				messages.add(new ChatMessage(o));
			}
		} catch (Exception e) {
			Logger.e("Unable to list messages", e);
		}
		return messages;
	}
}
