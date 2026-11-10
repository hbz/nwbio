package org.lobid.gnd.ui.controller;

import static org.lobid.gnd.ui.controller.ErrorHandler.apiErrorResponse;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class IndexHandler {

    public Mono<ServerResponse> page(ServerRequest request) {
        try {
            return ServerResponse.ok()
                    .render("index", Map.of("dataset", dataset(), "request", request.attributes()))
                    .onErrorResume(
                            WebClientResponseException.class, e -> apiErrorResponse(request, e));
        } catch (IOException e) {
            return Mono.error(e);
        }
    }

    private Map<String, Object> dataset() throws IOException {
        InputStream dataset = new ClassPathResource("static/dataset.jsonld").getInputStream();
        return new ObjectMapper().readValue(dataset, new TypeReference<>() {});
    }
}
