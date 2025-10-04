package com.Petcare.Petcare.Configurations;

import com.Petcare.Petcare.Models.Account.Account;
import com.Petcare.Petcare.Models.Account.AccountUser;
import com.Petcare.Petcare.Models.SitterProfile;
import com.Petcare.Petcare.Models.SitterWorkExperience;
import com.Petcare.Petcare.Models.User.Role;
import com.Petcare.Petcare.Models.User.User;
import com.Petcare.Petcare.Repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final AccountUserRepository accountUserRepository;
    private final SitterProfileRepository sitterProfileRepository;
    private final SitterWorkExperienceRepository sitterWorkExperienceRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            System.out.println("Base de datos vacía, iniciando el seeder de datos...");
            createSitters();
        } else {
            System.out.println("La base de datos ya contiene datos, el seeder no se ejecutará.");
        }
    }

    private void createSitters() {
        List<String> names = List.of("Ivan", "Juanita", "Abdon", "Alvaro", "Jorge");
        List<String> lastNames = List.of("González", "Rodríguez", "Pérez", "Martínez", "García");

        for (int i = 0; i < 5; i++) {
            String firstName = names.get(i);
            String lastName = lastNames.get(i);
            String email = firstName.toLowerCase() + "." + lastName.toLowerCase() + "@petcare.com";

            // 1. Crear el Usuario (User) usando constructores y setters
            User sitterUser = new User();
            sitterUser.setFirstName(firstName);
            sitterUser.setLastName(lastName);
            sitterUser.setEmail(email);
            sitterUser.setPassword(passwordEncoder.encode("password123"));
            sitterUser.setRole(Role.SITTER);
            userRepository.save(sitterUser);

            // 2. Crear la Cuenta (Account)
            Account account = new Account();
            account.setOwnerUser(sitterUser);
            account.setAccountName("Cuenta cuidador" + sitterUser.getFirstName());
            account.setAccountNumber("123-234"+sitterUser.getId());
            accountRepository.save(account);

            // 3. Vincular Usuario y Cuenta (AccountUser)
            AccountUser accountUser = new AccountUser();
            accountUser.setUser(sitterUser);
            accountUser.setAccount(account);
            accountUser.setRole(Role.SITTER);
            accountUserRepository.save(accountUser);

            // 4. Crear el Perfil de Cuidador (SitterProfile)
            SitterProfile sitterProfile = new SitterProfile();
            sitterProfile.setUser(sitterUser);
            sitterProfile.setBio("Amante de los animales con " + (i + 3) + " años de experiencia. Especialista en perros grandes y gatos tímidos.");
            sitterProfile.setServicingRadius(10);
            sitterProfile.setHourlyRate(new BigDecimal("1500.00").add(new BigDecimal(i * 100)));
            sitterProfileRepository.save(sitterProfile);

            // 5. Crear Experiencia Laboral (SitterWorkExperience)
            SitterWorkExperience experience = new SitterWorkExperience();
            experience.setSitterProfile(sitterProfile);
            experience.setJobTitle("Cuidador de Mascotas Senior");
            experience.setCompanyName("Refugio de Animales 'Patitas Felices'");
            experience.setStartDate(LocalDate.of(2018 + i, 1, 15));
            experience.setEndDate(LocalDate.now().minusMonths(6));
            experience.setResponsibilities("Responsable del cuidado diario, alimentación y paseos de más de 20 perros y gatos.");
            sitterWorkExperienceRepository.save(experience);

            System.out.println("Creada cuidadora: " + sitterUser.getEmail());
        }

        System.out.println("¡Seeder completado! Se crearon 5 cuidadoras.");
    }
}