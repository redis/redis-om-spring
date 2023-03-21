package com.redis.om.vss.controllers;

import com.redis.om.spring.search.stream.EntityStream;
import com.redis.om.spring.search.stream.SearchStream;
import com.redis.om.vss.domain.Product;
import com.redis.om.vss.domain.Product$;
import com.redis.om.vss.repositories.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/")
public class ProductController {
  Logger logger = LoggerFactory.getLogger(ProductController.class);

  private static int K = 15;

  @Autowired
  private ProductRepository repository;

  @Autowired
  private EntityStream entityStream;

  @GetMapping
  public String index(Model model) {
    logger.info("🔎 index :: Showing first {} products...", K);

    List<Product> products = entityStream.of(Product.class)
        .limit(K) //
        .collect(Collectors.toList());

    setModelProperties(model, products);

    return "index";
  }

  @GetMapping("/load")
  public String load(
      Model model, //
      @RequestParam Optional<String> gender, //
      @RequestParam Optional<String> category, //
      @RequestParam(name = "skip") Optional<Long> skipParam //
  ) {
    long count = repository.count();
    long skip = skipParam.isPresent() && skipParam.get() < count - K ? skipParam.get() + K : 0;

    logger.info("🔎 load :: Showing {} products, starting at {}...", K, skip);

    SearchStream<Product> stream = entityStream.of(Product.class)
        .skip(skip)
        .limit(K);

    applyFilters(stream, gender, category);

    List<Product> products = stream.collect(Collectors.toList());

    if (products.size() == 0) {
      skip = 0;
    }

    setModelProperties(model, products, Optional.of(skip), gender, category);

    return "fragments :: root";
  }

  @GetMapping("/vss/text/{id}")
  public String findSimilarByText(
      Model model, //
      @PathVariable("id") String id, //
      @RequestParam Optional<String> gender, //
      @RequestParam Optional<String> category, //
      @RequestParam Optional<Long> skip //
  ) {
    Optional<Product> maybeProduct = repository.findById(id);
    if (maybeProduct.isPresent()) {
      Product product = maybeProduct.get();
      logger.info("🔎 vss :: Finding products with text similar to product {} ({})...", id, product.getProductDisplayName());

      SearchStream<Product> stream = entityStream.of(Product.class);

      applyFilters(stream, gender, category);

      List<Product> products = stream
          .filter(Product$.SENTENCE_EMBEDDING.knn(K, product.getSentenceEmbedding())) //
          .sorted(Product$._SENTENCE_EMBEDDING_SCORE) //
          .limit(K) //
          .collect(Collectors.toList());

      setModelProperties(model, products, skip, gender, category);
    } else {
      logger.warn("🔎 vss :: There is no product with id {}", id);
    }

    return "fragments :: root";
  }

  @GetMapping("/vss/image/{id}")
  public String findSimilarByImage(
      Model model, //
      @PathVariable("id") String id, //
      @RequestParam Optional<String> gender, //
      @RequestParam Optional<String> category, //
      @RequestParam Optional<Long> skip //
  ) {
    Optional<Product> maybeProduct = repository.findById(id);
    if (maybeProduct.isPresent()) {
      Product product = maybeProduct.get();
      logger.info("🔎 vss :: Finding products with images similar to product {} ({})...", id, product.getProductDisplayName());

      SearchStream<Product> stream = entityStream.of(Product.class);

      applyFilters(stream, gender, category);

      List<Product> products = stream
          .filter(Product$.IMAGE_EMBEDDING.knn(K, product.getImageEmbedding())) //
          .sorted(Product$._IMAGE_EMBEDDING_SCORE) //
          .limit(K) //
          .collect(Collectors.toList());

      setModelProperties(model, products, skip, gender, category);
    } else {
      logger.warn("🔎 vss :: There is no product with id {}", id);
    }

    return "fragments :: root";
  }

  @GetMapping("/filters")
  public String filters(
      Model model, //
      @RequestParam Optional<String> gender, //
      @RequestParam Optional<String> category, //
      @RequestParam Optional<Long> skip //
  ) {
    logger.info("🔎️ :: Setting Filters. Gender → {}, Category → {}", gender, category);

    setModelProperties(model, skip, gender, category);

    return "fragments :: filters";
  }

  private void setModelProperties(Model model, List<Product> products) {
    setModelProperties(model, Optional.of(products), Optional.empty(),Optional.empty(), Optional.empty());
  }

  private void setModelProperties(Model model, Optional<Long> skip, Optional<String> gender, Optional<String> category) {
    setModelProperties(model, Optional.empty(), skip, gender, category);
  }

  private void setModelProperties(Model model, List<Product> products, Optional<Long> skip, Optional<String> gender, Optional<String> category) {
    setModelProperties(model, Optional.of(products), skip, gender, category);
  }

  private void setModelProperties(Model model, Optional<List<Product>> products, Optional<Long> skip, Optional<String> gender, Optional<String> category) {
    model.addAttribute("totalNumberOfProducts", repository.count());

    if (skip.isPresent()) {
      model.addAttribute("skip", skip.get());
    }

    if (products.isPresent()) {
      model.addAttribute("products", products.get());
    }

    if (gender.isPresent() && !gender.get().equalsIgnoreCase("all")) {
      model.addAttribute("gender", gender.get());
    }

    if (category.isPresent() && !category.get().equalsIgnoreCase("all")) {
      model.addAttribute("category", category.get());
    }
  }

  private SearchStream<Product> applyFilters(SearchStream<Product> stream, Optional<String> gender, Optional<String> category) {

    if (gender.isPresent() && !gender.get().equalsIgnoreCase("all")) {
      stream.filter(Product$.GENDER.eq(gender.get()));
    }

    if (category.isPresent() && !category.get().equalsIgnoreCase("all")) {
      stream.filter(Product$.MASTER_CATEGORY.eq(category.get()));
    }

    return stream;
  }

}
