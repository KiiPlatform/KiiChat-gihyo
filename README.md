#KiiChat-gihyo

[【gihyo.jp】MBaaS徹底入門 -- Kii Cloudでスマホアプリ開発](http://gihyo.jp/dev/serial/01/mbaas)の連載で作成したチャットアプリのソースコードです。  
実際に動作させる為には[KiiCloud](https://developer.kii.com/?locale=jp)への登録(無料)が必要になります。


Eclipseにプロジェクトをインポートしてビルドする場合、Android Support Library v7が必要です。  
以下のプロジェクトをEclipseのワークスペースにインポートしてください。

    {SDK-DIR}/android-sdk-macosx/extras/android/support/v7/appcompat


#TODO

1. ログインしていない状態でPushメッセージを受信した時の処理
2. チャット一覧の表示で、最新のメッセージを表示する
3. グループチャットの実装
4. 絵文字(顔文字)顔文字の実装
5. チャットの削除（ユーザ毎に履歴を管理？）
6. 新規にチャットを始めた時の、相手側の振る舞い

SENDER_ID=1012419078893  
API_KEY=AIzaSyBhhtpr1qyH4HLUDFEsOusuavkuiY4i5y4