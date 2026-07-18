package io.github.ivannavas.sprout_data_warehouse.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 * Base of every entity the agent writes. What the agent extracts is a proposal, not a fact: it comes from a
 * local model reading a rambling transcription, so a record lands pending and only a human turns it into
 * validated data, through the validation endpoint.
 *
 * <p>{@code validated} sits here, outside each entity's {@code @AllArgsConstructor} — Lombok does not include
 * inherited fields — so the agent's tools have no way to set it: every save the agent makes leaves the record
 * pending, including an update to an already validated one, which is the point (the agent changed it, so it
 * needs looking at again). It is {@link JsonIgnore}d because the agent has no business reading it either.
 */
@MappedSuperclass
@Getter
@Setter
public abstract class ValidatableEntity {

    @JsonIgnore
    @Column(name = "validated", nullable = false)
    private boolean validated = false;
}
