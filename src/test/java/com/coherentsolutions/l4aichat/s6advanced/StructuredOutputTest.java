package com.coherentsolutions.l4aichat.s6advanced;

import com.coherentsolutions.l4aichat.s6advanced.controller.StructuredChatController;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for structured output classes in s6advanced module
 */
class StructuredOutputTest {

    @Test
    void testProductInfo() {
        StructuredChatController.ProductInfo product = new StructuredChatController.ProductInfo();
        product.setName("Test Product");
        product.setDescription("A test product for unit testing");
        product.setPrice(99.99);
        product.setFeatures(Arrays.asList("Feature 1", "Feature 2", "Feature 3"));
        
        assertEquals("Test Product", product.getName());
        assertEquals("A test product for unit testing", product.getDescription());
        assertEquals(99.99, product.getPrice());
        assertEquals(3, product.getFeatures().size());
        assertTrue(product.getFeatures().contains("Feature 1"));
    }

    @Test
    void testSalesReportStructure() {
        StructuredChatController.SalesReport report = new StructuredChatController.SalesReport();
        report.setReportName("Q1 2024 Sales Report");
        report.setPeriod("2024-Q1");
        
        // Test ProductSales nested class
        StructuredChatController.SalesReport.ProductSales productSales = 
            new StructuredChatController.SalesReport.ProductSales();
        productSales.setProductName("Product A");
        productSales.setUnitsSold(100);
        productSales.setRevenue(9999.99);
        
        assertEquals("Product A", productSales.getProductName());
        assertEquals(100, productSales.getUnitsSold());
        assertEquals(9999.99, productSales.getRevenue());
        
        // Test Summary nested class
        StructuredChatController.SalesReport.Summary summary = 
            new StructuredChatController.SalesReport.Summary();
        summary.setTotalUnitsSold(500);
        summary.setTotalRevenue(49999.95);
        summary.setBestSellingProduct("Product A");
        
        assertEquals(500, summary.getTotalUnitsSold());
        assertEquals(49999.95, summary.getTotalRevenue());
        assertEquals("Product A", summary.getBestSellingProduct());
        
        // Set up the full report
        report.setProductSales(List.of(productSales));
        report.setSummary(summary);
        
        assertEquals("Q1 2024 Sales Report", report.getReportName());
        assertEquals("2024-Q1", report.getPeriod());
        assertEquals(1, report.getProductSales().size());
        assertNotNull(report.getSummary());
    }

    @Test
    void testEmptyStructuredObjects() {
        // Test that empty objects can be created without errors
        StructuredChatController.ProductInfo emptyProduct = new StructuredChatController.ProductInfo();
        assertNull(emptyProduct.getName());
        assertNull(emptyProduct.getDescription());
        assertEquals(0.0, emptyProduct.getPrice());
        assertNull(emptyProduct.getFeatures());
        
        StructuredChatController.SalesReport emptyReport = new StructuredChatController.SalesReport();
        assertNull(emptyReport.getReportName());
        assertNull(emptyReport.getPeriod());
        assertNull(emptyReport.getProductSales());
        assertNull(emptyReport.getSummary());
    }
}