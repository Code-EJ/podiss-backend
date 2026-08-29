package br.com.codejr.podiss.backend.post;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
class PostRequest {
    private String title;
    private String description;
    private List<String> tags;
    private MultipartFile image;
}