package com.Petcare.Petcare.Service;

import com.Petcare.Petcare.DTOs.Pet.CreatePetRequest;
import com.Petcare.Petcare.DTOs.Pet.PetResponse;
import com.Petcare.Petcare.DTOs.Pet.PetSummaryResponse;
import com.Petcare.Petcare.Exception.Business.AccountNotFoundException;
import com.Petcare.Petcare.Exception.Business.InactiveAccountException;
import com.Petcare.Petcare.Exception.Business.PetAlreadyExistsException;
import com.Petcare.Petcare.Exception.Business.PetNotFoundException;
import com.Petcare.Petcare.Models.Account.Account;
import com.Petcare.Petcare.Models.Pet;
import com.Petcare.Petcare.Models.User.Role;
import com.Petcare.Petcare.Models.User.User;
import com.Petcare.Petcare.Repositories.AccountRepository;
import com.Petcare.Petcare.Repositories.PetRepository;
import com.Petcare.Petcare.Repositories.UserRepository;
import com.Petcare.Petcare.Services.Implement.PetServiceImplement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Suite completa de pruebas unitarias para {@link PetServiceImplement}.
 *
 * <p>Esta clase verifica la lógica de negocio del servicio de mascotas,
 * incluyendo operaciones CRUD, validaciones de negocio, control de acceso
 * y gestión de estados.</p>
 *
 * <p><strong>Cobertura de pruebas:</strong></p>
 * <ul>
 *   <li>Creación de mascotas con validaciones</li>
 *   <li>Consulta por ID, cuenta y filtros</li>
 *   <li>Actualización y eliminación</li>
 *   <li>Gestión de estado activo/inactivo</li>
 *   <li>Búsquedas de texto</li>
 *   <li>Validaciones de negocio</li>
 * </ul>
 *
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 1.0
 * @see PetServiceImplement
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias: PetServiceImplement")
class PetServiceImplementTest {

    // ========== DEPENDENCIAS MOCKEADAS ==========
    @Mock
    private PetRepository petRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    // ========== SISTEMA BAJO PRUEBA ==========
    @InjectMocks
    private PetServiceImplement petService;

    // ========== DATOS DE PRUEBA ==========
    private Account testAccount;
    private Pet testPet;
    private CreatePetRequest createPetRequest;
    private User testUser;

    private static final Long VALID_ACCOUNT_ID = 1L;
    private static final Long VALID_PET_ID = 1L;
    private static final Long NONEXISTENT_PET_ID = 999L;
    private static final String VALID_PET_NAME = "Firulais";
    private static final String VALID_SPECIES = "Perro";
    private static final String VALID_BREED = "Labrador";
    private static final Integer VALID_AGE = 5;
    private static final String VALID_GENDER = "Macho";
    private static final String VALID_COLOR = "Marrón";
    private static final String TEST_USER_EMAIL = "admin@test.com";

    /**
     * Configuración inicial antes de cada prueba.
     * Prepara los objetos de prueba con datos válidos.
     */
    @BeforeEach
    void setUp() {
        // Usuario de prueba
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail(TEST_USER_EMAIL);
        testUser.setRole(Role.ADMIN);

        // Cuenta de prueba
        testAccount = new Account();
        testAccount.setId(VALID_ACCOUNT_ID);
        testAccount.setAccountName("Cuenta Familia Pérez");
        testAccount.setAccountNumber("ACC-001");
        testAccount.setActive(true);
        testAccount.setOwnerUser(testUser);

        // Mascota de prueba
        testPet = new Pet();
        testPet.setId(VALID_PET_ID);
        testPet.setName(VALID_PET_NAME);
        testPet.setSpecies(VALID_SPECIES);
        testPet.setBreed(VALID_BREED);
        testPet.setAge(VALID_AGE);
        testPet.setGender(VALID_GENDER);
        testPet.setColor(VALID_COLOR);
        testPet.setAccount(testAccount);
        testPet.setActive(true);
        testPet.setWeight(BigDecimal.valueOf(15.5));
        testPet.setCreatedAt(LocalDateTime.now());
        testPet.setUpdatedAt(LocalDateTime.now());

        // Request para crear mascota
        createPetRequest = new CreatePetRequest();
        createPetRequest.setName(VALID_PET_NAME);
        createPetRequest.setSpecies(VALID_SPECIES);
        createPetRequest.setBreed(VALID_BREED);
        createPetRequest.setAge(VALID_AGE);
        createPetRequest.setGender(VALID_GENDER);
        createPetRequest.setColor(VALID_COLOR);
        createPetRequest.setAccountId(VALID_ACCOUNT_ID);
    }

