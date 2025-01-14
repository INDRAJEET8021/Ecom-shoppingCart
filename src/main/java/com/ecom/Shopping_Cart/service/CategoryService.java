package com.ecom.Shopping_Cart.service;

import com.ecom.Shopping_Cart.model.Category;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CategoryService {
    public Category saveCategory(Category category);

    public Boolean existCategory(String name);

    public List<Category> getAllCategory();

    public Boolean deleteCategory(int id);

    public Category getCategoryById(int id);

    public List<Category> getAllActiveCategory();

    // public Page<Category> getAllCategoryPagination(Integer pageNo, Integer pageSize);


}
