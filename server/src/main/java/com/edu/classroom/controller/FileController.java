package com.edu.classroom.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileController {
  @Value("${file.upload-dir:uploads}")
  private String uploadDir;

  @GetMapping("/{fileName:.+}")
  public ResponseEntity<?> getFile(@PathVariable String fileName, HttpServletRequest request) {
    if (!StringUtils.hasText(fileName) || fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
      return ResponseEntity.badRequest().build();
    }
    Path path = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(fileName);
    if (!Files.exists(path) || !Files.isRegularFile(path)) {
      return ResponseEntity.notFound().build();
    }
    try {
      String contentType = Files.probeContentType(path);
      MediaType mediaType = StringUtils.hasText(contentType) ? MediaType.parseMediaType(contentType) : MediaType.APPLICATION_OCTET_STREAM;
      Resource resource = new FileSystemResource(path.toFile());
      long contentLength = resource.contentLength();

      String range = request.getHeader(HttpHeaders.RANGE);
      if (StringUtils.hasText(range)) {
        List<HttpRange> ranges = HttpRange.parseRanges(range);
        if (ranges == null || ranges.isEmpty()) {
          return ResponseEntity.status(416).build();
        }
        HttpRange r = ranges.get(0); // 小程序 video 一般只用单段
        long start = r.getRangeStart(contentLength);
        long end = r.getRangeEnd(contentLength);
        if (start >= contentLength) {
          return ResponseEntity.status(416).build();
        }
        long rangeLength = Math.min(contentLength - start, end - start + 1);
        ResourceRegion region = new ResourceRegion(resource, start, rangeLength);

        return ResponseEntity.status(206)
          .contentType(mediaType)
          .header(HttpHeaders.ACCEPT_RANGES, "bytes")
          .header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + (start + rangeLength - 1) + "/" + contentLength)
          .contentLength(rangeLength)
          .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + path.getFileName() + "\"")
          .body(region);
      }

      return ResponseEntity.ok()
        .contentType(mediaType)
        .header(HttpHeaders.ACCEPT_RANGES, "bytes")
        .contentLength(contentLength)
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + path.getFileName() + "\"")
        .body(resource);
    } catch (Exception e) {
      return ResponseEntity.internalServerError().build();
    }
  }
}

