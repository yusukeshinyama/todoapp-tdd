# todoapp-tdd

このリポジトリは、TDDワークショップのための演習教材が入っている。
題材として、Spring Boot + DynamoDB を使った簡単な TODO アプリを使う。

このワークショップの目的は2つある:

- システムの機能を外側からテストする (blackbox test) 方法を習得してもらう。
- TDDによってリファクタリングが安心してできるかを体験してもらう。

## 使い方

1. git cloneする。
2. トップレベルのフォルダで `$ docker compose up` を実行。
3. `backend`フォルダで `$ make test` を実行し、エラーが出ないことを確認。
4. `backend/src/test/.../TodoApplicationTests.kt` を開き、
   コメントアウトされているテストを埋めていく。

## 最終的に作りたいもの

```shell
# 2つの項目を新規に作成する。
$ curl -d '{"text":"foo"}' http://localhost:8080/api/todo
$ curl -d '{"text":"bar"}' http://localhost:8080/api/todo

# 作成した項目の一覧がJSONで取得できる。
$ curl http://localhost:8080/api/todo
[{"id": 1, "text":"foo"}, {"id":2, "text":"bar"}]

# IDを指定してひとつの項目を個別に取得できる。
$ curl http://localhost:8080/api/todo/1
{"id": 1, text:"foo"}

# IDを指定して項目を削除できる。
$ curl -X DELETE http://localhost:8080/api/todo/1
# 削除されていることを確認。
$ curl http://localhost:8080/api/todo
[{"id":2, "text":"bar"}]
```

## ファイル

- `README.md`: このファイル。
- `docker-compose.yml`: LocalStackを動かすためのファイル。
- `backend`: 実際のbackendコードが入っているフォルダ。(Spring Initializrで作成)
- `localstack`: LocalStack初期化用スクリプト。
