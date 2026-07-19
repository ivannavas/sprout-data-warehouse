package io.github.ivannavas.sprout_data_warehouse.controller;

import io.github.ivannavas.sprout.model.AgentResult;
import io.github.ivannavas.sprout_data_warehouse.agent.ConversationalAgent;
import io.github.ivannavas.sprout_data_warehouse.dto.ChatRequest;
import io.github.ivannavas.sprout_data_warehouse.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ConversationalAgent conversationalAgent;

    /**
     * Answers one question. The reply carries the conversation id back — minted here when the caller
     * sends none — because the agent's history is keyed by it: send it again on the next question and
     * the exchange continues, omit it and each question is answered cold.
     */
    @PostMapping
    public ResponseEntity<ChatResponse> ask(@RequestBody ChatRequest request) {
        if (request.message() == null || request.message().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        String conversationId = request.conversationId() == null || request.conversationId().isBlank()
                ? UUID.randomUUID().toString()
                : request.conversationId();

        AgentResult result = conversationalAgent.execute(conversationId, request.message().trim());
        return ResponseEntity.ok(new ChatResponse(result.conversationId(), result.response(), result.iterations()));
    }
}
