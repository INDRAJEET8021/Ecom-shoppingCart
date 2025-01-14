package com.ecom.Shopping_Cart.serviceImpl;

import com.cloudinary.Cloudinary;
import com.ecom.Shopping_Cart.model.Product;
import com.ecom.Shopping_Cart.repository.ProductRepository;
//import com.ecom.Shopping_Cart.service.FileService;
import com.ecom.Shopping_Cart.service.ProductService;
//import com.ecom.Shopping_Cart.util.BucketType;
import com.ecom.Shopping_Cart.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service

public class ProductServiceImpl implements ProductService {
    @Autowired
    ProductRepository productRepository;
    @Autowired
    CommonUtil commonUtil;
    @Autowired
    Cloudinary cloudinary;

    @Override
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Boolean deleteProduct(Integer id) {
        Product product = productRepository.findById(id).orElse(null);

        if (!ObjectUtils.isEmpty(product)) {
            productRepository.delete(product);
            return true;
        }
        return false;

    }

    @Override
    public Product getProductById(Integer id) {
        Product product = productRepository.findById(id).orElse(null);
        return product;
    }

    //    Boolean existsEmail = userService.existEmail(user.getEmail());
    @Override
    public Product updateProduct(Product product, MultipartFile image) {
        Product dbProduct = getProductById(product.getId());  // Retrieve the existing product by ID

        if (dbProduct == null) {
            // Handle the case where the product doesn't exist
            throw new IllegalArgumentException("Product not found with ID: " + product.getId());
        }

        // Update product fields with the values from the provided product object
        dbProduct.setTitle(product.getTitle());
        dbProduct.setCategory(product.getCategory());
        dbProduct.setDescription(product.getDescription());
        dbProduct.setPrice(product.getPrice());
        dbProduct.setStock(product.getStock());
        dbProduct.setActive(product.getActive());
        dbProduct.setDiscount(product.getDiscount());

        // Calculate the discount price
        Double discount = product.getPrice() * (product.getDiscount() / 100.0);
        Double discountPrice = product.getPrice() - discount;
        dbProduct.setDiscountPrice(discountPrice);

        // Update the image only if a new one is provided
        try {
            if (!image.isEmpty()) {
                if (image.getSize() > 10 * 1024 * 1024) {
                    throw new IllegalArgumentException("File is too large. Please upload an image smaller than 10 MB.");
                }

                // Upload the image to Cloudinary
                Map<String, Object> uploadResult = cloudinary.uploader().upload(image.getBytes(), Collections.emptyMap());
                String imageUrl = (String) uploadResult.get("url");
                dbProduct.setImage(imageUrl);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error occurred while uploading the image", e);
        }

        // Save the updated product in the database
        return productRepository.save(dbProduct);
    }

    @Override
    public List<Product> getAllActiveProducts(String category) {
        List<Product> products = null;
        if (ObjectUtils.isEmpty(category)) {
            products = productRepository.findByIsActiveTrue();
        } else {
            products = productRepository.findByCategory(category);
        }


        return products;
    }

    @Override
    public List<Product> searchProduct(String ch) {
        return productRepository.findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(ch, ch);
    }

//    Actual wala hai

    @Override
    public Page<Product> getAllActiveProductPagination(Integer pageNo, Integer pageSize,
                                                       String category) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<Product> pageProduct = null;

        if (ObjectUtils.isEmpty(category)) {
            pageProduct = productRepository.findByIsActiveTrue(pageable);
        } else {
            pageProduct = productRepository.findByCategory(pageable, category);
        }

        return pageProduct;
    }

//    @Override
//    public Page<Product> getAllActiveProductPagination(Integer pageNo, Integer pageSize,
//                                                       String category, String search) {
//        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("title").ascending());
//
//        if (ObjectUtils.isEmpty(category) && ObjectUtils.isEmpty(search)) {
//            return productRepository.findByIsActiveTrue(pageable);
//        } else if (ObjectUtils.isEmpty(category)) {
//            return productRepository.findByTitleContainingIgnoreCaseAndIsActiveTrue(search, pageable);
//        } else if (ObjectUtils.isEmpty(search)) {
//            return productRepository.findByCategoryAndIsActiveTrue(pageable, category);
//        } else {
//            return productRepository.findByCategoryAndTitleContainingIgnoreCaseAndIsActiveTrue(category, search, pageable);
//        }
//    }

    @Override
    public Page<Product> searchProductPagination(Integer pageNo, Integer pageSize, String ch) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return productRepository.findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(ch, ch, pageable);
    }

    @Override
    public Page<Product> getAllProductsPagination(Integer pageNo, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        return productRepository.findAll(pageable);
    }

    @Override
    public Page<Product> searchActiveProductPagination(Integer pageNo, Integer pageSize, String category, String ch) {
        Page<Product> pageProduct = null;
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        pageProduct = productRepository.findByisActiveTrueAndTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(ch, ch, pageable);
//        if (ObjectUtils.isEmpty(category)) {
//            pageProduct = productRepository.findByIsActiveTrue(pageable);
//        } else {
//            pageProduct = productRepository.findByCategory(pageable, category);
//        }
        return pageProduct;
    }


}
