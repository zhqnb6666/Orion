### axios希望接收的数据结构

#### Assignment
- **id**: 唯一的数字标识符
- **name**: 作业名称
- **type**: 作业类型
- **score**: 数字分数（如果未评分可以为 null）
- **totalPoints**: 最大可能分数
- **dueDate**: 作业截止日期（日期时间格式）
- **submittedDate**: 学生提交时间（可选）
- **gradedDate**: 教师评分时间（可选）
- **status**: 其中之一：
    - `graded`: 作业已评分
    - `upcoming`: 即将到期
    - `submitted`: 已提交，等待评分
    - `missing`: 过期未提交

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
- **createdAt**: 创建时间（日期时间格式）

#### CourseBasicInfo
- **courseName**: 课程名称
- **teacher**: 教师姓名
- **email**: 教师联系邮箱
- **courseDescription**: 详细课程信息

#### RegisterForm
- **username**: 3-20 个字符
- **email**: 有效的电子邮件地址
- **password**: 最少 6 个字符
- **confirmPassword**: 必须与密码匹配
- **role**: 用户类型（ADMIN/STUDENT/TEACHER）
- **verificationCode**: 6 位验证码