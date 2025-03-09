//notice: 0309 此为老版的副本 请参照最新版本
### axios希望接收的数据结构

#### Grade
- **id**: 唯一的数字标识符
- **name**: 名称（例如：作业1，期中考试，quiz2）
- **type**: 成绩类型
- **score**: 数字分数（如果未评分可以为 null）
- **totalPoints**: 最大可能分数
- **dueDate**: 作业截止日期（日期时间格式）
- **submittedDate**: 学生提交时间（可选）
- **gradedDate**: 教师评分时间（可选）
- **feedback**: 教师反馈（可选）
- **appealReason**: 申诉理由（可选）
- **appealTime**: 申诉时间（可选）
- **status**: 其中之一：
    - `graded`: 作业已评分
    - `upcoming`: 即将到期
    - `submitted`: 已提交，等待评分
    - `missing`: 过期未提交
    - `appealing`: 申诉中
    - `appealed`: 申诉已处理

#### CourseMaterial
- **id**: 唯一的字符串标识符
- **title**: 资料标题
- **type**: 资料类型
- **description**: 资料描述
- **attachments**: 附件列表
    - 每个附件包括：
        - id: 唯一标识符
        - name: 文件名
        - size: 文件大小（格式化字符串）


#### Assignment
前半部分是继承自CourseMaterial
- **id**: 唯一的字符串标识符
- **title**: 资料标题
- **type**: 资料类型
- **description**: 资料描述
- **attachments**: 附件列表
    - 每个附件包括：
        - id: 唯一标识符
        - name: 文件名
        - size: 文件大小（格式化字符串）
- **dueDate**: 作业截止日期（日期时间格式）
- **maxScore**: 最大可能分数
- **status**: 其中之一：
    - `open`: 作业开放提交
    - `closed`: 作业已关闭
    - `upcoming`: 作业即将到期
- **instructions**: 作业说明（可选）
- **submissionUrl**: 提交作业的URL

#### Task
- **id**: 唯一的字符串标识符
- **title**: 任务标题
- **deadline**: 截止日期和时间
- **completed**: 布尔状态
- **courseId**: 关联课程标识符
- **courseName**: 关联课程名称
- **description**: 可选任务详情

#### CourseItem
- **id**: 唯一的数字标识符
- **courseCode**: 课程代码
- **courseName**: 课程名称
- **semester**: 学期
- **description**: 课程描述
- **isActive**: 布尔值，表示课程是否激活
- **createdTime**: 创建时间（日期时间格式）

#### CourseBasicInfo
- **courseName**: 课程名称
- **teacher**: 教师姓名
- **email**: 教师联系邮箱
- **courseDescription**: 详细课程信息，可以为markdown格式

#### RegisterForm
- **username**: 3-20 个字符
- **email**: 有效的电子邮件地址
- **password**: 最少 6 个字符
- **confirmPassword**: 必须与密码匹配
- **role**: 用户类型（ADMIN/STUDENT/TEACHER）
- **verificationCode**: 6 位验证码