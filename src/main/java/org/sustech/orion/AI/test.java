package org.sustech.orion.AI;

public class test {
    public static void main(String[] args) throws Exception {
        String input =  "你好，你是谁";
        System.out.println(model.chat(input));

        String text = FileReader.readPdf("D:\\desktop\\江澈.pdf");
        System.out.println(text);

        text = FileReader.readRawMd("D:\\desktop\\report1\\report1\\report1 - team 5 - zh.md");
        System.out.println(text);

        text = FileReader.readDocx("D:\\desktop\\简历.docx");
        System.out.println(text);

        String photo = "D:\\desktop\\微信截图_20250317143531.png";
        System.out.println(ocr.OCR(photo));
    }
}
