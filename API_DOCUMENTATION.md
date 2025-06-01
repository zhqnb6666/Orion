# Orion API 文档

API分为学生端、教师端和管理员端三个主要部分，每个部分包含多个功能模块。

## 通用格式

### 请求格式

所有API请求需要在Header中包含授权信息：

```
Authorization: Bearer {jwt_token}
```

### 响应格式

所有API响应均为JSON格式，包含以下基本结构：

- 成功响应：直接返回数据对象或数组
- 错误响应：包含错误信息和HTTP状态码

## 1. 认证API

### 登录

- **URL**: `/api/auth/login`
- **方法**: POST
- **描述**: 使用用户名/邮箱和密码登录系统
- **请求体**:
  ```json
  {
    "usernameOrEmail": "string",
    "password": "string"
  }
  ```
- **响应**:
  ```json
  {
    "token": "string"
  }
  ```

### 注册

- **URL**: `/api/auth/register`
- **方法**: POST
- **描述**: 注册新用户
- **请求体**:
  ```json
  {
    "username": "string",
    "email": "string",
    "password": "string",
    "role": "STUDENT|TEACHER|ADMIN",
    "verificationCode": "string"
  }
  ```
- **响应**: 成功注册消息

### 发送验证码

- **URL**: `/api/auth/send-verification/{email}`
- **方法**: POST
- **描述**: 向指定邮箱发送验证码
- **路径参数**: `email` - 用户邮箱
- **响应**: 验证码发送成功消息

### 重置密码

- **URL**: `/api/auth/reset-password`
- **方法**: POST
- **描述**: 重置用户密码
- **请求体**:
  ```json
  {
    "email": "string",
    "verificationCode": "string",
    "newPassword": "string"
  }
  ```
- **响应**: 密码重置成功消息

### 获取当前用户信息

- **URL**: `/api/auth/user`
- **方法**: POST
- **描述**: 获取当前登录用户的基本信息
- **请求头**: 需要包含JWT令牌
- **响应**:
  ```json
  {
    "userId": "number",
    "username": "string",
    "email": "string",
    "role": "string",
    "isTokenExpired": "boolean"
  }
  ```

## 2. 学生API

### 2.1 课程相关

#### 获取所有课程

- **URL**: `/api/students/courses`
- **方法**: GET
- **描述**: 获取学生所有课程列表
- **响应**: 课程列表数组

#### 获取课程详情

- **URL**: `/api/students/courses/{courseId}`
- **方法**: GET
- **描述**: 获取特定课程基础信息
- **路径参数**: `courseId` - 课程ID
- **响应**: 课程详情对象

#### 获取当前学期课程

- **URL**: `/api/students/courses/current`
- **方法**: GET
- **描述**: 获取当前学期课程
- **响应**: 课程列表数组

#### 加入课程

- **URL**: `/api/students/courses/join`
- **方法**: POST
- **描述**: 通过课程代码加入课程
- **请求参数**: `courseCode` - 课程邀请码
- **响应**: 加入的课程信息

### 2.2 作业相关

#### 获取课程作业

- **URL**: `/api/students/courses/{courseId}/assignments`
- **方法**: GET
- **描述**: 获取课程下所有作业
- **路径参数**: `courseId` - 课程ID
- **响应**: 作业列表数组

#### 获取作业详情

- **URL**: `/api/students/assignments/{assignmentId}`
- **方法**: GET
- **描述**: 获取特定作业详情
- **路径参数**: `assignmentId` - 作业ID
- **响应**: 作业详情对象

#### 获取即将截止的作业

- **URL**: `/api/students/assignments/upcoming`
- **方法**: GET
- **描述**: 获取即将截止的作业
- **响应**: 作业列表数组

### 2.3 提交相关

#### 创建普通提交

- **URL**: `/api/students/submissions/{assignmentId}/submissions`
- **方法**: POST
- **描述**: 创建新提交（支持文件上传）
- **路径参数**: `assignmentId` - 作业ID
- **请求体**: 
  - `content` - 文本内容（可选）
  - `files` - 文件数组（可选）
- **响应**: 提交详情对象

#### 创建代码提交

- **URL**: `/api/students/assignments/{assignmentId}/submissions/code`
- **方法**: POST
- **描述**: 创建新代码提交
- **路径参数**: `assignmentId` - 作业ID
- **请求体**:
  ```json
  {
    "script": "string",
    "language": "string",
    "versionIndex": "number"
  }
  ```
