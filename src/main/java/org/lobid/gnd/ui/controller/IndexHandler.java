package org.lobid.gnd.ui.controller;

import static org.lobid.gnd.ui.controller.ErrorHandler.errorResponse;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class IndexHandler {

    public Mono<ServerResponse> page(ServerRequest request) {
        try {
            Map<String, Object> model =
                    Map.of("dataset", dataset(), "request", request.attributes());
            return ServerResponse.ok().render("index", model);
        } catch (Exception e) {
            return errorResponse(request, 500, "Failed to load index page: " + e.getMessage());
        }
    }

    private Map<String, Object> dataset() throws IOException {
        InputStream dataset = new ClassPathResource("static/dataset.jsonld").getInputStream();
        return new ObjectMapper().readValue(dataset, new TypeReference<>() {});
    }
}
