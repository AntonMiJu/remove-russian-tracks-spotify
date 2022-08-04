package com.removerussiantracksspotify;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MvcController {
    private final ObjectMapper objectMapper;

    public MvcController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @GetMapping("/")
    public String preview() {
        return "preview";
    }

    @GetMapping("/callback")
    public String callback() {
        return "callback";
    }

    @GetMapping("/home")
    public String home(@RequestParam String token, Model model)
            throws URISyntaxException, IOException, InterruptedException {
        HashMap<String, String> likedArtists = new HashMap<>();
        int counter = 0;
        int total;
        do {
            HttpRequest request =
                    HttpRequest.newBuilder().uri(new URI("https://api.spotify.com/v1/me/tracks?limit=50&offset=" + counter))
                            .setHeader("Authorization", token).build();

            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            JsonNode node = objectMapper.readValue(response.body(), JsonNode.class);
            total = node.get("total").asInt();
            node.findValues("track").forEach(track -> {
                track.get("artists").iterator().forEachRemaining(artist -> {
                    
                    likedArtists.put(artist.get("id").textValue(), artist.get("name").textValue());
                });
            });
            counter+=50;
        } while (counter <= total);
        model.addAttribute("likedArtists", likedArtists);
        return "home";
    }
}
