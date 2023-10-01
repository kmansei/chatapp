## 動作確認
```
サーバー
./gradlew server:run

クライアント
./gradlew client:run
```

## 実装
- https://github.com/kmansei/chatapp/blob/main/server/src/main/java/chatapp/Server.java
  - サーバー実装
- https://github.com/kmansei/chatapp/blob/main/client/src/main/java/chatapp/Client.java
  - クライアント実装

## 使用ライブラリ, ツール
- Gradle
- Netty
- JUnit

## 設計
### 当初
- クライアントがチャットサーバーと接続されるたびにクライアント側及びサーバー側でスレッド生成
  - クライアント
    - スレッドはサーバーからのメッセージを受信してコンソールに送信
    - メインスレッドはユーザーの入力待ち
  - サーバー
    - スレッドはクライアントからの入力メッセージを待ち受け
    - メインスレッドは新たなクライアントからの接続を待ち受ける

### 改善
- [2044de7](https://github.com/kmansei/chatapp/commit/2044de78b39f4b5c10a367b1a3fbe5cf1befb7bb)
  - クライアントが退室後のリソース解放が正しく出来ていなかったので修正
- [eaaa1f4](https://github.com/kmansei/chatapp/commit/eaaa1f418534bf530525633b01af7d8c64e06b82)
  - ExecutorServiceを使うように修正
- [8e4237d](https://github.com/kmansei/chatapp/commit/8e4237d8a7ad801cfa1122f60c7d8d6000078f00)
  - テストコードの追加、リファクタリング
- [17b0769](https://github.com/kmansei/chatapp/commit/17b07698b8aaf0cb91758025cfc322b339e03997)
  - クライアント退室後にサーバーサイドのclienthandlerのリソースが解放されているかのテスト
- [ab9d3a3](https://github.com/kmansei/chatapp/commit/ab9d3a3094683743d31556e1ee8d42c2a4f962af)
  - サーバー及びクライアントにてnioを使うように修正
  - 実装にあたって下記の記事を参照
    - https://www.kimullaa.com/posts/201612081500/
    - https://jenkov.com/tutorials/java-nio/index.html
- [1a05eeb](https://github.com/kmansei/chatapp/commit/1a05eeb34fe1f18e69c50010b4c197f0ba052887)
  - Gradle導入
- [1bc94d](https://github.com/kmansei/chatapp/commit/1bc94def92545aa1555a70e1b60842eb25525208)
  - Netty導入
  - https://netty.io/wiki/user-guide-for-4.x.html#wiki-h3-8