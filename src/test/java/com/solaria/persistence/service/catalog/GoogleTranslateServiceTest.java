package com.solaria.persistence.service.catalog;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import com.solaria.persistence.config.GoogleTranslateProperties;
import com.solaria.persistence.exception.BusinessRuleException;

class GoogleTranslateServiceTest {
    @Test
    void sendsNamedFieldsAndConsumerTokenToRegistryAndValidatesLanguages() {
        var builder = RestClient.builder().baseUrl("http://registry.test");
        var server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("http://registry.test/v1/i18n/translate"))
                .andExpect(method(HttpMethod.POST)).andExpect(header("Authorization", "Bearer consumer-test-token"))
                .andExpect(content().json("{\"fields\":[{\"field_name\":\"title\",\"text\":\"Painel\"}]}"))
                .andRespond(withSuccess("{\"source_language\":\"pt\",\"translations\":[{\"field_name\":\"title\",\"language\":\"en\",\"value\":\"Panel\"},{\"field_name\":\"title\",\"language\":\"es\",\"value\":\"Panel solar\"}]}", MediaType.APPLICATION_JSON));
        var properties = new GoogleTranslateProperties(); properties.setConsumerToken("consumer-test-token");
        var result = new GoogleTranslateService(builder.build(), properties).translateFields(Map.of("title", "Painel"));
        assertEquals("pt", result.sourceLanguage());
        assertEquals("Painel", result.translations().get("pt").get("title"));
        assertEquals("Panel", result.translations().get("en").get("title"));
        server.verify();
    }

    @Test
    void rejectsMissingFieldsAndDoesNotTreatIncompleteResponseAsSuccess() {
        var builder = RestClient.builder().baseUrl("http://registry.test");
        var server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("http://registry.test/v1/i18n/translate"))
                .andRespond(withSuccess("{\"source_language\":\"pt\",\"translations\":[]}", MediaType.APPLICATION_JSON));
        var properties = new GoogleTranslateProperties(); properties.setConsumerToken("consumer-test-token");
        assertThrows(BusinessRuleException.class, () -> new GoogleTranslateService(builder.build(), properties).translateFields(Map.of("title", "Painel")));
        server.verify();
    }

    @Test
    void missingConsumerTokenFailsBeforeCallingRegistry() {
        var builder = RestClient.builder();
        var server = MockRestServiceServer.bindTo(builder).build();
        assertThrows(BusinessRuleException.class, () -> new GoogleTranslateService(builder.build(), new GoogleTranslateProperties()).translateFields(Map.of("title", "Painel")));
        server.verify();
    }
}
