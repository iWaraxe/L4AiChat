package com.coherentsolutions.l4aichat.s2components;

import com.coherentsolutions.l4aichat.testutils.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = ComponentsApplication.class)
@AutoConfigureMockMvc
class S2ComponentsIntegrationTest extends IntegrationTestBase {

    @Test
    void shouldDemonstrateChatClientBasics() throws Exception {
        mockMvc.perform(get("/api/s2/demo/chat-client"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.basicResponse").exists())
                .andExpect(jsonPath("$.systemPromptResponse").exists())
                .andExpect(jsonPath("$.customOptionsResponse").exists());
    }

    @Test
    void shouldDemonstrateChatModelFeatures() throws Exception {
        mockMvc.perform(get("/api/s2/demo/chat-model"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.simpleResponse").exists())
                .andExpect(jsonPath("$.detailedResponse").exists())
                .andExpect(jsonPath("$.usage").exists());
    }

    @Test
    void shouldDemonstrateMessageTypes() throws Exception {
        mockMvc.perform(get("/api/s2/demo/message-types"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userMessage").exists())
                .andExpect(jsonPath("$.systemMessage").exists())
                .andExpect(jsonPath("$.assistantMessage").exists());
    }

    @Test
    void shouldDemonstrateStructuredOutput() throws Exception {
        mockMvc.perform(post("/api/s2/demo/structured-output")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"topic\": \"artificial intelligence\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.person").exists())
                .andExpect(jsonPath("$.person.name").exists())
                .andExpect(jsonPath("$.person.age").exists())
                .andExpect(jsonPath("$.rawResponse").exists());
    }

    @Test
    void shouldHandleStreamingResponse() throws Exception {
        mockMvc.perform(get("/api/s2/demo/streaming"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_EVENT_STREAM_VALUE))
                .andExpect(header().string("Cache-Control", "no-cache"));
    }

    @Test
    void shouldHandleImageContent() throws Exception {
        String imageRequestJson = """
            {
                "prompt": "Describe this image",
                "imageUrl": "https://example.com/test-image.jpg"
            }
            """;

        mockMvc.perform(post("/api/s2/demo/image-analysis")
                .contentType(MediaType.APPLICATION_JSON)
                .content(imageRequestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.description").exists())
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/test-image.jpg"));
    }

    @Test
    void shouldValidateStructuredOutputRequest() throws Exception {
        mockMvc.perform(post("/api/s2/demo/structured-output")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")) // Empty request
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldHandleInvalidImageUrl() throws Exception {
        String invalidImageRequestJson = """
            {
                "prompt": "Describe this image",
                "imageUrl": "not-a-valid-url"
            }
            """;

        mockMvc.perform(post("/api/s2/demo/image-analysis")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidImageRequestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldDemonstrateOptionsConfiguration() throws Exception {
        mockMvc.perform(get("/api/s2/demo/options"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.lowTemperatureResponse").exists())
                .andExpect(jsonPath("$.highTemperatureResponse").exists())
                .andExpect(jsonPath("$.shortResponse").exists())
                .andExpect(jsonPath("$.longResponse").exists());
    }

    @Test
    void shouldHandleMultiModalInput() throws Exception {
        String multiModalRequestJson = """
            {
                "textPrompt": "Analyze this content",
                "media": [
                    {
                        "type": "image",
                        "url": "https://example.com/image1.jpg"
                    },
                    {
                        "type": "text",
                        "content": "Additional context text"
                    }
                ]
            }
            """;

        mockMvc.perform(post("/api/s2/demo/multi-modal")
                .contentType(MediaType.APPLICATION_JSON)
                .content(multiModalRequestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.analysis").exists())
                .andExpect(jsonPath("$.mediaCount").value(2));
    }
}