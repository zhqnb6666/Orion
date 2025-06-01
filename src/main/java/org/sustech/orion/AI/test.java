package org.sustech.orion.AI;

public class test {
    public static void main(String[] args) throws Exception {
//        String input =  "你好，你是谁";
//        //available models: qwq-32b qwen3-32b deepseek-r1-distill-qwen-32b qwen3-235b-a22b
//        System.out.println(Model.chat(input, "qwen3-235b-a22b"));
//
        // 大模型查重示例代码
        String a = "值传递：传递的是变量的副本，修改不会影响原始变量。\n" +
                  "引用传递：传递的是对象的引用副本，修改对象内容会影响原始对象，但不能改变引用本身。 Java 中没有真正的引用传递，所有传递的都是值（包括对象引用的值）。";
        String b = "值传递：传递的是变量的副本，修改不会影响原始变量。\n" +
                  "传递引用或者指针：传递的是对象的引用副本，修改对象内容会影响原始对象，但不能改变引用本身。 Java 中没有真正的引用传递，所有传递的都是值（包括对象引用的值）。";
        System.out.println(Model.llm_plagiarism_check(a,b,"qwen3-32b"));
//
//        // 大模型评分示例代码
//        String Question = "请简要介绍一下Java中的异常处理机制";
//        String Answer = "Java中的异常处理机制主要通过try-catch-finally语句来实现。try块中包含可能抛出异常的代码，catch块用于捕获和处理异常，finally块中的代码无论是否发生异常都会执行。此外，Java还提供了throw和throws关键字来手动抛出异常和声明异常。";
//        System.out.println(Model.llm_grading(Question, Answer, "qwen3-32b"));
//
//        // 文件读取示例代码
//        String text = FileReader.readPdf("D:\\desktop\\江澈.pdf");
//        System.out.println(text);
//
//        text = FileReader.readRawMd("D:\\desktop\\report1\\report1\\report1 - team 5 - zh.md");
//        System.out.println(text);
//
//        text = FileReader.readDocx("D:\\desktop\\简历.docx");
//        System.out.println(text);

        // OCR示例代码
//        String photo = "img.png";
//        System.out.println(OCR.OCR(photo));
    }
}