    /**
     * Limpia el contexto de seguridad después de cada prueba.
     */
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Configura el contexto de seguridad como ADMIN para pruebas.
     */
    private void setupAdminSecurityContext() {
        // Crear authorities con ROLE_ADMIN para que isAdmin() retorne true
        List<org.springframework.security.core.authority.SimpleGrantedAuthority> authorities = 
                List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"));
        
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                TEST_USER_EMAIL, null, authorities
        );
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    // ========== TESTS: CREATE PET ==========

    @Nested
    @DisplayName("createPet")
    class CreatePetTests {

        @Test
        @DisplayName("createPet | Éxito | Debería crear mascota cuando la cuenta existe y está activa")
        void createPet_WhenAccountExistsAndActive_ShouldCreatePet() {
            // Given
            when(accountRepository.findById(VALID_ACCOUNT_ID)).thenReturn(Optional.of(testAccount));
            when(petRepository.existsByNameIgnoreCaseAndAccountId(VALID_PET_NAME, VALID_ACCOUNT_ID))
                    .thenReturn(false);
            when(petRepository.save(any(Pet.class))).thenAnswer(invocation -> {
                Pet pet = invocation.getArgument(0);
                pet.setId(VALID_PET_ID);
                return pet;
            });

            // When
            PetResponse response = petService.createPet(createPetRequest);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.name()).isEqualTo(VALID_PET_NAME);
            assertThat(response.species()).isEqualTo(VALID_SPECIES);
            assertThat(response.breed()).isEqualTo(VALID_BREED);

            verify(accountRepository).findById(VALID_ACCOUNT_ID);
            verify(petRepository).save(any(Pet.class));
        }

        @Test
        @DisplayName("createPet | Falla | Debería lanzar excepción si la cuenta no existe")
        void createPet_WhenAccountNotFound_ShouldThrowException() {
            // Given
            when(accountRepository.findById(VALID_ACCOUNT_ID)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> petService.createPet(createPetRequest))
                    .isInstanceOf(AccountNotFoundException.class)
                    .hasMessageContaining("Cuenta no encontrada con ID: " + VALID_ACCOUNT_ID);

            verify(accountRepository).findById(VALID_ACCOUNT_ID);
            verify(petRepository, never()).save(any());
        }

        @Test
        @DisplayName("createPet | Falla | Debería lanzar excepción si la cuenta está inactiva")
        void createPet_WhenAccountIsInactive_ShouldThrowException() {
            // Given
            testAccount.setActive(false);
            when(accountRepository.findById(VALID_ACCOUNT_ID)).thenReturn(Optional.of(testAccount));

            // When/Then
            assertThatThrownBy(() -> petService.createPet(createPetRequest))
                    .isInstanceOf(InactiveAccountException.class);

            verify(petRepository, never()).save(any());
        }

        @Test
        @DisplayName("createPet | Falla | Debería lanzar excepción si el nombre ya existe en la cuenta")
        void createPet_WhenPetNameAlreadyExists_ShouldThrowException() {
            // Given
            when(accountRepository.findById(VALID_ACCOUNT_ID)).thenReturn(Optional.of(testAccount));
            when(petRepository.existsByNameIgnoreCaseAndAccountId(VALID_PET_NAME, VALID_ACCOUNT_ID))
                    .thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> petService.createPet(createPetRequest))
                    .isInstanceOf(PetAlreadyExistsException.class)
                    .hasMessageContaining("Ya existe una mascota con el nombre '" + VALID_PET_NAME + "'");

