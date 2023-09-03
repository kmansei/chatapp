## 動作確認
```
サーバー
java Server

クライアント
java Client
```

## 実装
- src/Server.java
  - サーバー実装
- src/Client.java
  - クライアント実装
- src/ServerTest.java
  - テスト

## 使用ライブラリ
- JUnit4
  - テスト

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