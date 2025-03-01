### 学生端

#### 1. 用户认证与个人信息

```
GET /api/auth/user           # 获取当前登录用户信息	*zhq
POST /api/auth/send-verification/{email}	#发送邮件	*z
PUT /api/students/profile    # 更新个人资料
GET /api/students/dashboard  # 获取学生仪表盘数据(包括待完成作业、最近截止等)
```

#### 2. 课程相关

```
GET /api/students/courses                  # 获取学生所有课程列表
GET /api/students/courses/{courseId}       # 获取特定课程详情
GET /api/students/courses/current          # 获取当前学期课程
POST /api/students/courses/{courseId}/join # 加入课程
```

#### 3. 作业相关

```
GET /api/students/courses/{courseId}/assignments           # 获取课程下所有作业
GET /api/students/assignments/{assignmentId}               # 获取特定作业详情
GET /api/students/assignments/upcoming                     # 获取即将截止的作业
```

#### 4. 提交相关

```
POST /api/students/assignments/{assignmentId}/submissions            # 创建新提交
GET /api/students/assignments/{assignmentId}/submissions             # 获取自己的提交历史
GET /api/students/submissions/{submissionId}                         # 获取提交详情
PUT /api/students/submissions/{submissionId}                         # 更新提交
DELETE /api/students/submissions/{submissionId}                      # 删除提交
GET /api/students/submissions/{submissionId}/status                  # 检查提交状态
POST /api/students/submissions/{submissionId}/files                  # 上传提交文件
GET /api/students/assignments/{assignmentId}/submissions/remaining   # 获取剩余提交次数
```

#### 5. 成绩和反馈

```
GET /api/students/submissions/{submissionId}/grade               # 获取提交的评分和评语
GET /api/students/courses/{courseId}/grades                      # 获取课程所有成绩
GET /api/students/assignments/{assignmentId}/feedback            # 获取作业反馈
GET /api/students/grades/summary                                 # 获取成绩汇总
POST /api/students/submissions/{submissionId}/appeal             # 提交成绩申诉
```

#### 6. 通知和消息

```
GET /api/students/notifications              # 获取所有通知
GET /api/students/notifications/unread       # 获取未读通知
PUT /api/students/notifications/{id}/read    # 标记通知为已读
DELETE /api/students/notifications/{id}      # 删除通知
```

#### 7. 资源获取

```
GET /api/students/courses/{courseId}/resources                  # 获取课程资源
GET /api/students/assignments/{assignmentId}/resources          # 获取作业相关资源
GET /api/students/resources/{resourceId}/download               # 下载特定资源
```

### 教师端

#### 1. 用户认证与个人信息

```
PUT /api/teachers/profile          # 更新个人资料
GET /api/teachers/dashboard        # 获取教师仪表盘数据(待批改作业、课程概览等)
```

#### 2. 课程管理

```
GET /api/teachers/courses                     # 获取教师所有课程
POST /api/teachers/courses                    # 创建新课程
PUT /api/teachers/courses/{courseId}          # 更新课程信息
DELETE /api/teachers/courses/{courseId}       # 删除课程
GET /api/teachers/courses/{courseId}          # 获取课程详情
GET /api/teachers/courses/{courseId}/invitationCode  # 生成邀请码
GET /api/teachers/courses/{courseId}/students # 获取课程学生列表
POST /api/teachers/courses/{courseId}/students # 添加学生到课程
DELETE /api/teachers/courses/{courseId}/students/{studentId} # 从课程移除学生
```

#### 3. 作业管理

```
GET /api/teachers/courses/{courseId}/assignments        # 获取课程下所有作业
POST /api/teachers/courses/{courseId}/assignments       # 创建新作业
GET /api/teachers/assignments/{assignmentId}            # 获取作业详情
PUT /api/teachers/assignments/{assignmentId}            # 更新作业
DELETE /api/teachers/assignments/{assignmentId}         # 删除作业
PUT /api/teachers/assignments/{assignmentId}/publish    # 设置作业可见
PUT /api/teachers/assignments/{assignmentId}/unpublish  # 设置作业不可见
```

#### 4. 测试用例管理

```
GET /api/teachers/assignments/{assignmentId}/testcases         # 获取作业的测试用例
POST /api/teachers/assignments/{assignmentId}/testcases        # 添加测试用例
PUT /api/teachers/testcases/{testcaseId}                       # 更新测试用例
DELETE /api/teachers/testcases/{testcaseId}                    # 删除测试用例
```

#### 5. 提交和评分管理

