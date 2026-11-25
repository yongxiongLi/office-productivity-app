# Flow办公

An Android productivity app for meeting notes, tasks and reminders, and daily/weekly reports.

## 功能

- 首页工作台：查看待办总数、今日到期任务与会议记录快捷入口。
- 任务：创建标题、截止时间、优先级和负责人；按今天、即将到期、已完成或全部筛选；完成/删除任务。
- 提醒：截止时间通过 WorkManager 安排本地通知。Android 13+ 需要通知权限；系统可能延后后台任务，因此不保证精确到分钟。
- 会议：记录标题、时间、参与人和内容；可填写行动项、负责人、优先级和截止时间，保存后自动关联创建任务。
- 报告：查看今日或本周完成的任务汇总。
- Room 本地存储，并提供从旧版数据库的 1→2 迁移。

## 运行

1. 使用 Android Studio 打开项目，安装 Android SDK 35 和 JDK 17。
2. 等待 Gradle Sync 完成后选择 `app` 配置。
3. 在模拟器或 Android 设备上运行。

## 后续方向

可继续增加任务编辑、会议关联任务列表、报告导出、自动化测试及云端同步。
