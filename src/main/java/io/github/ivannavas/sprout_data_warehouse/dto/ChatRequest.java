package io.github.ivannavas.sprout_data_warehouse.dto;

/**
 * A question for the conversational agent. {@code conversationId} is optional: sending the one from a
 * previous reply continues that conversation, leaving it out starts a new one.
 */
public record ChatRequest(String conversationId, String message) {
}