- **响应**: 提交详情对象

#### 获取提交详情

- **URL**: `/api/students/submissions/{submissionId}`
- **方法**: GET
- **描述**: 获取提交详情
- **路径参数**: `submissionId` - 提交ID
- **响应**: 提交详情对象

#### 获取代码提交结果

- **URL**: `/api/students/assignments/submissions/code/{submissionId}`
- **方法**: GET
- **描述**: 获取代码提交的运行结果
- **路径参数**: `submissionId` - 提交ID
- **响应**: 代码运行结果对象

#### 获取提交历史

- **URL**: `/api/students/assignments/{assignmentId}/submissions`
- **方法**: GET
- **描述**: 获取作业提交历史
- **路径参数**: `assignmentId` - 作业ID
- **响应**: 提交列表数组

### 2.4 成绩和反馈

#### 获取提交评分

- **URL**: `/api/students/submissions/{submissionId}/grade`
- **方法**: GET
- **描述**: 获取提交的评分和评语
- **路径参数**: `submissionId` - 提交ID
- **响应**: 评分详情对象

#### 获取课程成绩

- **URL**: `/api/students/courses/{courseId}/grades`
- **方法**: GET
- **描述**: 获取课程所有成绩
- **路径参数**: `courseId` - 课程ID
- **响应**: 成绩列表数组

#### 提交成绩申诉

- **URL**: `/api/students/submissions/{submissionId}/appeal`
- **方法**: POST
- **描述**: 提交成绩申诉
- **路径参数**: `submissionId` - 提交ID
- **请求体**: 申诉理由文本
- **响应**: 申诉提交成功状态

## 3. 教师API

### 3.1 课程管理

#### 创建课程

- **URL**: `/api/teachers/courses`
- **方法**: POST
- **描述**: 创建新课程
- **请求体**:
  ```json
  {
    "courseName": "string",
    "courseCode": "string",
    "description": "string",
    "isActive": "boolean"
  }
  ```
- **响应**: 创建的课程信息

#### 获取教师所有课程

- **URL**: `/api/teachers/courses`
- **方法**: GET
- **描述**: 获取当前教师负责的所有课程
- **响应**: 课程列表数组

#### 更新课程信息

- **URL**: `/api/teachers/courses/{courseId}`
- **方法**: PUT
- **描述**: 更新课程信息
- **路径参数**: `courseId` - 课程ID
- **请求体**:
  ```json
  {
    "courseName": "string",
    "description": "string",
    "isActive": "boolean"
  }
  ```
- **响应**: 更新后的课程信息

#### 删除课程

- **URL**: `/api/teachers/courses/{courseId}`
- **方法**: DELETE
- **描述**: 删除课程
- **路径参数**: `courseId` - 课程ID
- **响应**: 无内容(204)

#### 获取课程学生列表

- **URL**: `/api/teachers/courses/{courseId}/students`
- **方法**: GET
- **描述**: 获取课程学生列表
- **路径参数**: `courseId` - 课程ID
- **响应**: 学生列表数组

#### 添加学生到课程

- **URL**: `/api/teachers/courses/{courseId}/students`
- **方法**: POST
- **描述**: 添加学生到课程
- **路径参数**: `courseId` - 课程ID
- **请求参数**: `studentId` - 学生ID
- **响应**: 无内容(204)

#### 从课程移除学生

- **URL**: `/api/teachers/courses/{courseId}/students/{studentId}`
- **方法**: DELETE
- **描述**: 从课程移除学生
- **路径参数**: 
  - `courseId` - 课程ID
  - `studentId` - 学生ID
- **响应**: 无内容(204)

### 3.2 作业管理

#### 创建作业

- **URL**: `/api/teachers/courses/{courseId}/assignments`
- **方法**: POST
- **描述**: 创建新作业
- **路径参数**: `courseId` - 课程ID
- **请求体**: 作业信息对象
- **响应**: 创建的作业信息

#### 获取作业详情

- **URL**: `/api/teachers/assignments/{assignmentId}`
- **方法**: GET
- **描述**: 获取作业详情
- **路径参数**: `assignmentId` - 作业ID
- **响应**: 作业详情对象

#### 更新作业

- **URL**: `/api/teachers/assignments/{assignmentId}`
- **方法**: PUT
- **描述**: 更新作业
- **路径参数**: `assignmentId` - 作业ID
- **请求体**: 更新的作业信息
- **响应**: 更新后的作业信息

