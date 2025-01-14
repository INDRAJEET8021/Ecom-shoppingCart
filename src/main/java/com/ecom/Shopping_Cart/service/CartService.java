package com.ecom.Shopping_Cart.service;

import com.ecom.Shopping_Cart.model.Cart;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CartService {
    public Cart saveCart(Integer productId, Integer userId);

    public List<Cart> getCartsByUser(Integer userId);

    public Integer getCountCart(Integer userId);

    void updateQuantity(String sy, Integer cid);

}
