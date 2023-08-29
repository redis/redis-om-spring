package com.redis.om.vss.ui;

import com.redis.om.vss.domain.Product;
import lombok.Builder;
import org.springframework.ui.Model;

import java.util.List;

@Builder(builderMethodName = "of")
public class ProductsPageModel {
  private Model model;
  private List<Product> products;
  private List<Double> scores;
  private Long skip;
  private String gender;
  private String category;
  private Long count;

  public void apply() {
    if (products != null) {
      model.addAttribute("products", products);
    }

    if (scores != null) {
      model.addAttribute("scores", scores);
    }

    if (skip != null) {
      model.addAttribute("skip", skip);
    }

    if (gender != null && !gender.equalsIgnoreCase("all")) {
      model.addAttribute("gender", gender);
    }

    if (category != null && !category.equalsIgnoreCase("all")) {
      model.addAttribute("category", category);
    }

    if (count != null) {
      model.addAttribute("totalNumberOfProducts", count);
    }
  }
}