#### 发布/取消发布作业

- **URL**: `/api/teachers/assignments/{assignmentId}/publish`
- **方法**: PUT
- **描述**: 设置作业可见
- **路径参数**: `assignmentId` - 作业ID
- **响应**: 更新后的作业信息

- **URL**: `/api/teachers/assignments/{assignmentId}/unpublish`
- **方法**: PUT
- **描述**: 设置作业不可见
- **路径参数**: `assignmentId` - 作业ID
- **响应**: 更新后的作业信息

### 3.3 提交和评分管理

#### 获取作业的所有提交

- **URL**: `/api/teachers/assignments/{assignmentId}/submissions`
- **方法**: GET
- **描述**: 获取作业的所有提交
- **路径参数**: `assignmentId` - 作业ID
- **响应**: 提交列表数组

#### 获取提交详情

- **URL**: `/api/teachers/submissions/{submissionId}`
- **方法**: GET
- **描述**: 获取提交详情
- **路径参数**: `submissionId` - 提交ID
- **响应**: 提交详情对象

#### 提交评分

- **URL**: `/api/teachers/submissions/{submissionId}/grade`
- **方法**: POST
- **描述**: 提交评分
- **路径参数**: `submissionId` - 提交ID
- **请求体**: 评分信息
- **响应**: 评分结果

#### 获取AI评分

- **URL**: `/api/teachers/submissions/{submissionId}/AIGrade`
- **方法**: GET
- **描述**: 获取AI评分
- **路径参数**: `submissionId` - 提交ID
- **响应**: AI评分结果

### 3.4 测试用例管理

#### 获取测试用例

- **URL**: `/api/teachers/assignments/{assignmentId}/testcases`
- **方法**: GET
- **描述**: 获取作业的测试用例
- **路径参数**: `assignmentId` - 作业ID
- **响应**: 测试用例列表

#### 添加测试用例

- **URL**: `/api/teachers/assignments/{assignmentId}/testcases`
- **方法**: POST
- **描述**: 添加测试用例
- **路径参数**: `assignmentId` - 作业ID
- **请求体**: 测试用例信息
- **响应**: 创建的测试用例

### 3.5 抄袭检测

#### 启动抄袭检测

- **URL**: `/api/teachers/assignments/{assignmentId}/plagiarismCheck`
- **方法**: POST
- **描述**: 启动抄袭检测
- **路径参数**: `assignmentId` - 作业ID
- **响应**: 检测任务ID

#### 获取抄袭检测结果

- **URL**: `/api/teachers/assignments/{assignmentId}/plagiarismResults`
- **方法**: GET
- **描述**: 获取抄袭检测结果
- **路径参数**: `assignmentId` - 作业ID
- **响应**: 检测结果列表

## 4. 管理员API

### 4.1 用户管理

#### 获取所有用户

- **URL**: `/api/admin/users`
- **方法**: GET
- **描述**: 获取所有用户
- **响应**: 用户列表数组

#### 创建用户

- **URL**: `/api/admin/users`
- **方法**: POST
- **描述**: 创建用户
- **请求体**: 用户信息
- **响应**: 创建的用户信息

#### 更新用户信息

- **URL**: `/api/admin/users/{userId}`
- **方法**: PUT
- **描述**: 更新用户信息
- **路径参数**: `userId` - 用户ID
- **请求体**: 更新的用户信息
- **响应**: 更新后的用户信息

#### 更改用户角色

- **URL**: `/api/admin/users/{userId}/role`
- **方法**: PUT
- **描述**: 更改用户角色
- **路径参数**: `userId` - 用户ID
- **请求体**: 新角色信息
- **响应**: 更新后的用户信息



## 5. 错误处理

所有API错误响应遵循统一格式：

```json
{
  "message": "错误描述信息",
  "status": 400  // HTTP状态码
}
```

常见错误状态码：
- 400: 请求参数错误
- 401: 未授权
- 403: 权限不足
- 404: 资源不存在
- 409: 资源冲突
- 500: 服务器内部错误

## 6. 安全与认证

系统使用JWT（JSON Web Token）进行身份验证和授权。每个API请求需要在Header中包含有效的JWT令牌：

```
Authorization: Bearer {jwt_token}
```

令牌过期时间默认为24小时，过期后需要重新登录获取新令牌。 