# Music Platform Frontend

基于 `Vue 3 + Vite + Pinia + Vue Router` 的前端工作台，按当前 Spring Boot 后端接口实现。

## 已覆盖模块

- 登录 / 注册
- 总览页
- 歌曲搜索、收藏、上传、评论、详情
- 帖子搜索、排序、发帖、详情、评论
- 用户搜索、关注 / 粉丝、用户详情预览
- 私信会话与实时消息
- 个人资料修改
- 举报创建与举报记录
- 管理台举报处理、封禁、添加管理员

## 启动方式

```bash
npm install
npm run dev
```

开发时请尽量从 `http://localhost:5173/#/` 进入。前端路由已改为 `hash` 模式，这样刷新页面时不会把 `/search`、`/messages` 这类前端路径直接交给 Spring Boot 处理。

默认会把以下路径代理到 `http://localhost:8080`：

- `/user`
- `/song`
- `/post`
- `/message`
- `/report`
- `/admin`
- `/superAdmin`
- `/connect`
- `/media`
- `/default`

其中媒体文件直接依赖后端 `WebConfig` 的静态资源映射访问，也就是：

- 数据库或接口里如果拿到的是 `/media/**`，前端直接访问
- 如果后端某些接口返回的是裸相对路径，例如 `ab/cd/file.png`，前端会自动补成 `/media/ab/cd/file.png`

如果后端地址不是 `http://localhost:8080`，先复制 `.env.example` 为 `.env`，再修改：

```bash
VITE_API_TARGET=http://your-backend-host:port
```

## 构建

```bash
npm run build
```

构建产物会输出到 `dist/`。
