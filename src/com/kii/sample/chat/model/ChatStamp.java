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
 * もしユーザが'$STAMP:'から始まるテキストメッセージを送信しようとすると、うまくテキストを送信することはできません。
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
			// 作成日時でソートして、クエリ結果の順序を保証する
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
	
	/**
	 * ローカルファイルからインスタンスを生成します。
	 * このコンストラクタは新規スタンプの追加時に作成されます。
	 * 
	 * @param imageFile
	 */
	public ChatStamp(File imageFile) {
		super(getBucket().object());
		this.imageFile = imageFile;
	}
	/**
	 * スタンプを表すKiiObjectからインスタンスを生成します。
	 * 
	 * @param kiiObject
	 */
	public ChatStamp(KiiObject kiiObject) {
		super(kiiObject);
		this.uri = kiiObject.toUri().toString();
	}
	/**
	 * ChatMessageからインスタンスを生成します。
	 * 渡されるChatMessageはスタンプはスタンプを表すChatMessageである必要があります。(isStamp()がtrueのもの)
	 * 
	 * @param message
	 */
	public ChatStamp(ChatMessage message) {
		super(KiiObject.createByUri(Uri.parse(message.getStampUri())));
		this.uri = message.getStampUri();
	}
	/**
	 * スタンプをKiiCloudに保存し、画像をアップロードします。
	 * 
	 * @throws Exception
	 */
	public void save() throws Exception {
		this.kiiObject.save();
		if (this.imageFile != null) {
			this.uri = this.kiiObject.toUri().toString();
			KiiUploader uploader = this.kiiObject.uploader(KiiChatApplication.getContext(), this.imageFile);
			uploader.transfer(null);
			// アップロードしたファイルを、KiiObjectのURIに応じた名前にリネームする
			File cacheFile = StampCacheUtils.getCacheFile(this.kiiObject.toUri().toString());
			this.imageFile.renameTo(cacheFile);
		}
	}
	/**
	 * このスタンプを表すKiiObjectのURIを取得します。
	 * 
	 * @return
	 */
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
				// ファイルを指定してChatStampのインスタンスが生成された場合 (新規スタンプの追加時)
				image = readImageFromLocal(this.imageFile);
			} else if (this.uri != null) {
				// イメージがキャッシュされていれば、キャッシュから読み込む
				File cacheFile = StampCacheUtils.getCacheFile(this.uri);
				if (cacheFile.exists()) {
					image = readImageFromLocal(cacheFile);
				} else {
					// キャッシュに存在しない場合は、KiiCloudからダウンロードする
					Logger.i("downloads stamp image from KiiCloud");
					KiiDownloader downloader = this.kiiObject.downloader(KiiChatApplication.getContext(), cacheFile);
					downloader.transfer(null);
					image = readImageFromLocal(cacheFile);
				}
			}
			if (image != null) {
				return BitmapFactory.decodeByteArray(image, 0, image.length);
			}
			Logger.w("failed to download stamp");
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
