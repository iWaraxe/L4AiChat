package com.coherentsolutions.l4aichat.s8multimodel.controller;

import com.coherentsolutions.l4aichat.s8multimodel.dto.ModelComparisonRequest;
import com.coherentsolutions.l4aichat.s8multimodel.dto.ModelComparisonResponse;
import com.coherentsolutions.l4aichat.s8multimodel.dto.ModelResponse;
import com.coherentsolutions.l4aichat.s8multimodel.service.MultiModelService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/s8/multimodel")
public class MultiModelController {

    private static final Logger logger = LoggerFactory.getLogger(MultiModelController.class);
    private final MultiModelService multiModelService;

    public MultiModelController(MultiModelService multiModelService) {
        this.multiModelService = multiModelService;
    }

    /**
     * Get response from a specific model
     */
    @PostMapping("/model/{modelName}")
    public ResponseEntity<ModelResponse> singleModel(
            @PathVariable String modelName,
            @Valid @RequestBody ModelComparisonRequest request) {
        
        logger.info("Single model request for {}: {}", modelName, request.getMessage());
        
        ModelResponse response = multiModelService.getSingleModelResponse(
            modelName, 
            request.getMessage(),
            request.getTemperature(),
            request.getMaxTokens()
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Compare responses from multiple models
     */
    @PostMapping("/compare")
    public ResponseEntity<ModelComparisonResponse> compareModels(
            @Valid @RequestBody ModelComparisonRequest request) {
        
        logger.info("Model comparison request: {}", request.getMessage());
        
        ModelComparisonResponse response = multiModelService.compareModels(
            request.getMessage(),
            request.getModels(),
            request.getTemperature(),
            request.getMaxTokens()
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get fastest response from available models
     */
    @PostMapping("/fastest")
    public ResponseEntity<ModelResponse> fastestResponse(
            @Valid @RequestBody ModelComparisonRequest request) {
        
        logger.info("Fastest response request: {}", request.getMessage());
        
        ModelResponse response = multiModelService.getFastestResponse(
            request.getMessage(),
            request.getTemperature(),
            request.getMaxTokens()
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get available models
     */
    @GetMapping("/models")
    public ResponseEntity<List<String>> getAvailableModels() {
        List<String> models = multiModelService.getAvailableModels();
        return ResponseEntity.ok(models);
    }

    /**
     * Get model statistics and information
     */
    @GetMapping("/models/stats")
    public ResponseEntity<List<MultiModelService.ModelStats>> getModelStats() {
        List<MultiModelService.ModelStats> stats = multiModelService.getModelStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * Compare specific models
     */
    @PostMapping("/compare/models")
    public ResponseEntity<ModelComparisonResponse> compareSpecificModels(
            @Valid @RequestBody ModelComparisonRequest request,
            @RequestParam List<String> models) {
        
        logger.info("Specific models comparison request for models {}: {}", 
                   models, request.getMessage());
        
        ModelComparisonResponse response = multiModelService.compareModels(
            request.getMessage(),
            models,
            request.getTemperature(),
            request.getMaxTokens()
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Benchmark models with a standard question
     */
    @PostMapping("/benchmark")
    public ResponseEntity<ModelComparisonResponse> benchmarkModels(
            @RequestParam(defaultValue = "Explain quantum computing in simple terms") String question,
            @RequestParam(defaultValue = "0.7") double temperature,
            @RequestParam(defaultValue = "150") int maxTokens) {
        
        logger.info("Benchmark request with question: {}", question);
        
        ModelComparisonResponse response = multiModelService.compareModels(
            question,
            multiModelService.getAvailableModels(),
            temperature,
            maxTokens
        );
        
        return ResponseEntity.ok(response);
    }
}