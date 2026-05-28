package com.shoppilot.controller;

import com.shoppilot.common.ApiResponse;
import com.shoppilot.service.UploadService;
import com.shoppilot.vo.UploadResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private final UploadService uploadService;

    public UploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @PostMapping("/product-image")
    public ApiResponse<UploadResponse> uploadProductImage(@RequestParam("file") MultipartFile file) {
        return ApiResponse.success(uploadService.uploadProductImage(file));
    }
}
