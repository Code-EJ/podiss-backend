package br.com.codejr.podiss.backend.video;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class VideoService {
    @Autowired
    private VideoRepository repository;

    public List<Video> getAllEpisodes() {
        return repository.findAll();
    }

    public Video getEpisodeByYouTubeId(String youtubeId) {
        return repository.findByVideoUrlContaining(youtubeId).orElse(null);
    }

    public Video createEpisode(String videoUrl) {
        Video video = new Video();
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("python3", "youtube-scrapp.py", videoUrl);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder jsonOutput = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonOutput.append(line);
            }

            process.waitFor();

            ObjectMapper objectMapper = new ObjectMapper();
            
            @SuppressWarnings("unchecked")
            Map<String, String> videoData = objectMapper.readValue(jsonOutput.toString(), Map.class);

            video.setTitle(videoData.get("title"));
            video.setDescription(videoData.get("description"));
            video.setVideoUrl(videoUrl);
            video.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));

            return repository.save(video);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean deleteEpisode(UUID id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

}
