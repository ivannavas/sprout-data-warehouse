package io.github.ivannavas.sprout_data_warehouse.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "day_summaries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DaySummary extends ValidatableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "date", unique = true)
    @JsonSerialize(using = ToStringSerializer.class)
    private LocalDate date;

    @Column(name = "summary", columnDefinition = "text")
    private String summary;
}
