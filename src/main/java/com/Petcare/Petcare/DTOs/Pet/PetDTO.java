package com.Petcare.Petcare.DTOs.Pet;

public class PetDTO {
    private Long id;
    private String name;
    private String type;
    private int age;
    private String owner;

    public PetDTO() {
    }

    public PetDTO(Long id, String name, String type, int age, String owner) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.age = age;
        this.owner = owner;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}

