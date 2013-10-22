package com.kii.sample.chat.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import org.apache.commons.io.IOUtils;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

import com.kii.cloud.storage.KiiGroup;
import com.kii.cloud.storage.KiiObject;
import com.kii.cloud.storage.resumabletransfer.KiiDownloader;
import com.kii.cloud.storage.resumabletransfer.KiiUploader;
import com.kii.sample.chat.KiiChatApplication;

/**
 * 
 * 
 * @author noriyoshi.fukuzaki@kii.com
 */
public class ChatStamp extends ChatMessage {
	
	static final String PREFIX_STAMP = "$STAMP:";
	
	private File imageFile;
	private String imageId;
	
//	public ChatStamp(KiiGroup kiiGroup) {
//		super(kiiGroup);
//	}
	public ChatStamp(KiiObject message) {
		super(message);
	}
	public ChatStamp(KiiGroup kiiGroup, File imageFile) {
		super(kiiGroup);
		this.imageFile = imageFile;
	}
	public ChatStamp(KiiGroup kiiGroup, String imageId) {
		super(kiiGroup);
		this.imageId = imageId;
	}
	@Override
	public boolean isStamp() {
		return true;
	}
	public void save() throws Exception {
		// FIXME:save後にIDが設定されるかどうか？
		this.kiiObject.save();
		if (this.imageFile != null) {
			KiiUploader uploader = this.kiiObject.uploader(KiiChatApplication.getContext(), this.imageFile);
			uploader.transfer(null);
			// アップロードしたスタンプをキャッシュディレクトリにコピーする
			FileChannel source = null; 
			FileChannel dest = null;
			try {
				source = new FileInputStream(this.imageFile).getChannel();
				dest = new FileInputStream(getCacheFile(this.kiiObject.toUri().toString())).getChannel();
				dest.transferFrom(source, 0, source.size());
			} finally {
				IOUtils.closeQuietly(source);
				IOUtils.closeQuietly(dest);
			}
		}
	}
	/**
	 * スタンプのイメージをバイト配列で取得します。
	 * このメソッドはKiiCloudにアクセスする可能性があるのでメインスレッドでは実行しないでください。
	 * 
	 * @return
	 */
	public byte[] getImage() {
		try {
			if (this.imageFile != null) {
				return readImageFromLocal(this.imageFile);
			} else if (this.imageId != null) {
				// イメージがキャッシュされていれば、キャッシュから読み込む
				File cacheFile = getCacheFile(this.imageId);
				if (cacheFile.exists()) {
					return readImageFromLocal(cacheFile);
				}
				// キャッシュに存在しない場合は、KiiCloudからダウンロードする
				KiiDownloader downloader = kiiObject.downloader(KiiChatApplication.getContext(), cacheFile);
				downloader.transfer(null);
				return readImageFromLocal(cacheFile);
			}
			return null;
		} catch (Exception e) {
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
	/**
	 * キャッシュファイルを取得します。
	 * 
	 * @param uri
	 * @return
	 */
	private File getCacheFile(String uri) {
		final File cacheDir = new File(Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED || !isExternalStorageRemovable() ?
				getExternalCacheDir(KiiChatApplication.getContext()).getPath() : KiiChatApplication.getContext().getCacheDir().getPath());
		if (cacheDir.exists()) {
			cacheDir.mkdirs();
		}
		return new File(cacheDir + File.separator + this.escapeUri(uri));
	}
	private String escapeUri(String uri) {
		return uri.replace("://", "_").replace("/", "_");
	}
	private static boolean isExternalStorageRemovable() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			return Environment.isExternalStorageRemovable();
		}
		return true;
	}
	private static File getExternalCacheDir(Context context) {
		if (hasExternalCacheDir()) {
			return context.getExternalCacheDir();
		}
		final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
		return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
	}
	private static boolean hasExternalCacheDir() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
	}
}
