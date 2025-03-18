package org.sustech.orion.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.http.HttpStatus;
import org.sustech.orion.exception.ApiException;

public class FileSizeUtil {
    private static final OkHttpClient client = new OkHttpClient();

    // 每次查询需要300-400ms，所以不要频繁调用
    public static long getFileSize(String fileUrl) {
        Request request = new Request.Builder()
                .url(fileUrl)
                .head() // 发送 HEAD 请求
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String contentLength = response.header("Content-Length");
                if (contentLength != null) {
                    return Long.parseLong(contentLength);
                }
            }
            throw new ApiException("Failed to get file size from " + fileUrl, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            throw new ApiException("Failed to get file size from " + fileUrl, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public static String formatFileSize(Long size) {
        if (size == null) {
            return "0B";
        }
        if (size < 0) {
            throw new ApiException("File size cannot be negative", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        String[] units = {"B", "KB", "MB", "GB", "TB"};
        double base = 1024;

        int unitIndex = 0;
        double filesize = size;
        while (filesize >= base && unitIndex < units.length - 1) {
            filesize /= base;
            unitIndex++;
        }

        return String.format("%.2f%s", filesize, units[unitIndex]);
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        long fileSize = getFileSize("https://www.sustech.edu.cn/uploads/files/2023/12/18171801_17593.pdf");
        long end = System.currentTimeMillis();
        System.out.println("File size: " + fileSize);
        System.out.println("Formatted file size: " + formatFileSize(fileSize));
        System.out.println("Time: " + (end - start) + "ms");
    }
}
