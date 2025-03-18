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
        if (size < 1024) {
            return size + "B";
        } else if (size < 1024 * 1024) {
            Double kb = size / 1024.0;
            return String.format("%.2f", kb) + "KB";
        } else if (size < 1024 * 1024 * 1024) {
            Double mb = size / 1024.0 / 1024;
            return String.format("%.2f", mb) + "MB";
        } else {
            Double gb = size / 1024.0 / 1024 / 1024;
            return String.format("%.2f", gb) + "GB";
        }
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
