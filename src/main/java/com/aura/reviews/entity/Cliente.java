package com.aura.reviews.entity;

import tools.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Entidad Cliente - relación OneToOne con Review
@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotNull(message = "La edad es obligatoria")
    @Min(value = 0, message = "La edad debe ser mayor o igual a 0")
    @Column(nullable = false)
    private Integer edad;

    @NotBlank(message = "El género es obligatorio")
    @Size(max = 20, message = "El género no puede superar 20 caracteres")
    @Column(nullable = false, length = 20)
    private String genero;

    @Column(nullable = false)
    private Boolean intolerancia = false;

    @Size(max = 255, message = "El detalle de intolerancia no puede superar 255 caracteres")
    @Column(length = 255)
    private String detalleIntolerancia;

    @JsonIgnore
    @OneToOne(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    private Review review;

    public Cliente() {
        super();
    }

    public Cliente(String nombre, Integer edad, String genero, Boolean intolerancia, String detalleIntolerancia) {
        this.nombre = nombre;
        this.edad = edad;
        this.genero = genero;
        this.intolerancia = intolerancia;
        this.detalleIntolerancia = detalleIntolerancia;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getEdad() {
        return edad;
    }

    public void setEdad(Integer edad) {
        this.edad = edad;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public Boolean getIntolerancia() {
        return intolerancia;
    }

    public void setIntolerancia(Boolean intolerancia) {
        this.intolerancia = intolerancia;
    }

    public String getDetalleIntolerancia() {
        return detalleIntolerancia;
    }

    public void setDetalleIntolerancia(String detalleIntolerancia) {
        this.detalleIntolerancia = detalleIntolerancia;
    }

    public Review getReview() {
        return review;
    }

    public void setReview(Review review) {
        this.review = review;
    }
}
