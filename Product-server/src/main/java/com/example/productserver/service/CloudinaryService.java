package com.example.productserver.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(
            @Value("${cloudinary.cloud_name}") String cloudName,
            @Value("${cloudinary.api_key}") String apiKey,
            @Value("${cloudinary.api_secret}") String apiSecret) {

        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey);
        config.put("api_secret", apiSecret);
        cloudinary = new Cloudinary(config);
    }

    // الدالة السابقة (ترفع صورة واحدة)
    public String uploadImage(MultipartFile file) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
        return uploadResult.get("url").toString();
    }

    // 🔥 الدالة الجديدة (ترفع عدة صور معاً وتلخص الروابط)
    public List<String> uploadMultipleImages(MultipartFile[] files) throws IOException {
        List<String> imageUrls = new ArrayList<>();

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String url = uploadImage(file); // نستخدم الدالة الأولى لرفع كل صورة
                imageUrls.add(url); // نضيف الرابط للقائمة
            }
        }
        return imageUrls; // نرجع قائمة الروابط جاهزة!
    }
}