package com.Petcare.Petcare.Models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String species;

    private int age;

    private String gender;

    private String color;
}
