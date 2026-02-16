package com.redis.om.hybrid.config;

import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.redis.om.hybrid.domain.Product;
import com.redis.om.hybrid.repositories.ProductRepository;
import com.redis.om.spring.util.ObjectUtils;

/**
 * Loads sample product data on application startup.
 */
@Configuration
public class DataLoader {

  private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

  @Bean
  CommandLineRunner loadData(ProductRepository productRepository) {
    return args -> {
      if (productRepository.count() == 0) {
        logger.info("Loading sample product data...");

        List<Product> products = List.of(
            // Electronics
            createProduct("elec-001", "Wireless Bluetooth Headphones", "Electronics",
                "Premium noise-cancelling wireless headphones with superior sound quality and long battery life",
                199.99, 50, "AudioTech", generateMockEmbedding(0.8f, 0.2f, 0.7f)), createProduct("elec-002",
                    "4K Smart TV 55 inch", "Electronics",
                    "Ultra HD 4K television with smart features, HDR support, and streaming capabilities", 799.99, 15,
                    "VisionPlus", generateMockEmbedding(0.75f, 0.3f, 0.65f)), createProduct("elec-003", "Gaming Laptop",
                        "Electronics",
                        "High-performance gaming laptop with RTX graphics card and fast processor for gaming", 1499.99,
                        8, "GameRig", generateMockEmbedding(0.85f, 0.25f, 0.75f)), createProduct("elec-004",
                            "Wireless Mouse", "Electronics",
                            "Ergonomic wireless mouse with precision tracking and long battery life", 29.99, 100,
                            "TechGear", generateMockEmbedding(0.7f, 0.4f, 0.6f)),

            // Home & Kitchen
            createProduct("home-001", "Coffee Maker Machine", "Home & Kitchen",
                "Programmable coffee maker with thermal carafe, brew strength control, and auto shutoff", 89.99, 30,
                "BrewMaster", generateMockEmbedding(0.4f, 0.8f, 0.3f)), createProduct("home-002",
                    "Stainless Steel Cookware Set", "Home & Kitchen",
                    "Professional-grade stainless steel pots and pans set for cooking all your favorite meals", 249.99,
                    20, "ChefPro", generateMockEmbedding(0.3f, 0.85f, 0.35f)), createProduct("home-003",
                        "Robot Vacuum Cleaner", "Home & Kitchen",
                        "Smart robot vacuum with mapping technology, automatic cleaning, and app control", 349.99, 25,
                        "CleanBot", generateMockEmbedding(0.6f, 0.7f, 0.5f)),

            // Sports & Outdoors
            createProduct("sport-001", "Yoga Mat", "Sports & Outdoors",
                "Extra thick yoga mat with non-slip surface perfect for yoga, pilates, and exercise", 39.99, 75,
                "FitLife", generateMockEmbedding(0.2f, 0.3f, 0.9f)), createProduct("sport-002", "Running Shoes",
                    "Sports & Outdoors",
                    "Lightweight running shoes with cushioned sole and breathable mesh for comfortable running", 129.99,
                    40, "RunFast", generateMockEmbedding(0.25f, 0.35f, 0.85f)), createProduct("sport-003",
                        "Camping Tent", "Sports & Outdoors",
                        "4-person waterproof camping tent with easy setup for outdoor adventures and hiking", 199.99,
                        12, "OutdoorPro", generateMockEmbedding(0.3f, 0.4f, 0.8f)),

            // Books
            createProduct("book-001", "Redis Programming Guide", "Books",
                "Comprehensive guide to Redis database programming with practical examples and best practices", 49.99,
                100, "TechBooks", generateMockEmbedding(0.5f, 0.5f, 0.5f)), createProduct("book-002",
                    "Machine Learning Basics", "Books",
                    "Introduction to machine learning algorithms, neural networks, and artificial intelligence", 59.99,
                    80, "TechBooks", generateMockEmbedding(0.55f, 0.45f, 0.52f)));

        productRepository.saveAll(products);
        logger.info("Loaded {} products", products.size());
      } else {
        logger.info("Product data already exists, skipping load");
      }
    };
  }

  private Product createProduct(String id, String name, String category, String description, double price, int stock,
      String brand, byte[] embedding) {
    Product product = Product.of(name, category, description, price, embedding, stock, brand);
    product.setId(id);
    return product;
  }

  /**
   * Generates a mock 384-dimensional embedding based on seed values.
   * In a real application, these would come from a sentence transformer model.
   *
   * The seed values create a pattern that makes similar products cluster together.
   */
  private byte[] generateMockEmbedding(float seed1, float seed2, float seed3) {
    Random random = new Random((long) (seed1 * 1000 + seed2 * 100 + seed3 * 10));
    float[] embedding = new float[384];

    // Create a pattern based on seeds
    for (int i = 0; i < 384; i++) {
      float base = (i < 128) ? seed1 : (i < 256) ? seed2 : seed3;
      embedding[i] = base + (random.nextFloat() - 0.5f) * 0.1f;
    }

    return ObjectUtils.floatArrayToByteArray(embedding);
  }
}
