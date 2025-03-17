package org.sustech.orion.AI;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileReader {
    // 读取.pdf 文件
    public static String readPdf(String filePath) throws Exception {
        try (PDDocument document = PDDocument.load(new File(filePath))) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }catch (Exception e){
            return "Error reading PDF file: " + e.getMessage();
        }
    }
    // 读取.md 文件
    public static String readRawMd(String filePath) throws Exception {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }
    // 读取 .docx 文件
    public static String readDocx(String filePath) throws Exception {
        try (XWPFDocument doc = new XWPFDocument(new FileInputStream(filePath))) {
            XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
            return extractor.getText();
        }catch (Exception e){
            return "Error reading DOCX file: " + e.getMessage();
        }
    }
    // 读取 .doc 文件
    public static String readDoc(String filePath) throws Exception {
        try (HWPFDocument doc = new HWPFDocument(new FileInputStream(filePath))) {
            WordExtractor extractor = new WordExtractor(doc);
            return extractor.getText();
        }catch  (Exception e){
            return "Error reading DOC file: " + e.getMessage();
        }
    }

}
