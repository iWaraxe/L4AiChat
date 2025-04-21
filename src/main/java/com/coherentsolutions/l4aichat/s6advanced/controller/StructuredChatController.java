package com.coherentsolutions.l4aichat.s6advanced.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat/structured")
public class StructuredChatController {

    private final ChatClient chatClient;

    public StructuredChatController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * Returns a simple Map structure from the AI
     */
    @PostMapping("/map")
    public ResponseEntity<Map<String, Object>> getMapResponse(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");

        // Create a MapOutputConverter
        MapOutputConverter mapOutputConverter = new MapOutputConverter();

        // Use the format from the converter to instruct the AI on output format
        Map<String, Object> response = this.chatClient.prompt()
                .user(u -> u.text("""
                        %s
                        
                        %s
                        """.formatted(userMessage, mapOutputConverter.getFormat())))
                .call()
                .entity(new ParameterizedTypeReference<Map<String, Object>>() {});

        return ResponseEntity.ok(response);
    }

    /**
     * Returns a List structure from the AI
     */
    @PostMapping("/list")
    public ResponseEntity<List<String>> getListResponse(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");

        // Create a ListOutputConverter
        ListOutputConverter listOutputConverter = new ListOutputConverter(new DefaultConversionService());

        // Use the format from the converter
        List<String> response = this.chatClient.prompt()
                .user(u -> u.text("""
                        %s
                        
                        %s
                        """.formatted(userMessage, listOutputConverter.getFormat())))
                .call()
                .entity(listOutputConverter);

        return ResponseEntity.ok(response);
    }

    /**
     * Returns a typed Java object from the AI
     */
    @PostMapping("/bean")
    public ResponseEntity<ProductInfo> getBeanResponse(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");

        // Create a BeanOutputConverter for the ProductInfo class
        BeanOutputConverter<ProductInfo> beanOutputConverter =
                new BeanOutputConverter<>(ProductInfo.class);

        // Use the format from the converter
        ProductInfo response = this.chatClient.prompt()
                .user(u -> u.text("""
                        %s
                        
                        %s
                        """.formatted(userMessage, beanOutputConverter.getFormat())))
                .call()
                .entity(ProductInfo.class);

        return ResponseEntity.ok(response);
    }

    /**
     * Returns a complex entity with nested objects
     */
    @PostMapping("/complex")
    public ResponseEntity<SalesReport> getComplexResponse(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");

        // Create a BeanOutputConverter for the complex SalesReport class
        BeanOutputConverter<SalesReport> beanOutputConverter =
                new BeanOutputConverter<>(SalesReport.class);

        // Use the format from the converter
        SalesReport response = this.chatClient.prompt()
                .user(u -> u.text("""
                        %s
                        
                        %s
                        """.formatted(userMessage, beanOutputConverter.getFormat())))
                .call()
                .entity(SalesReport.class);

        return ResponseEntity.ok(response);
    }

    /**
     * Product information entity class
     */
    public static class ProductInfo {
        private String name;
        private String description;
        private double price;
        private List<String> features;

        // Default constructor
        public ProductInfo() {}

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }

        public List<String> getFeatures() { return features; }
        public void setFeatures(List<String> features) { this.features = features; }
    }

    /**
     * Complex sales report entity with nested objects
     */
    public static class SalesReport {
        private String reportName;
        private String period;
        private List<ProductSales> productSales;
        private Summary summary;

        // Default constructor
        public SalesReport() {}

        // Getters and setters
        public String getReportName() { return reportName; }
        public void setReportName(String reportName) { this.reportName = reportName; }

        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }

        public List<ProductSales> getProductSales() { return productSales; }
        public void setProductSales(List<ProductSales> productSales) { this.productSales = productSales; }

        public Summary getSummary() { return summary; }
        public void setSummary(Summary summary) { this.summary = summary; }

        // Nested ProductSales class
        public static class ProductSales {
            private String productName;
            private int unitsSold;
            private double revenue;

            // Default constructor
            public ProductSales() {}

            // Getters and setters
            public String getProductName() { return productName; }
            public void setProductName(String productName) { this.productName = productName; }

            public int getUnitsSold() { return unitsSold; }
            public void setUnitsSold(int unitsSold) { this.unitsSold = unitsSold; }

            public double getRevenue() { return revenue; }
            public void setRevenue(double revenue) { this.revenue = revenue; }
        }

        // Nested Summary class
        public static class Summary {
            private int totalUnitsSold;
            private double totalRevenue;
            private String bestSellingProduct;

            // Default constructor
            public Summary() {}

            // Getters and setters
            public int getTotalUnitsSold() { return totalUnitsSold; }
            public void setTotalUnitsSold(int totalUnitsSold) { this.totalUnitsSold = totalUnitsSold; }

            public double getTotalRevenue() { return totalRevenue; }
            public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }

            public String getBestSellingProduct() { return bestSellingProduct; }
            public void setBestSellingProduct(String bestSellingProduct) { this.bestSellingProduct = bestSellingProduct; }
        }
    }
}