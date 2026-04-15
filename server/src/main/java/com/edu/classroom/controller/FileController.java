package com.edu.classroom.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
@RestController
@RequestMapping("/api/files")
public class FileController {
  private static final Logger log = LoggerFactory.getLogger(FileController.class);

  @Value("${file.upload-dir:uploads}")
  private String uploadDir;

  @GetMapping
  public ResponseEntity<?> getFileByQuery(@RequestParam("name") String fileName, HttpServletRequest request) {
    return doGetFile(fileName, request);
  }

  @GetMapping("/{fileName:.+}")
  public ResponseEntity<?> getFile(@PathVariable String fileName, HttpServletRequest request) {
    return doGetFile(fileName, request);
  }

  private ResponseEntity<?> doGetFile(String fileName, HttpServletRequest request) {
    String uri = request.getRequestURI();
    String query = request.getQueryString();
    String rangeHeader = request.getHeader(HttpHeaders.RANGE);
    log.info("[files] request uri={}, query={}, rawName={}, range={}", uri, query, fileName, rangeHeader);

    String normalizedFileName = normalizeFileName(fileName);
    if (!StringUtils.hasText(normalizedFileName)) {
      log.warn("[files] bad request: invalid file name, rawName={}", fileName);
      return ResponseEntity.badRequest().build();
    }
    Path path = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(normalizedFileName);
    log.info("[files] resolved path={}, normalizedName={}", path, normalizedFileName);
    if (!Files.exists(path) || !Files.isRegularFile(path)) {
      log.warn("[files] file not found or not regular file: {}", path);
      return ResponseEntity.notFound().build();
    }
    try {
      String contentType = Files.probeContentType(path);
      MediaType mediaType = StringUtils.hasText(contentType) ? MediaType.parseMediaType(contentType) : MediaType.APPLICATION_OCTET_STREAM;
      Resource resource = new FileSystemResource(path.toFile());
      long contentLength = resource.contentLength();
      log.info("[files] file ready contentType={}, contentLength={}", mediaType, contentLength);

      String range = request.getHeader(HttpHeaders.RANGE);
      if (StringUtils.hasText(range)) {
        // 兼容模式：当前运行环境缺少 ResourceRegion 对应 converter，避免 400 直接回完整文件流。
        log.info("[files] range header present but using full-stream fallback, range={}", range);
      }

      log.info("[files] full response 200 length={}", contentLength);
      return ResponseEntity.ok()
        .contentType(mediaType)
        .header(HttpHeaders.ACCEPT_RANGES, "bytes")
        .contentLength(contentLength)
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + path.getFileName() + "\"")
        .body(resource);
    } catch (Exception e) {
      log.error("[files] internal error path={}, message={}", path, e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }

  private String normalizeFileName(String raw) {
    if (!StringUtils.hasText(raw)) return null;
    String name;
    try {
      name = URLDecoder.decode(raw, StandardCharsets.UTF_8);
    } catch (Exception e) {
      name = raw;
    }
    int q = name.indexOf('?');
    if (q >= 0) name = name.substring(0, q);
    int h = name.indexOf('#');
    if (h >= 0) name = name.substring(0, h);
    name = name.trim();
    // 兼容客户端误把完整路径/URL 作为文件名传入，保留最后一段
    int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
    if (slash >= 0 && slash < name.length() - 1) {
      name = name.substring(slash + 1).trim();
    }
    if (!StringUtils.hasText(name)) return null;
    if (name.contains("..") || name.contains("/") || name.contains("\\")) return null;
    return name;
  }
}

