package io.github.ivannavas.sprout_data_warehouse.ingestor;

import io.github.ivannavas.sprout.event.ModelResponseEvent;
import io.github.ivannavas.sprout.model.TokenUsage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Adds up what the model cost over an ingestion run, so a pass reports the tokens it burned next to
 * the days it processed. Sprout publishes a {@link ModelResponseEvent} per model call and the Spring
 * bridge republishes it as an application event, so this observes the agent loop without being wired
 * into it.
 *
 * <p>The figure worth watching is the cached share. Every iteration of the extraction agent resends
 * the system prompt, the tool schemas and the conversation so far; with prompt caching those arrive
 * as cache reads at a fraction of the price, and without it they are billed in full every time. A run
 * whose ratio sits near zero is paying full price for the same tokens over and over — the cause is
 * almost always something varying inside the prompt prefix rather than caching being off.
 */
@Component
@Slf4j
public class ModelUsageTracker {

    private final AtomicReference<TokenUsage> total = new AtomicReference<>(TokenUsage.ZERO);

    @EventListener
    public void onModelResponse(ModelResponseEvent event) {
        total.accumulateAndGet(event.response().usage(), TokenUsage::plus);
    }

    /** Drops the running total. Called when a pass starts so its report covers that pass alone. */
    public void reset() {
        total.set(TokenUsage.ZERO);
    }

    public TokenUsage total() {
        return total.get();
    }

    /** One-line summary of the tokens a pass consumed, and how much of the prompt came from cache. */
    public String summary() {
        TokenUsage usage = total.get();
        return String.format(
                "%,d prompt tokens (%,d fresh, %,d cache write, %,d cache read — %.0f%% cached), %,d output",
                usage.totalInputTokens(), usage.inputTokens(), usage.cacheWriteTokens(),
                usage.cacheReadTokens(), usage.cacheHitRatio() * 100, usage.outputTokens());
    }
}
