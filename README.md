```
サーバー
java Server

クライアント
java Client
```

## 実装
- IPはローカルホストを使用
- 最大接続数50
  - ServerSocketクラスの上限
  > The maximum queue length for incoming connection indications (a request to connect) is set to 50. If a connection indication arrives when the queue is full, the connection is refused.

## 改善案
- プログラムが動作するのは同一端末なので、Unixドメインソケットを使ってみる。
- サーバーからのブロードキャストにUDPを使ってみる
- 接続数が50を超えたら新たにServersocketをnewして、そちらを使用する。
  - どのserversocketを管理するクラスが必要そう
