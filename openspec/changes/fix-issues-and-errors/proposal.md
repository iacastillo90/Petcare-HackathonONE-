# Proposal: Fix Issues and Errors

## Intent

El proyecto tiene aproximadamente 30+ tests fallando en los módulos de SitterProfile, SitterWorkExperience y UserController. El objetivo es diagnosticar la causa raíz de los fallos y corregirlos para lograr que todos los tests unitarios y de integración pasen exitosamente.

## Scope

### In Scope
- Diagnosticar y corregir los tests fallando en `SitterProfileControllerTest`
- Diagnosticar y corregir los tests fallando en `SitterWorkExperienceControllerTest`
- Diagnosticar y corregir los tests fallando en `UserControllerTest`
- Verificar que el `GlobalExceptionHandler` cubra todos los casos definidos en la spec de error-handling
- Correr la suite completa de tests para confirmar que todos pasan

### Out of Scope
- Nuevas features o refactors mayores
- Cambios en la arquitectura de seguridad
- Actualización de dependencias

## Approach

1. **Diagnóstico**: Ejecutar los tests fallidos individualmente para identificar el patrón de error
2. **Análisis de causa raíz**: 
   - Verificar configuración de seguridad en los tests (`TestSecurityConfig`)
   - Revisar endpoints y permisos en los controladores
   - Confirmar que las respuestas HTTP coincidan con lo esperado
3. **Implementación**: Aplicar correcciones necesarias
4. **Verificación**: Ejecutar toda la suite de tests

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `src/test/java/.../SitterProfileControllerTest.java` | Modified | ~13 tests fallando (autorización, validación, 404) |
| `src/test/java/.../SitterWorkExperienceControllerTest.java` | Modified | ~10 tests fallando |
| `src/test/java/.../UserControllerTest.java` | Modified | ~4 tests fallando |
| `src/main/java/.../GlobalExceptionHandler.java` | Modified | Verificar cobertura de excepciones |
| `src/main/java/.../InvoiceController.java` | Modified | Errores LSP: campos no inicializados, log no resuelto |
| `src/main/java/.../DashboardController.java` | Modified | Errores LSP: campos no inicializados |
| `src/main/java/.../PetController.java` | Modified | Errores LSP: campos no inicializados, log no resuelto |
| `src/main/java/.../EmailController.java` | Modified | Errores LSP: métodos no definidos (getTo, getTrackingId, getAttachments) |
| `src/main/java/.../CouponExpiredException.java` | Modified | Errores LSP: log no resuelto |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Los tests requieren cambios en la lógica de negocio | Medium | Ejecutar tests incrementalmente |
| Tests de autorización fallan por configuración incorrecta | High | Revisar TestSecurityConfig |
| Introducir nuevos bugs al corregir | Medium | Ejecutar tests antes y después de cada cambio |

## Rollback Plan

1. Revertir cambios en archivos de test con `git checkout --`
2. Ejecutar tests para confirmar estado anterior
3. Si hay cambios en código fuente, crear backup antes de modificar

## Dependencies

- Ninguna dependencia externa

## Success Criteria

- [ ] El 100% de los tests en `SitterProfileControllerTest` pasan
- [ ] El 100% de los tests en `SitterWorkExperienceControllerTest` pasan
- [ ] El 100% de los tests en `UserControllerTest` pasan
- [ ] La suite completa de tests (`./gradlew test`) pasa sin errores
- [ ] Los mensajes de error cumplen con la spec de error-handling existente
