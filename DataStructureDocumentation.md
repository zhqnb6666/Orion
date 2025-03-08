### Data Structure Documentation

#### Assignment
- **id**: Unique numeric identifier
- **name**: Assignment name
- **type**: Assignment type
- **score**: Numeric score (can be null if ungraded)
- **totalPoints**: Maximum possible points
- **dueDate**: Assignment deadline (date-time format)
- **submittedDate**: When student submitted (optional)
- **gradedDate**: When teacher graded (optional)
- **status**: One of:
    - `GRADED`: Assignment has been graded
    - `UPCOMING`: Due in the future
    - `SUBMITTED`: Turned in, awaiting grade
    - `MISSING`: Past due, not submitted

#### CourseMaterial
- **id**: Unique string identifier
- **title**: Material title
- **type**: Material type
- **description**: Material description
- **attachments**: List of attached files
    - Each attachment includes:
        - id: Unique identifier
        - name: File name
        - size: File size (formatted string)

#### Task
- **id**: Unique string identifier
- **title**: Task title
- **deadline**: Due date and time
- **completed**: Boolean status
- **classID**: Associated class identifier
- **className**: Associated class name
- **description**: Optional task details

#### ClassInformation
1. ClassItem
    - **id**: Unique string identifier
    - **name**: Class name
    - **description**: Class description

2. BasicInformation
    - **className**: Name of the course
    - **teacher**: Teacher's name
    - **email**: Teacher's contact email
    - **courseDescription**: Detailed course information

#### Registration Data
- **username**: 3-20 characters
- **email**: Valid email address
- **password**: Minimum 6 characters
- **confirmPassword**: Must match password
- **role**: User type (ADMIN/STUDENT/TEACHER)
- **verificationCode**: 6-digit verification code