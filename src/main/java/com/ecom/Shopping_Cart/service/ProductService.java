package com.ecom.Shopping_Cart.service;

import com.ecom.Shopping_Cart.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service


public interface ProductService {
    public Product saveProduct(Product product);

    List<Product> getAllProducts();

    public Boolean deleteProduct(Integer id);

    public Product getProductById(Integer id);

    public Product updateProduct(Product product, MultipartFile file);

    public List<Product> getAllActiveProducts(String category);

    public List<Product> searchProduct(String ch);
//    new aaya hai String search


    public Page<Product> getAllActiveProductPagination(Integer pageNo, Integer pageSize,
                                                       String category);

    public Page<Product> searchProductPagination(Integer pageNo, Integer pageSize, String ch);

    public Page<Product> getAllProductsPagination(Integer pageNo, Integer pageSize);

    Page<Product> searchActiveProductPagination(Integer pageNo, Integer pageSize, String category, String ch);

}
