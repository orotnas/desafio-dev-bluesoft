package br.com.bluesoft.erp.testecandidatos;

import br.com.bluesoft.erp.testecandidatos.config.TestConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.TestPropertySource;

/**
 * Simple integration test to verify that the application context loads correctly.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TesteApplication.class)
@Import(TestConfig.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "logging.level.org.springframework=DEBUG",
    "logging.level.org.hibernate=DEBUG",
    "spring.jpa.show-sql=true"
})
public class SimpleIntegrationTest {

    @Test
    public void contextLoads() {
        // This test will pass if the application context loads successfully
    }
}
