package org.sustech.orion.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sustech.orion.AI.FileReader;
import org.sustech.orion.AI.Model;
import org.sustech.orion.model.PlagiarismCheck;
import org.sustech.orion.model.Submission;
import org.sustech.orion.model.SubmissionContent;
import org.sustech.orion.repository.PlagiarismCheckRepository;
import org.sustech.orion.repository.SubmissionRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlagiarismService {

    @Autowired
    private PlagiarismCheckRepository plagiarismCheckRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    /**
     * 对两个提交进行查重检查
     *
     * @param submissionAId 第一个提交的ID
     * @param submissionBId 第二个提交的ID
     * @param modelName     使用的AI模型名称
     * @return 查重结果
     */
    public PlagiarismCheck checkPlagiarism(Long submissionAId, Long submissionBId, String modelName) {
        Submission submissionA = submissionRepository.findById(submissionAId)
                .orElseThrow(() -> new RuntimeException("提交A不存在"));
        Submission submissionB = submissionRepository.findById(submissionBId)
                .orElseThrow(() -> new RuntimeException("提交B不存在"));

        // 确保两个提交属于同一个作业
        if (!submissionA.getAssignment().getId().equals(submissionB.getAssignment().getId())) {
            throw new RuntimeException("两个提交必须属于同一个作业才能进行查重");
        }

        // 获取提交内容
        String contentA = extractSubmissionContent(submissionA).trim();
        String contentB = extractSubmissionContent(submissionB).trim();

        // cut off
        System.out.println("提交A长度: " + contentA.length());
        System.out.println("提交B长度: " + contentB.length());
        if (contentA.length() > 2048){
            contentA = contentA.substring(0, 2048);
        }
        if (contentB.length() > 2048){
            contentB = contentB.substring(0, 2048);
        }

        // 调用AI模型进行查重
        System.out.println("plagiarism check sent");
        String result = Model.llm_plagiarism_check(contentA, contentB, modelName);

        // 解析查重结果，这里假设AI模型返回的结果包含相似度分数
        double similarityScore = parseSimilarityScore(result);

        // 创建查重记录
        PlagiarismCheck plagiarismCheck = new PlagiarismCheck();
        plagiarismCheck.setAssignment(submissionA.getAssignment());
        plagiarismCheck.setSubmissionA(submissionA);
        plagiarismCheck.setSubmissionB(submissionB);
        plagiarismCheck.setSimilarityScore(similarityScore);
        plagiarismCheck.setCheckTime(Timestamp.from(Instant.now()));
        plagiarismCheck.setStatus("COMPLETED");
        plagiarismCheck.setDetails(result);

        // 保存查重结果
        return plagiarismCheckRepository.save(plagiarismCheck);
    }

    /**
     * 提取提交内容
     */
    private String extractSubmissionContent(Submission submission) {
        List<SubmissionContent> contents = submission.getContents();
        StringBuilder contentBuilder = new StringBuilder();
        
        for (SubmissionContent content : contents) {
            try {
                if (content.getType() == SubmissionContent.ContentType.TEXT) {
                    contentBuilder.append(content.getContent()).append("\n\n");
                } else if (content.getType() == SubmissionContent.ContentType.CODE) {
                    if (content.getCodeSubmission() != null && content.getCodeSubmission().getScript() != null) {
                        contentBuilder.append(content.getCodeSubmission().getScript()).append("\n\n");
                    }
                } else if (content.getType() == SubmissionContent.ContentType.FILE) {
                    if (content.getFile() != null && content.getFile().getUrl() != null) {
                        String filePath = content.getFile().getUrl();
                        String fileExtension = getFileExtension(filePath).toLowerCase();
                        
                        // 判断文件类型并尝试读取
                        String fileContent = null;
                        try {
                            if (fileExtension.equals("pdf")) {
                                fileContent = FileReader.readPdf(filePath);
                            } else if (fileExtension.equals("md")) {
                                fileContent = FileReader.readRawMd(filePath);
                            } else if (fileExtension.equals("docx")) {
                                fileContent = FileReader.readDocx(filePath);
                            } else if (fileExtension.equals("doc")) {
                                fileContent = FileReader.readDoc(filePath);
                            } else {
                                // 对于其他文本类型，尝试直接读取
                                Path path = Paths.get(filePath);
                                if (Files.exists(path) && Files.isReadable(path)) {
                                    try {
                                        fileContent = new String(Files.readAllBytes(path));
                                    } catch (Exception e) {
                                        System.err.println("无法读取文件: " + filePath + ", 错误: " + e.getMessage());
                                    }
                                }
                            }
                            
                            if (fileContent != null && !fileContent.isEmpty()) {
                                contentBuilder.append(fileContent).append("\n\n");
                            }
                        } catch (Exception e) {
                            System.err.println("读取文件失败: " + filePath + ", 错误: " + e.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("处理提交内容时出错: " + e.getMessage());
            }
        }
        
        return contentBuilder.toString().trim();
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return "";
        }
        
        int lastDotPos = filePath.lastIndexOf(".");
        if (lastDotPos >= 0 && lastDotPos < filePath.length() - 1) {
            return filePath.substring(lastDotPos + 1);
        }
        
        return "";
    }

    /**
     * 解析AI返回的相似度分数
     */
    private double parseSimilarityScore(String aiResult) {
        // 这里需要根据AI模型返回的格式进行解析
        // 假设AI模型返回的结果中包含类似"相似度: 85.5%"这样的文本
        try {
            if (aiResult.contains("\\boxed{")) {
                int start = aiResult.indexOf("\\boxed{") + "\\boxed{".length();
                int end = aiResult.indexOf("}", start);
                if (start >= 0 && end > start) {
                    String scoreStr = aiResult.substring(start, end).trim();
                    return Double.parseDouble(scoreStr);
                }
            }
            // 默认返回一个中间值
            return 60.0;
        } catch (Exception e) {
            // 解析失败时返回默认值
            return 50.0;
        }
    }

    /**
     * 获取特定作业的所有查重记录
     */
    public List<PlagiarismCheck> getChecksByAssignment(Long assignmentId) {
        return plagiarismCheckRepository.findByAssignmentId(assignmentId);
    }

    /**
     * 获取特定查重记录的详情
     */
    public PlagiarismCheck getCheckById(Long checkId) {
        return plagiarismCheckRepository.findById(checkId)
                .orElseThrow(() -> new RuntimeException("查重记录不存在"));
    }
} 