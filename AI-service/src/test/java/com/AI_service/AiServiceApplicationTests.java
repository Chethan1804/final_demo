package com.AI_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.boot.test.mock.mockito.MockBean;
import com.AI_service.client.ResumeClient;

@SpringBootTest(properties = {"spring.cloud.discovery.enabled=false"})
class AiServiceApplicationTests {

    @MockBean
    private ResumeClient resumeClient;

	@Test
	void contextLoads() {
	}

}
