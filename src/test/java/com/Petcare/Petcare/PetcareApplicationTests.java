package com.Petcare.Petcare;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"jwt.secret=testSecret123456789012345678901234567890",
		"spring.mail.username=test@test.com",
		"spring.mail.password=test"
})
class PetcareApplicationTests {

	@Test
	void contextLoads() {
	}

}
