package io.github.ivannavas.sprout_data_warehouse.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * A group of people I talk about as a unit: a family, a team at work, a circle of friends. Its members are
 * held as plain {@code Person} ids in a side table, matching how the rest of the model refers across entities
 * (see {@code PersonEvent.personId}) rather than mapping a JPA association.
 */
@Entity
@Table(name = "person_groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Group extends ValidatableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "person_group_members", joinColumns = @JoinColumn(name = "group_id"))
    @Column(name = "person_id")
    private List<Long> memberIds = new ArrayList<>();
}
