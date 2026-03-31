# Design: Fix Issues and Errors

## Technical Approach

Este cambio tiene como objetivo diagnosticar y corregir ~42 tests fallando en tres clases de test: `SitterProfileControllerTest`, `SitterWorkExperienceControllerTest`, y `UserControllerTest`. La causa raíz principal identificada es la inconsistencia entre la configuración de seguridad en los tests y el comportamiento esperado por los tests.

**Estrategia:**
1. Ejecutar tests individualmente para confirmar patrones de fallo
2. Analizar la configuración de seguridad (`TestSecurityConfig`) vs expectativas de los tests
3. Corregir la configuración de seguridad para que refleje el comportamiento real de producción
4. Verificar que todos los tests pasen

## Architecture Decisions

### Decision: TestSecurityConfig Authorization Mode

**Choice**: Modificar `TestSecurityConfig` para utilizar `anyRequest().authenticated()` en lugar de `anyRequest().permitAll()`, permitiendo testing de autorización real.

**Alternatives considered**: 
- Crear una configuración separada para cada rol de test
- Mockear el SecurityFilterChain en cada test
- Usar `@WithMockUser` de Spring Security

**Rationale**: 
- La configuración actual (`permitAll()`) deshabilita completamente la seguridad, haciendo imposible probar autorización
- Los tests esperan que Spring Security responda con 403 para accesos no autorizados
- Mantener una única configuración consistente es más mantenible que crear configuraciones por test

### Decision: Test Data Setup Approach

**Choice**: Utilizar el método helper `getAuthTokenForRole(Role role)` existente para crear usuarios con roles específicos y generar tokens JWT válidos.

**Alternatives considered**:
- Crear usuarios de prueba en `@BeforeAll`
- Usar `@WithMockUser` de Spring Security

**Rationale**: 
- El método ya existe y funciona correctamente
- Genera tokens JWT reales que funcionan con el filtro de seguridad
- Mantiene consistencia con el patrón existente en el proyecto

### Decision: Error Response Format

**Choice**: Estandarizar que todas las respuestas de error de autorización sigan el formato `ErrorResponseDTO` con status 403.

**Alternatives considered**:
- Permitir diferentes formatos según el tipo de error
- Usar excepciones de Spring Security directamente

**Rationale**: 
- El proyecto ya tiene un `GlobalExceptionHandler` que formatea errores consistentemente
- La spec de error-handling define este formato
- Facilita el testing assertions

## Data Flow

```
Test Execution Flow:
===================

1. Test Setup (@BeforeEach)
   ├── Create test users (CLIENT, SITTER, ADMIN)
   ├── Save to H2 database
   └── Generate JWT tokens via JwtService

2. Test Execution
   ├── Send HTTP request with Bearer token
   ├── JwtAuthenticationFilter validates token
   ├── Authorization check (roles/permissions)
   ├── Controller processes request
   └── Response returned

3. Assertion
   └── Verify response status (200, 403, 404, etc.)
```

## File Changes

| File | Action | Description |
|------|--------|-------------|
| `src/test/java/.../Config/TestSecurityConfig.java` | Modify | Cambiar `permitAll()` a `authenticated()` para habilitar testing de autorización |
| `src/test/java/.../Controllers/SitterProfileControllerTest.java` | Analyze | Verificar que tests de autorización pasen con nueva config |
| `src/test/java/.../Controllers/SitterWorkExperienceControllerTest.java` | Analyze | Verificar que tests de autorización pasen con nueva config |
| `src/test/java/.../Controllers/UserControllerTest.java` | Analyze | Verificar que tests de autorización pasen con nueva config |
| `src/main/java/.../GlobalExceptionHandler.java` | Verify | Confirmar cobertura de excepciones según spec |

## Interfaces / Contracts

### TestSecurityConfig Contract

```java
@TestConfiguration
public class TestSecurityConfig {
    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated()  // CAMBIAR de permitAll()
            )
            .build();
    }
}
```

### Error Response Contract (per error-handling spec)

```json
{
  "status": 403,
  "message": "Acceso denegado: no tienes permiso para este recurso",
  "timestamp": "2025-01-15T10:30:00",
  "validationErrors": null
}
```

## Testing Strategy

| Layer | What to Test | Approach |
|-------|-------------|----------|
| Integration | SitterProfileController authorization | Ejecutar `./gradlew test --tests "SitterProfileControllerTest"` |
| Integration | SitterWorkExperienceController authorization | Ejecutar `./gradlew test --tests "SitterWorkExperienceControllerTest"` |
| Integration | UserController authorization | Ejecutar `./gradlew test --tests "UserControllerTest"` |
| Integration | Full test suite | Ejecutar `./gradlew test` |

### Test Execution Order

```bash
# Phase 1: Run individual test classes
./gradlew test --tests "com.Petcare.Petcare.Controllers.SitterProfileControllerTest"
./gradlew test --tests "com.Petcare.Petcare.Controllers.SitterWorkExperienceControllerTest"
./gradlew test --tests "com.Petcare.Petcare.Controllers.UserControllerTest"

# Phase 2: Run full test suite
./gradlew test
```

## Migration / Rollout

No migration required. This is a test-only change that does not affect production code.

**Rollback Plan:**
```bash
git checkout -- src/test/java/com/Petcare/Petcare/Config/TestSecurityConfig.java
```

## Open Questions

- [ ] **LSP Errors in Controllers**: Los controllers mencionados (InvoiceController, DashboardController, PetController, EmailController, CouponExpiredException) tienen errores LSP que necesitan investigarse. ¿Son errores de compilación reales o false positives del IDE?

- [ ] **Security Config Interaction**: Al cambiar de `permitAll()` a `authenticated()`, ¿se requiere también configurar JWT validation en los tests? El JwtAuthenticationFilter debe manejar tokens correctamente.

- [ ] **Admin Endpoints**: Los tests de endpoints `/api/users/admin` ¿necesitan configuración adicional de CORS o headers?

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Tests de autorización siguen fallando después del cambio | Medium | Verificar JwtAuthenticationFilter está correctamente configurado |
| Tests de integración rotos por cambio de seguridad | Low | Ejecutar tests incrementalmente |
| Validación DTO no funciona en contexto de test | Low | Verificar que MockMvc tiene configuración de validación correcta |

---

## Root Cause Summary

| Category | Affected Tests | Root Cause |
|----------|---------------|------------|
| Authorization (403) | ~35 tests | `TestSecurityConfig` usa `permitAll()` que deshabilita seguridad |
| Validation (400) | ~3 tests | DTO validation puede no estar configurado en MockMvc |
| Not Found (404) | ~4 tests | Datos de prueba no incluyen IDs correctos |

---

**Design Version**: 1.0
**Status**: Ready for Tasks Phase (sdd-tasks)
