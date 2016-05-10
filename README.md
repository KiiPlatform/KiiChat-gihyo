#はじめに

[【gihyo.jp】MBaaS徹底入門 -- Kii Cloudでスマホアプリ開発](http://gihyo.jp/dev/serial/01/mbaas)の連載で作成したチャットアプリのソースコードです。  
今までSkypeやLINEのようなインスタントメッセンジャーアプリの作成にはサーバサイドの開発が必須であり、個人レベルの開発者が全てを実装するのには、とても高い障壁がありました。  
MBaaSを利用することにより、このようなアプリケーションをサーバサイドの開発無しに、作成することが可能になっています。  
このソースコードで簡単な[KiiCloud](https://developer.kii.com/?locale=jp)の使い方を学ぶことができます。  
アプリケーションを実際に動作させる為には[KiiCloud](https://developer.kii.com/?locale=jp)への登録**(無料)**が必要になります。

#KiiCloudについて

[KiiCloud](https://developer.kii.com/?locale=jp)は[Kii株式会社](http://jp.kii.com/)が提供しているMBaaS(Mobile Backend as a Service)です。  
主にモバイルアプリケーション向けにユーザ管理、データ管理、アクセス制御、プッシュ通知、データ分析などの様々な機能を提供しています。  
[KiiCloud](https://developer.kii.com/?locale=jp)を利用することにより、サーバサイドの開発無しにリッチなモバイルアプリケーションを開発することが可能になります。  
[KiiCloud](https://developer.kii.com/?locale=jp)は**無料**で始められますので、是非、MBaaSの力を体験してみて下さい。


#開発環境

[【gihyo.jp】MBaaS徹底入門 -- Kii Cloudでスマホアプリ開発](http://gihyo.jp/dev/serial/01/mbaas)の連載時、Androidアプリの開発にはEclipseを使用するのが一般的でしたので、記事もEclipseを使った説明になっています。  
現在では[AndroidStudio](http://developer.android.com/intl/ja/sdk/index.html)を使って開発をするのがデファクト・スタンダードになっているため、プロジェクトはAndroidStudio形式になっています。古いEclipseのプロジェクトを参照したい場合は、[LatestEclipseProject](https://github.com/KiiPlatform/KiiChat-gihyo/releases/tag/LatestEclipseProject)をチェックアウトしてください。  
プッシュ通知を実装するために、GCM (Google Cloud Messaging)を使用していますが、この機能を使用するためにGoogleのアカウントが必要になります。  
エミュレータを使って動作確認する場合は、ターゲットを以下のようにGoogle APIsに設定してください。エミュレータ起動後に「設定->アカウント」からGoogleアカウントの設定を行ってください。  

#サポート

本ソースコードおよび、KiiCloudについてのご質問は[コミュニティサイト](http://community-jp.kii.com/)にてお願い致します。


#リソース
サンプルアプリでは以下のリソースを使用しています。各ライセンスについてはリンク先を参照してください。
###アプリアイコン
[Free 3D Social Icons](https://www.iconfinder.com/icons/54521/about_balloon_baloon_bubble_chat_comment_comments_forum_help_hint_knob_mandarin_mandarine_orange_pin_snap_speech_tack_talk_tangerine_icon) - [By Aha-Soft](http://www.aha-soft.com/)  
[Creative Commons (Attribution-Share Alike 3.0 Unported)](http://creativecommons.org/licenses/by-sa/3.0/)  

###その他のアイコン
https://github.com/Templarian/MaterialDesign  
  
###チャットの吹き出し画像
http://www.codeproject.com/Tips/897826/Designing-Android-Chat-Bubble-Chat-UI  

#スクリーンショット

<table border="0">
  <tr>
    <td><img src="screenshots/01.png"></td>
    <td><img src="screenshots/02.png"></td>
  </tr>
  <tr>
    <td><img src="screenshots/03.png"></td>
    <td><img src="screenshots/04.png"></td>
  </tr>
</talbe>