            verify(petRepository, never()).save(any());
        }
    }

    // ========== TESTS: GET PET BY ID ==========

    @Nested
    @DisplayName("getPetById")
    class GetPetByIdTests {

        @Test
        @DisplayName("getPetById | Éxito | Debería retornar la mascota cuando existe")
        void getPetById_WhenPetExists_ShouldReturnPet() {
            // Given
            setupAdminSecurityContext();
            when(petRepository.findById(VALID_PET_ID)).thenReturn(Optional.of(testPet));

            // When
            PetResponse response = petService.getPetById(VALID_PET_ID);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(VALID_PET_ID);
            assertThat(response.name()).isEqualTo(VALID_PET_NAME);

            verify(petRepository).findById(VALID_PET_ID);
        }

        @Test
        @DisplayName("getPetById | Falla | Debería lanzar excepción cuando la mascota no existe")
        void getPetById_WhenPetNotFound_ShouldThrowException() {
            // Given
            when(petRepository.findById(NONEXISTENT_PET_ID)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> petService.getPetById(NONEXISTENT_PET_ID))
                    .isInstanceOf(PetNotFoundException.class)
                    .hasMessageContaining("Mascota no encontrada con ID: " + NONEXISTENT_PET_ID);

            verify(petRepository).findById(NONEXISTENT_PET_ID);
        }
    }

    // ========== TESTS: GET ALL PETS ==========

    @Nested
    @DisplayName("getAllPets")
    class GetAllPetsTests {

        @Test
        @DisplayName("getAllPets | Éxito | Debería retornar lista de mascotas")
        void getAllPets_ShouldReturnPetList() {
            // Given
            setupAdminSecurityContext();
            Pet pet2 = new Pet();
            pet2.setId(2L);
            pet2.setName("Michi");
            pet2.setSpecies("Gato");
            pet2.setAccount(testAccount);
            pet2.setActive(true);

            when(petRepository.findAll()).thenReturn(List.of(testPet, pet2));

            // When
            List<PetResponse> response = petService.getAllPets();

            // Then
            assertThat(response).isNotNull();
            assertThat(response).hasSize(2);

            verify(petRepository).findAll();
        }

        @Test
        @DisplayName("getAllPets | Éxito | Debería retornar lista vacía cuando no hay mascotas")
        void getAllPets_WhenNoPets_ShouldReturnEmptyList() {
            // Given
            setupAdminSecurityContext();
            when(petRepository.findAll()).thenReturn(Collections.emptyList());

            // When
            List<PetResponse> response = petService.getAllPets();

            // Then
            assertThat(response).isNotNull();
            assertThat(response).isEmpty();

            verify(petRepository).findAll();
        }
    }

    // ========== TESTS: GET PETS BY ACCOUNT ID ==========

    @Nested
    @DisplayName("getPetsByAccountId")
    class GetPetsByAccountIdTests {

        @Test
        @DisplayName("getPetsByAccountId | Éxito | Debería retornar mascotas de la cuenta")
        void getPetsByAccountId_ShouldReturnPetsForAccount() {
            // Given
            setupAdminSecurityContext();
            when(petRepository.findByAccountId(VALID_ACCOUNT_ID)).thenReturn(List.of(testPet));

            // When
            List<PetResponse> response = petService.getPetsByAccountId(VALID_ACCOUNT_ID);

            // Then
            assertThat(response).isNotNull();
            assertThat(response).hasSize(1);
            assertThat(response.get(0).name()).isEqualTo(VALID_PET_NAME);

            verify(petRepository).findByAccountId(VALID_ACCOUNT_ID);
        }

        @Test
        @DisplayName("getPetsByAccountId | Éxito | Debería retornar lista vacía si no hay mascotas")
        void getPetsByAccountId_WhenNoPets_ShouldReturnEmptyList() {
            // Given
            setupAdminSecurityContext();
            when(petRepository.findByAccountId(VALID_ACCOUNT_ID)).thenReturn(Collections.emptyList());

            // When
            List<PetResponse> response = petService.getPetsByAccountId(VALID_ACCOUNT_ID);

            // Then
            assertThat(response).isNotNull();
            assertThat(response).isEmpty();
        }
    }

    // ========== TESTS: GET PETS BY SPECIES ==========

    @Nested
    @DisplayName("getPetsBySpecies")
    class GetPetsBySpeciesTests {

        @Test
        @DisplayName("getPetsBySpecies | Éxito | Debería filtrar mascotas por especie")
        void getPetsBySpecies_ShouldReturnFilteredPets() {
            // Given
            setupAdminSecurityContext();
            when(petRepository.findBySpeciesIgnoreCase(VALID_SPECIES)).thenReturn(List.of(testPet));

            // When
            List<PetResponse> response = petService.getPetsBySpecies(VALID_SPECIES);

            // Then
            assertThat(response).isNotNull();
            assertThat(response).hasSize(1);
            assertThat(response.get(0).species()).isEqualTo(VALID_SPECIES);

            verify(petRepository).findBySpeciesIgnoreCase(VALID_SPECIES);
        }

        @Test
        @DisplayName("getPetsBySpecies | Éxito | Debería retornar lista vacía si no hay mascotas de esa especie")
        void getPetsBySpecies_WhenNoPetsFound_ShouldReturnEmptyList() {
            // Given
            setupAdminSecurityContext();
            when(petRepository.findBySpeciesIgnoreCase("Ave")).thenReturn(Collections.emptyList());

            // When
            List<PetResponse> response = petService.getPetsBySpecies("Ave");

            // Then
            assertThat(response).isNotNull();
            assertThat(response).isEmpty();
        }
    }

    // ========== TESTS: GET PETS BY BREED ==========

    @Nested
    @DisplayName("getPetsByBreed")
    class GetPetsByBreedTests {

        @Test
        @DisplayName("getPetsByBreed | Éxito | Debería filtrar mascotas por raza")
        void getPetsByBreed_ShouldReturnFilteredPets() {
            // Given
            setupAdminSecurityContext();
            when(petRepository.findByBreedIgnoreCase(VALID_BREED)).thenReturn(List.of(testPet));

            // When
            List<PetResponse> response = petService.getPetsByBreed(VALID_BREED);

            // Then
            assertThat(response).isNotNull();
            assertThat(response).hasSize(1);
            assertThat(response.get(0).breed()).isEqualTo(VALID_BREED);

            verify(petRepository).findByBreedIgnoreCase(VALID_BREED);
        }
    }

    // ========== TESTS: GET ACTIVE PETS ==========

    @Nested
    @DisplayName("getActivePets")
    class GetActivePetsTests {

        @Test
        @DisplayName("getActivePets | Éxito | Debería retornar solo mascotas activas")
        void getActivePets_ShouldReturnOnlyActivePets() {
            // Given
            setupAdminSecurityContext();
            when(petRepository.findByIsActiveTrue()).thenReturn(List.of(testPet));

            // When
            List<PetResponse> response = petService.getActivePets();

            // Then
            assertThat(response).isNotNull();
            assertThat(response).hasSize(1);
            assertThat(response.get(0).isActive()).isTrue();

            verify(petRepository).findByIsActiveTrue();
        }

        @Test
        @DisplayName("getActivePets | Éxito | Debería retornar lista vacía si no hay mascotas activas")
        void getActivePets_WhenNoActivePets_ShouldReturnEmptyList() {
            // Given
            setupAdminSecurityContext();
            when(petRepository.findByIsActiveTrue()).thenReturn(Collections.emptyList());

            // When
            List<PetResponse> response = petService.getActivePets();

            // Then
            assertThat(response).isNotNull();
            assertThat(response).isEmpty();
        }
    }

    // ========== TESTS: SEARCH PETS ==========

    @Nested
    @DisplayName("searchPets")
    class SearchPetsTests {

        @Test
        @DisplayName("searchPets | Éxito | Debería encontrar mascotas por término de búsqueda")
        void searchPets_ShouldReturnMatchingPets() {
            // Given
            setupAdminSecurityContext();
            when(petRepository.findBySearchTerm("Firulais")).thenReturn(List.of(testPet));

            // When
            List<PetResponse> response = petService.searchPets("Firulais");

            // Then
            assertThat(response).isNotNull();
            assertThat(response).hasSize(1);
            assertThat(response.get(0).name()).isEqualTo(VALID_PET_NAME);

            verify(petRepository).findBySearchTerm("Firulais");
        }

        @Test
        @DisplayName("searchPets | Éxito | Debería retornar lista vacía si no hay coincidencias")
        void searchPets_WhenNoMatch_ShouldReturnEmptyList() {
            // Given
            setupAdminSecurityContext();
            when(petRepository.findBySearchTerm("NoExiste")).thenReturn(Collections.emptyList());

            // When
            List<PetResponse> response = petService.searchPets("NoExiste");

            // Then
            assertThat(response).isNotNull();
            assertThat(response).isEmpty();
        }
    }

    // ========== TESTS: IS PET NAME AVAILABLE ==========

    @Nested
    @DisplayName("isPetNameAvailable")
    class IsPetNameAvailableTests {

        @Test
        @DisplayName("isPetNameAvailable | Éxito | Debería retornar true cuando el nombre está disponible")
        void isPetNameAvailable_WhenNameIsAvailable_ShouldReturnTrue() {
            // Given
            when(petRepository.existsByNameIgnoreCaseAndAccountId(VALID_PET_NAME, VALID_ACCOUNT_ID))
                    .thenReturn(false);

            // When
            boolean result = petService.isPetNameAvailable(VALID_PET_NAME, VALID_ACCOUNT_ID);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("isPetNameAvailable | Éxito | Debería retornar false cuando el nombre ya existe")
        void isPetNameAvailable_WhenNameExists_ShouldReturnFalse() {
            // Given
            when(petRepository.existsByNameIgnoreCaseAndAccountId(VALID_PET_NAME, VALID_ACCOUNT_ID))
                    .thenReturn(true);

            // When
            boolean result = petService.isPetNameAvailable(VALID_PET_NAME, VALID_ACCOUNT_ID);

            // Then
            assertThat(result).isFalse();
        }
    }

    // ========== TESTS: HEALTH CHECK ==========

    @Nested
    @DisplayName("healthCheck")
    class HealthCheckTests {

        @Test
        @DisplayName("healthCheck | Éxito | Debería retornar mensaje de confirmación")
        void healthCheck_ShouldReturnSuccessMessage() {
            // When
            String response = petService.healthCheck();

            // Then
            assertThat(response).isNotNull();
            assertThat(response).contains("PetService");
            assertThat(response).contains("operativo");
        }
    }
}
