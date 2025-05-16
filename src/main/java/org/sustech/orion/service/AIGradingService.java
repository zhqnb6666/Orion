package org.sustech.orion.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sustech.orion.AI.FileReader;
import org.sustech.orion.AI.Model;
import org.sustech.orion.model.AIGrading;
import org.sustech.orion.model.Assignment;
import org.sustech.orion.model.Submission;
import org.sustech.orion.model.SubmissionContent;
import org.sustech.orion.repository.AIGradingRepository;
import org.sustech.orion.repository.SubmissionRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AIGradingService {

    @Autowired
    private AIGradingRepository aiGradingRepository;
    
    @Autowired
    private SubmissionRepository submissionRepository;

    /**
     * 为提交进行AI自动评分
     *
     * @param submissionId 提交ID
     * @param modelName    使用的AI模型名称
     * @return AI评分结果
     */
    public AIGrading gradeSubmission(Long submissionId, String modelName) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("提交不存在"));

        // 检查是否已经有AI评分
        if (submission.getAiGrading() != null) {
            return submission.getAiGrading();
        }

        // 获取作业信息和提交内容
        Assignment assignment = submission.getAssignment();
        String question = assignment.getDescription();
        String answer = extractSubmissionContent(submission);

        // 调用AI模型进行评分
        String result = Model.llm_grading(question, answer, modelName);

        // 解析AI评分结果
        double score = parseAIScore(result);
        double confidence = parseConfidence(result);
        String feedback = extractFeedback(result);

        // 创建AI评分记录
        AIGrading aiGrading = new AIGrading();
        aiGrading.setSubmission(submission);
        aiGrading.setAiScore(score);
        aiGrading.setConfidence(confidence);
        aiGrading.setFeedbackSuggestions(feedback);

        // 保存AI评分结果
        return aiGradingRepository.save(aiGrading);
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
                }
                else if (content.getType() == SubmissionContent.ContentType.FILE) {
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
     * 解析AI返回的分数
     */
    private double parseAIScore(String aiResult) {
        try {
            // 简单实现，假设AI模型返回的结果中包含类似"分数: 85"这样的文本
            if (aiResult.contains("分数:")) {
                String[] parts = aiResult.split("分数:");
                if (parts.length > 1) {
                    String scoreStr = parts[1].trim().split("[^0-9.]")[0];
                    return Double.parseDouble(scoreStr);
                }
            }
            // 默认返回一个中间值
            return 60.0;
        } catch (Exception e) {
            // 解析失败时返回默认值
            return 60.0;
        }
    }

    /**
     * 解析AI返回的置信度
     */
    private double parseConfidence(String aiResult) {
        try {
            // 简单实现，假设AI模型返回的结果中包含类似"置信度: 0.85"这样的文本
            if (aiResult.contains("置信度:")) {
                String[] parts = aiResult.split("置信度:");
                if (parts.length > 1) {
                    String confStr = parts[1].trim().split("[^0-9.]")[0];
                    return Double.parseDouble(confStr);
                }
            }
            // 默认返回一个中等置信度
            return 0.7;
        } catch (Exception e) {
            // 解析失败时返回默认值
            return 0.7;
        }
    }

    /**
     * 提取AI返回的反馈建议
     */
    private String extractFeedback(String aiResult) {
        try {
            // 简单实现，假设AI模型返回的结果中包含类似"反馈: ..."这样的文本
            if (aiResult.contains("反馈:")) {
                String[] parts = aiResult.split("反馈:");
                if (parts.length > 1) {
                    return parts[1].trim();
                }
            }
            // 如果没有找到反馈部分，返回整个结果作为反馈
            return aiResult;
        } catch (Exception e) {
            // 解析失败时返回原始结果
            return aiResult;
        }
    }

    /**
     * 获取特定提交的AI评分
     */
    public AIGrading getGradingBySubmission(Long submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("提交不存在"));
        return submission.getAiGrading();
    }
} 