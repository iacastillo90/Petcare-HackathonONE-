# 🐺 Plataforma Petcare - Hackathon Alura & Oracle ONE

API REST desarrollada en Java y Spring Boot como parte del Hackathon de la especialización backend de Alura Latam y Oracle (ONE). El proyecto simula el backend para una plataforma que conecta a dueños de mascotas con cuidadores para la reserva de servicios.

---

## 🎯 Objetivo del Proyecto

El sistema debe gestionar las funcionalidades clave para la reserva de servicios, el registro de cuidadores y la administración de la plataforma, asegurando la integridad de los datos y la lógica de negocio.

---

## ✨ Funcionalidades Principales

* **Reserva de Servicios:** Permite a los dueños de mascotas agendar paseos y cuidados.
* **Registro de Cuidadores:** Permite a los cuidadores ofrecer sus servicios y gestionar su disponibilidad.
* **Gestión y Seguridad:** Provee endpoints para la administración de la plataforma y la protección de datos de los usuarios.

---

## 👥 Tipos de Usuario

El sistema está diseñado para interactuar con tres roles principales:

1.  **Dueños de Mascotas:** Consumen la API para buscar y reservar servicios.
2.  **Cuidadores (Sitters):** Utilizan la API para registrarse, definir sus servicios y gestionar las reservas recibidas.
3.  **Administradores:** Acceden a endpoints específicos para supervisar la calidad del servicio y gestionar la plataforma.

---

## 🛠️ Stack Tecnológico

* **Backend:** Java 21, Spring Boot 3.5.4
* **Base de Datos:** Test: H2, MySQL
* **Seguridad:** Spring Security
* **Validaciones:** Jakarta Bean Validation

---

## 📖 User Stories

* **Como dueño,** quiero poder reservar paseos y cuidados para mi mascota.
* **Como cuidador,** quiero poder ofrecer mis servicios y generar ingresos.
* **Como administrador,** quiero poder velar por la calidad del servicio.
