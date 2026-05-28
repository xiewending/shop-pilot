package com.shoppilot.service;

import com.shoppilot.common.BusinessException;
import com.shoppilot.config.UploadProperties;
import com.shoppilot.vo.UploadResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
public class UploadService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");

    private final UploadProperties uploadProperties;

    public UploadService(UploadProperties uploadProperties) {
        this.uploadProperties = uploadProperties;
    }

    public UploadResponse uploadProductImage(MultipartFile file) {
        validateImage(file);

        String extension = Objects.requireNonNull(StringUtils.getFilenameExtension(file.getOriginalFilename()))
                .toLowerCase(Locale.ROOT);
        String datePath = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String fileName = UUID.randomUUID() + "." + extension;

        Path uploadRoot = Paths.get(uploadProperties.getBaseDir()).toAbsolutePath().normalize();
        Path targetDir = uploadRoot.resolve("product").resolve(datePath).normalize();
        Path targetFile = targetDir.resolve(fileName).normalize();
        if (!targetFile.startsWith(uploadRoot)) {
            throw new BusinessException(400, "文件路径无效");
        }

        try {
            Files.createDirectories(targetDir);
            file.transferTo(targetFile);
        } catch (IOException exception) {
            throw new BusinessException(500, "图片保存失败");
        }

        String publicPath = uploadProperties.getPublicPath().endsWith("/")
                ? uploadProperties.getPublicPath()
                : uploadProperties.getPublicPath() + "/";
        return new UploadResponse(publicPath + "product/" + datePath + "/" + fileName);
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请选择要上传的图片");
        }
        if (file.getSize() > uploadProperties.getMaxSize()) {
            throw new BusinessException(400, "图片大小不能超过 2MB");
        }
        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new BusinessException(400, "仅支持 JPG、PNG、WEBP、GIF 图片");
        }
        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        if (!StringUtils.hasText(extension) || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase(Locale.ROOT))) {
            throw new BusinessException(400, "图片文件类型不支持");
        }
    }
}
