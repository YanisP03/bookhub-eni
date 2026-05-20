package com.example.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.UUID;

@Tag(name = "Upload", description = "Upload de fichiers (couvertures de livres)")
@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Operation(summary = "Uploader une image de couverture", description = "Formats acceptés : JPEG, PNG, WebP. Taille max : 5 Mo. Réservé bibliothécaire/admin.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "URL de l'image uploadée"),
        @ApiResponse(responseCode = "400", description = "Fichier non valide (pas une image)")
    })
    @PostMapping("/couverture")
    public ResponseEntity<Map<String, String>> uploadCouverture(@RequestParam("file") MultipartFile file,
                                                                HttpServletRequest request) throws IOException {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/"))
            return ResponseEntity.badRequest().body(Map.of("error", "Seules les images sont acceptées (JPEG, PNG, WebP…)."));

        Path dir = Paths.get(uploadDir);
        if (!Files.exists(dir)) Files.createDirectories(dir);

        String original = file.getOriginalFilename();
        String ext = (original != null && original.contains("."))
                ? original.substring(original.lastIndexOf('.'))
                : ".jpg";
        String filename = UUID.randomUUID() + ext;

        Files.copy(file.getInputStream(), dir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);

        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        String url = baseUrl + "/uploads/couvertures/" + filename;
        return ResponseEntity.ok(Map.of("url", url));
    }
}
