package com.notification_service.template;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EmailTemplateBuilderTest {

    @Mock
    private TemplateEngine templateEngine;

    @InjectMocks
    private EmailTemplateBuilder emailTemplateBuilder;

    private Map<String, Object> variables;

    @BeforeEach
    void setUp() {
        variables = new HashMap<>();
        variables.put("name", "John Doe");
        variables.put("link", "http://localhost:8080/reset");
    }

    @Test
    void testBuild_WithVariables() {
        String templateName = "welcome-email";
        String expectedHtml = "<html>Welcome John Doe</html>";
        when(templateEngine.process(eq(templateName), any(Context.class))).thenReturn(expectedHtml);

        String result = emailTemplateBuilder.build(templateName, variables);

        assertEquals(expectedHtml, result);
        
        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq(templateName), contextCaptor.capture());
        
        Context capturedContext = contextCaptor.getValue();
        assertEquals("John Doe", capturedContext.getVariable("name"));
    }

    @Test
    void testBuild_WithNullVariables() {
        String templateName = "simple-email";
        String expectedHtml = "<html>Hello</html>";
        when(templateEngine.process(eq(templateName), any(Context.class))).thenReturn(expectedHtml);

        String result = emailTemplateBuilder.build(templateName, null);

        assertEquals(expectedHtml, result);

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq(templateName), contextCaptor.capture());

        Context capturedContext = contextCaptor.getValue();
        // Since variables were null, context should be empty (though default variables might be present depending on context setup)
        assertEquals(0, capturedContext.getVariableNames().size());
    }
}
