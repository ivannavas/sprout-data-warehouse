package io.github.ivannavas.sprout_data_warehouse.dto;

/** The agent's answer, and the id to send back to keep asking within the same conversation. */
public record ChatResponse(String conversationId, String response, int iterations) {
}
