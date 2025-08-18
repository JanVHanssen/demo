package be.ucll.se.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;

@SpringBootTest
class JanvhanssenBackendApplicationTests {

	@MockBean
	private JavaMailSender mailSender; // fake bean voor testcontext

	@Test
	void contextLoads() {
	}

}
