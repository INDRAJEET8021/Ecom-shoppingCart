package com.ecom.Shopping_Cart.service;

import com.ecom.Shopping_Cart.model.Product;
import com.ecom.Shopping_Cart.model.UserDtls;
import com.ecom.Shopping_Cart.model.Watchlist;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface WatchlistService {


    public List<Product> getWishlistProducts(Integer userId);

    public boolean removeProductFromWishlist(String userEmail, Integer productId);

    public boolean toggleProductInWishlist(String userEmail, Integer productId);

    public int getWishlistCount(Integer userId);

    public boolean isProductInWatchlist(Integer userId, Integer productId);
}
