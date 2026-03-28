package com.Petcare.Petcare.Service;

import com.Petcare.Petcare.DTOs.Account.AccountResponse;
import com.Petcare.Petcare.DTOs.Account.CreateAccountRequest;
import com.Petcare.Petcare.Exception.Business.AccountNotFoundException;
import com.Petcare.Petcare.Models.Account.Account;
import com.Petcare.Petcare.Models.User.Role;
import com.Petcare.Petcare.Models.User.User;
import com.Petcare.Petcare.Repositories.AccountRepository;
import com.Petcare.Petcare.Services.Implement.AccountServiceImplement;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Suite de pruebas unitarias para {@link AccountServiceImplement}.
 *
 * @author Equipo Petcare 10
 * @version 1.0
 * @since 2025
 * @see AccountServiceImplement
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Pruebas Unitarias: AccountServiceImplement")
class AccountServiceImplementTest {

    @Mock private AccountRepository accountRepository;

    @InjectMocks
    private AccountServiceImplement accountService;

    private User testUser;
    private Account testAccount;
    private CreateAccountRequest createAccountRequest;

    private static final Long VALID_ACCOUNT_ID = 1L;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("user@test.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(Role.CLIENT);
        testUser.setActive(true);

        testAccount = new Account();
        testAccount.setId(VALID_ACCOUNT_ID);
        testAccount.setOwnerUser(testUser);
        testAccount.setAccountNumber("ACC-123");
        testAccount.setAccountName("Cuenta Familiar");

        createAccountRequest = new CreateAccountRequest();
        createAccountRequest.setAccountName("Nueva Cuenta");
        createAccountRequest.setAccountNumber("ACC-456");
    }

    // ========== TESTS: createAccount ==========

    @Nested
    @DisplayName("createAccount")
    class CreateAccountTests {

        @Test
        @DisplayName("createAccount | Éxito | Debería crear cuenta con usuario propietario")
        void createAccount_WithValidData_ShouldCreateAccount() {
            // Given
            when(accountRepository.save(any(Account.class))).thenAnswer(inv -> {
                Account a = inv.getArgument(0);
                a.setId(VALID_ACCOUNT_ID);
                return a;
            });

            // When
            AccountResponse response = accountService.createAccount(createAccountRequest, testUser);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.accountName()).isEqualTo("Nueva Cuenta");
            verify(accountRepository).save(any(Account.class));
        }
    }

    // ========== TESTS: getAccountById ==========

    @Nested
    @DisplayName("getAccountById")
    class GetAccountByIdTests {

        @Test
        @DisplayName("getAccountById | Éxito | Debería retornar cuenta existente")
        void getAccountById_WhenExists_ShouldReturnAccount() {
            // Given
            when(accountRepository.findById(VALID_ACCOUNT_ID)).thenReturn(Optional.of(testAccount));

            // When
            AccountResponse response = accountService.getAccountById(VALID_ACCOUNT_ID);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(VALID_ACCOUNT_ID);
        }

        @Test
        @DisplayName("getAccountById | Falla | Debería lanzar AccountNotFoundException cuando no existe")
        void getAccountById_WhenNotExists_ShouldThrowException() {
            // Given
            when(accountRepository.findById(99L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> accountService.getAccountById(99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Cuenta no encontrada");
        }
    }

    // ========== TESTS: getAllAccounts ==========

    @Nested
    @DisplayName("getAllAccounts")
    class GetAllAccountsTests {

        @Test
        @DisplayName("getAllAccounts | Éxito | Debería retornar lista de cuentas")
        void getAllAccounts_ShouldReturnAllAccounts() {
            // Given
            when(accountRepository.findAll()).thenReturn(List.of(testAccount));

            // When
            List<AccountResponse> result = accountService.getAllAccounts();

            // Then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("getAllAccounts | Éxito | Debería retornar lista vacía cuando no hay cuentas")
        void getAllAccounts_WhenNoAccounts_ShouldReturnEmptyList() {
            // Given
            when(accountRepository.findAll()).thenReturn(List.of());

            // When
            List<AccountResponse> result = accountService.getAllAccounts();

            // Then
            assertThat(result).isEmpty();
        }
    }
}
