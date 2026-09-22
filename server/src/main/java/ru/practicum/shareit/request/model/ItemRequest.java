package ru.practicum.shareit.request.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;


@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "requests")
public class ItemRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "description")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requestor_id")
    @ToString.Exclude
    private User user;

    @Column(name = "creation_date")
    private LocalDateTime creationDate = LocalDateTime.now();
}
