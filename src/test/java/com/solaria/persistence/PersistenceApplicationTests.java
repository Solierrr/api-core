package com.solaria.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PersistenceApplicationTests {

    @org.springframework.test.context.bean.override.mockito.MockitoBean(name = "authRestClient")
    private org.springframework.web.client.RestClient authRestClient;

	@Test
	void contextLoads() {
	}

}
