package com.code.yolanda.back.video;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/episodes")
public class VideoController {

    @Autowired
    private VideoService service;

    @GetMapping("")
    public ResponseEntity<List<Video>> getAllEpisodes() {
        List<Video> episodes = service.getAllEpisodes();
        return ResponseEntity.ok(episodes);
    }

    @GetMapping("/{youtubeId}")
    public ResponseEntity<Video> getEpisodeByYouTubeId(@PathVariable String youtubeId) {
        Video episode = service.getEpisodeByYouTubeId(youtubeId);
        return episode != null ? ResponseEntity.ok(episode) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Video> createEpisode(@RequestBody VideoUrlRequest videoUrlRequest) {
        Video episode = service.createEpisode(videoUrlRequest.getVideoUrl());
        if (episode != null) {
            return ResponseEntity.ok(episode);
        } else {
            return ResponseEntity.status(500).build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteEpisode(@PathVariable UUID id) {
        boolean isDeleted = service.deleteEpisode(id);
        return isDeleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}

