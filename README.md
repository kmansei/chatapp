## エントリーポイント

```
サーバー
java Serve

クライアント
java Client
```

## 実装
- IPはローカルホスト使用
- 最大接続数50
  - ServerSocketクラスの上限(?)
  > The maximum queue length for incoming connection indications (a request to connect) is set to 50. If a connection indication arrives when the queue is full, the connection is refused.
  > 
