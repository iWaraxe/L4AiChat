package com.coherentsolutions.l4aichat.s8multimodel.service;

import com.coherentsolutions.l4aichat.s8multimodel.dto.ModelResponse;
import com.coherentsolutions.l4aichat.s8multimodel.dto.ModelComparisonResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service demonstrating multiple chat models and model comparison
 */
@Service
public class MultiModelService {

    private static final Logger logger = LoggerFactory.getLogger(MultiModelService.class);
    
    private final ChatClient gpt4Client;
    private final ChatClient gpt35TurboClient; 
    private final ChatClient gpt4TurboClient;
    private final ExecutorService executorService;

    public MultiModelService(ChatClient.Builder chatClientBuilder) {
        // Create different model configurations
        this.gpt4Client = chatClientBuilder
                .defaultSystem("You are a helpful assistant using GPT-4.")
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("gpt-4")
                        .temperature(0.7)
                        .build())
                .build();

        this.gpt35TurboClient = chatClientBuilder
                .defaultSystem("You are a helpful assistant using GPT-3.5 Turbo.")
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("gpt-3.5-turbo")
                        .temperature(0.7)
                        .build())
                .build();

        this.gpt4TurboClient = chatClientBuilder
                .defaultSystem("You are a helpful assistant using GPT-4 Turbo.")
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("gpt-4-turbo")
                        .temperature(0.7)
                        .build())
                .build();

        this.executorService = Executors.newFixedThreadPool(4);
        logger.info("MultiModelService initialized with GPT-4, GPT-3.5 Turbo, and GPT-4 Turbo");
    }

    /**
     * Get response from a specific model
     */
    public ModelResponse getSingleModelResponse(String modelName, String message, 
                                              Double temperature, Integer maxTokens) {
        logger.info("Getting response from model: {} for message: {}", modelName, message);
        
        ChatClient client = getClientForModel(modelName);
        if (client == null) {
            ModelResponse errorResponse = new ModelResponse(modelName, null);
            errorResponse.setError("Model not supported: " + modelName);
            return errorResponse;
        }

        long startTime = System.currentTimeMillis();
        
        try {
            // Build options if provided
            OpenAiChatOptions.Builder optionsBuilder = OpenAiChatOptions.builder()
                    .model(modelName);
            
            if (temperature != null) {
                optionsBuilder.temperature(temperature);
            }
            if (maxTokens != null) {
                optionsBuilder.maxTokens(maxTokens);
            }
            
            OpenAiChatOptions options = optionsBuilder.build();
            
            String response = client.prompt()
                    .user(message)
                    .options(options)
                    .call()
                    .content();
            
            long responseTime = System.currentTimeMillis() - startTime;
            
            ModelResponse modelResponse = new ModelResponse(modelName, response);
            modelResponse.setResponseTimeMs(responseTime);
            
            return modelResponse;
            
        } catch (Exception e) {
            logger.error("Error getting response from model {}: {}", modelName, e.getMessage());
            
            ModelResponse errorResponse = new ModelResponse(modelName, null);
            errorResponse.setError("Error: " + e.getMessage());
            errorResponse.setResponseTimeMs(System.currentTimeMillis() - startTime);
            
            return errorResponse;
        }
    }

    /**
     * Compare responses from multiple models
     */
    public ModelComparisonResponse compareModels(String message, List<String> models, 
                                               Double temperature, Integer maxTokens) {
        logger.info("Comparing models {} for message: {}", models, message);
        
        List<String> modelsToUse = models != null && !models.isEmpty() 
            ? models 
            : getAvailableModels();
        
        // Execute requests in parallel
        List<CompletableFuture<ModelResponse>> futures = new ArrayList<>();
        
        for (String model : modelsToUse) {
            CompletableFuture<ModelResponse> future = CompletableFuture.supplyAsync(
                () -> getSingleModelResponse(model, message, temperature, maxTokens),
                executorService
            );
            futures.add(future);
        }
        
        // Wait for all responses
        List<ModelResponse> responses = new ArrayList<>();
        for (CompletableFuture<ModelResponse> future : futures) {
            try {
                responses.add(future.get());
            } catch (Exception e) {
                logger.error("Error getting model response: {}", e.getMessage());
                ModelResponse errorResponse = new ModelResponse("unknown", null);
                errorResponse.setError("Execution error: " + e.getMessage());
                responses.add(errorResponse);
            }
        }
        
        return new ModelComparisonResponse(message, responses);
    }

    /**
     * Get fastest model response
     */
    public ModelResponse getFastestResponse(String message, Double temperature, Integer maxTokens) {
        logger.info("Getting fastest response for message: {}", message);
        
        List<String> models = getAvailableModels();
        ModelComparisonResponse comparison = compareModels(message, models, temperature, maxTokens);
        
        return comparison.getModelResponses().stream()
                .filter(r -> r.getError() == null)
                .min((r1, r2) -> Long.compare(r1.getResponseTimeMs(), r2.getResponseTimeMs()))
                .orElse(comparison.getModelResponses().get(0));
    }

    /**
     * Get available models
     */
    public List<String> getAvailableModels() {
        return List.of("gpt-4", "gpt-3.5-turbo", "gpt-4-turbo");
    }

    /**
     * Get model statistics
     */
    public record ModelStats(String model, boolean available, String description) {}
    
    public List<ModelStats> getModelStatistics() {
        return List.of(
            new ModelStats("gpt-4", true, "OpenAI GPT-4 - High quality, slower"),
            new ModelStats("gpt-3.5-turbo", true, "OpenAI GPT-3.5 Turbo - Fast, cost-effective"),
            new ModelStats("gpt-4-turbo", true, "OpenAI GPT-4 Turbo - Enhanced GPT-4")
        );
    }

    private ChatClient getClientForModel(String modelName) {
        return switch (modelName.toLowerCase()) {
            case "gpt-4" -> gpt4Client;
            case "gpt-3.5-turbo" -> gpt35TurboClient;
            case "gpt-4-turbo" -> gpt4TurboClient;
            default -> null;
        };
    }
}