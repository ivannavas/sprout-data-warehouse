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
@Table(name = "dated_feelings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DatedFeeling extends ValidatableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "feeling_id")
    private Long feelingId;

    @Column(name = "date")
    @JsonSerialize(using = ToStringSerializer.class)
    private LocalDate date;

    @Column(name = "description", columnDefinition = "text")
    private String description;
}
