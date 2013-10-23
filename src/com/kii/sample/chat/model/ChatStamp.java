package com.kii.sample.chat.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.kii.cloud.storage.Kii;
import com.kii.cloud.storage.KiiBucket;
import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.query.KiiQuery;
import com.kii.cloud.storage.resumabletransfer.KiiDownloader;
import com.kii.cloud.storage.resumabletransfer.KiiUploader;
import com.kii.sample.chat.KiiChatApplication;
import com.kii.sample.chat.util.Logger;
import com.kii.sample.chat.util.StampCacheUtils;

/**
 * チャットのメッセージで使用できるスタンプを表します。
 * ユーザは画像ファイルをアプロードしてスタンプとして使用できます。
 * アプリケーションスコープのデータとしてKiiCloudに保存されるので、他のユーザによってアップロードされたスタンプは誰でも利用することが可能です。
 * メッセージとしてのスタンプは通常のチャットメッセージと同じようにchat_roomバケットに保存されます。
 * その際、'$STAMP:{画像のKiiObjectのURI}'という形式のテキストとして保存します。
 * KiiChatアプリケーションは '$STAMP:' から始まるメッセージを受信した場合、それがスタンプであると判断し画像を表示します。
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public class ChatStamp extends KiiObjectWrapper {
	
	private static final String BUCKET_NAME = "chat_stamps";
	
	public static KiiBucket getBucket() {
		return Kii.bucket(BUCKET_NAME);
	}
	
	/**
	 * ユーザにアップロードされた全てのスタンプを取得します。
	 * スタンプ本体の画像イメージは取得されません。
	 * 
	 * @return
	 * @throws Exception
	 */
	public static List<ChatStamp> list() {
		List<ChatStamp> stamps = new ArrayList<ChatStamp>();
		try {
			KiiQuery query = new KiiQuery();
			query.sortByAsc(FIELD_CREATED);
			List<KiiObject> objects = getBucket().query(query).getResult();
			for (KiiObject object : objects) {
				stamps.add(new ChatStamp(object));
			}
			return stamps;
		} catch (Exception e) {
			Logger.e("Unable to list stamps", e);
			return stamps;
		}
	}
	
	private File imageFile;
	private String uri;
	
	public ChatStamp(File imageFile) {
		super(getBucket().object());
		this.imageFile = imageFile;
	}
	public ChatStamp(KiiObject kiiObject) {
		super(kiiObject);
		this.uri = kiiObject.toUri().toString();
	}
	public ChatStamp(ChatMessage message) {
		super(KiiObject.createByUri(Uri.parse(message.getStampUri())));
		this.uri = message.getStampUri();
	}
	/**
	 * @throws Exception
	 */
	public void save() throws Exception {
		this.kiiObject.save();
		if (this.imageFile != null) {
			// FIXME:save後にIDが設定されるかどうか？
			this.uri = this.kiiObject.toUri().toString();
			KiiUploader uploader = this.kiiObject.uploader(KiiChatApplication.getContext(), this.imageFile);
			uploader.transfer(null);
			// アップロードしたファイルを、KiiObjectのURIに応じた名前にリネームする
			File cacheFile = StampCacheUtils.getCacheFile(this.kiiObject.toUri().toString());
			this.imageFile.renameTo(cacheFile);
		}
	}
	public String getUri() {
		return this.uri;
	}
	/**
	 * スタンプの画像を取得します。
	 * ディスクに画像がキャッシュされている場合は、KiiCloudにアクセスすることなく画像を返します。
	 * 画像がキャッシュにない場合、KiiCloudとの通信が発生するので、メインスレッドでは実行しないでください。
	 * 
	 * @return
	 */
	public Bitmap getImage() {
		try {
			byte[] image = null;
			if (this.imageFile != null) {
				image = readImageFromLocal(this.imageFile);
			} else if (this.uri != null) {
				// イメージがキャッシュされていれば、キャッシュから読み込む
				File cacheFile = StampCacheUtils.getCacheFile(this.uri);
				if (cacheFile.exists()) {
					image = readImageFromLocal(cacheFile);
				} else {
					// キャッシュに存在しない場合は、KiiCloudからダウンロードする
					kiiObject.refresh();
					KiiDownloader downloader = kiiObject.downloader(KiiChatApplication.getContext(), cacheFile);
					downloader.transfer(null);
					image = readImageFromLocal(cacheFile);
				}
			}
			if (image != null) {
				return BitmapFactory.decodeByteArray(image, 0, image.length);
			}
			return null;
		} catch (Exception e) {
			Logger.e("failed to download stamp", e);
			return null;
		}
	}
	/**
	 * ローカルのファイルを読み込みます。
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private byte[] readImageFromLocal(File file) throws IOException {
		FileInputStream fs = new FileInputStream(file);
		try {
			return IOUtils.toByteArray(fs);
		} finally {
			IOUtils.closeQuietly(fs);
		}
	}
}
