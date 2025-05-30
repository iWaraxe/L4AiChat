package com.coherentsolutions.l4aichat.s2components.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.ai.converter.StructuredOutputConverter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * This controller demonstrates structured output conversion methods for Spring AI 1.0.0
 */
@RestController
@RequestMapping("/api/structured")
public class StructuredOutputDemoController {

    private final ChatClient chatClient;

    public StructuredOutputDemoController(ChatClient.Builder chatClientBuilder) {
        // Build the ChatClient with any default system messages, advisors, etc.
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * Using the built-in entity() method for direct mapping
     */
    @PostMapping("/entity")
    public ProductInfo entityMethod(@RequestBody Map<String, String> request) {
        String productName = request.get("product");

        return this.chatClient
                .prompt()
                .system("""
                You are a product information service that returns structured data.
                Always respond with valid JSON that matches the requested schema.
                """)
                .user("Give me information about " + productName)
                .call()
                .entity(ProductInfo.class);
    }

    /**
     * Using BeanOutputConverter for more control over conversion
     */
    @PostMapping("/bean-converter")
    public ProductInfo beanConverterMethod(@RequestBody Map<String, String> request) {
        String productName = request.get("product");

        // Create a converter for ProductInfo class
        StructuredOutputConverter<ProductInfo> converter = new BeanOutputConverter<>(ProductInfo.class);

        // Extract the format instructions from the converter
        String formatInstructions = converter.getFormat();

        return this.chatClient
                .prompt()
                .user(u -> u.text("""
                    Give me information about {product}.
                    {format}
                    """)
                        .param("product", productName)
                        .param("format", formatInstructions))
                .call()
                .entity(converter);
    }

    /**
     * Using ListOutputConverter for comma-separated list outputs
     */
    @PostMapping("/list-converter")
    public List<String> listConverterMethod(@RequestBody Map<String, String> request) {
        String category = request.get("category");
        Integer count = Integer.parseInt(request.getOrDefault("count", "5"));

        // Create a converter for a List of Strings
        ListOutputConverter listConverter = new ListOutputConverter(new DefaultConversionService());

        String formatInstructions = listConverter.getFormat();

        return this.chatClient
                .prompt()
                .user(u -> u.text("""
                    List {count} most popular {category}.
                    {format}
                    """)
                        .param("category", category)
                        .param("count", count)
                        .param("format", formatInstructions))
                .call()
                .entity(listConverter);
    }

    /**
     * Using MapOutputConverter for key-value pair outputs
     */
    @PostMapping("/map-converter")
    public Map<String, Object> mapConverterMethod(@RequestBody Map<String, String> request) {
        String subject = request.get("subject");

        // Create a converter for Maps
        MapOutputConverter mapConverter = new MapOutputConverter();

        String formatInstructions = mapConverter.getFormat();

        return this.chatClient
                .prompt()
                .user(u -> u.text("""
                    Create a fact sheet about {subject} with these keys:
                    - name
                    - description
                    - yearCreated
                    - keyFeatures (as an array)

                    {format}
                    """)
                        .param("subject", subject)
                        .param("format", formatInstructions))
                .call()
                .entity(mapConverter);
    }

    /**
     * Models for structured output conversion
     */
    public record ProductInfo(
            String name,
            String description,
            double price,
            List<String> features,
            Map<String, String> specifications
    ) {}

    public record MovieRecommendation(
            String title,
            int year,
            String director,
            List<String> actors,
            String genre,
            double rating
    ) {}

    /**
     * Using a ParameterizedTypeReference for complex types
     */
    @PostMapping("/complex-types")
    public List<MovieRecommendation> complexTypeMethod(@RequestBody Map<String, String> request) {
        String genre = request.get("genre");

        // Create a converter for List<MovieRecommendation>
        StructuredOutputConverter<List<MovieRecommendation>> converter =
                new BeanOutputConverter<>(new ParameterizedTypeReference<>() {});

        String formatInstructions = converter.getFormat();

        return this.chatClient
                .prompt()
                .system("You are a movie recommendation service.")
                .user(u -> u.text("""
                    Recommend 3 {genre} movies with their details.
                    {format}
                    """)
                        .param("genre", genre)
                        .param("format", formatInstructions))
                .call()
                .entity(converter);
    }
}