```
GET /api/teachers/assignments/{assignmentId}/submissions        # 获取作业的所有提交
GET /api/teachers/submissions/{submissionId}                    # 获取提交详情
GET /api/teachers/courses/{courseId}/submissions/pending        # 获取待批改的提交
POST /api/teachers/submissions/{submissionId}/grade             # 提交评分
PUT /api/teachers/submissions/{submissionId}/grade              # 更新评分
PUT /api/teachers/grades/{gradeId}/finalize                     # 确认最终成绩
GET /api/teachers/assignments/{assignmentId}/statistics         # 获取作业统计信息
GET /api/teachers//submissions/{submissionId}/AIGrade			# 获取AI评分
POST /api/teachers/assignments/{assignmentId}/batchGrade        # 批量AI评分
GET /api/teachers/submissions/appeals                           # 获取成绩申诉列表
PUT /api/teachers/submissions/appeals/{appealId}                # 处理成绩申诉
```

#### 6. 资源管理

```
POST /api/teachers/courses/{courseId}/resources                 # 上传课程资源
GET /api/teachers/courses/{courseId}/resources                  # 获取课程资源
DELETE /api/teachers/resources/{resourceId}                     # 删除资源
PUT /api/teachers/resources/{resourceId}                        # 更新资源信息
POST /api/teachers/assignments/{assignmentId}/resources         # 上传作业资源
```

#### 7. 抄袭检测

```
POST /api/teachers/assignments/{assignmentId}/plagiarismCheck   # 启动抄袭检测
GET /api/teachers/assignments/{assignmentId}/plagiarismResults  # 获取抄袭检测结果
GET /api/teachers/plagiarismChecks/{checkId}/details            # 获取抄袭检测详情
```

#### 8. 通知管理

```
POST /api/teachers/courses/{courseId}/announcements             # 发布课程公告
GET /api/teachers/courses/{courseId}/announcements              # 获取课程公告
DELETE /api/teachers/announcements/{announcementId}             # 删除公告
PUT /api/teachers/announcements/{announcementId}                # 编辑公告
```

### 管理员端

#### 1. 用户认证与管理

```
GET /api/admin/users                                    # 获取所有用户
POST /api/admin/users                                   # 创建用户
GET /api/admin/users/{userId}                           # 获取用户详情
PUT /api/admin/users/{userId}                           # 更新用户信息
DELETE /api/admin/users/{userId}                        # 删除用户
PUT /api/admin/users/{userId}/status                    # 更改用户状态(激活/停用)
PUT /api/admin/users/{userId}/role                      # 更改用户角色
POST /api/admin/users/import                            # 批量导入用户
GET /api/admin/users/export                             # 导出用户数据
```

#### 2. 课程管理

```
GET /api/admin/courses                                 # 获取所有课程
GET /api/admin/courses/{courseId}                      # 获取课程详情
PUT /api/admin/courses/{courseId}                      # 管理员更新课程信息
DELETE /api/admin/courses/{courseId}                   # 管理员删除课程
PUT /api/admin/courses/{courseId}/status               # 更改课程状态
POST /api/admin/courses/archive                        # 批量归档课程
```

#### 3. 系统配置管理

```
GET /api/admin/configs                                # 获取系统配置
PUT /api/admin/configs                                # 更新系统配置
GET /api/admin/configs/submission                     # 获取提交相关配置
PUT /api/admin/configs/submission                     # 更新提交相关配置
GET /api/admin/configs/notification                   # 获取通知相关配置
PUT /api/admin/configs/notification                   # 更新通知相关配置
```

#### 4. 权限管理

```
GET /api/admin/roles                                  # 获取所有角色
POST /api/admin/roles                                 # 创建新角色
PUT /api/admin/roles/{roleId}                         # 更新角色
DELETE /api/admin/roles/{roleId}                      # 删除角色
GET /api/admin/permissions                            # 获取所有权限
PUT /api/admin/roles/{roleId}/permissions             # 更新角色权限
```

#### 5. 系统监控与日志

```
GET /api/admin/logs                                 # 获取系统日志
GET /api/admin/logs/actions                         # 获取操作日志
GET /api/admin/logs/errors                          # 获取错误日志
GET /api/admin/logs/access                          # 获取访问日志
GET /api/admin/stats/users                          # 获取用户统计
GET /api/admin/stats/submissions                    # 获取提交统计
GET /api/admin/stats/performance                    # 获取系统性能统计
GET /api/admin/stats/storage                        # 获取存储使用统计
```

#### 6. AI评分管理

```
GET /api/admin/ai/status                           # 获取AI服务状态
PUT /api/admin/ai/settings                         # 更新AI服务设置
GET /api/admin/ai/models                           # 获取可用AI模型
PUT /api/admin/ai/models/{modelId}                 # 配置特定AI模型
GET /api/admin/ai/statistics                       # 获取AI评分统计
```