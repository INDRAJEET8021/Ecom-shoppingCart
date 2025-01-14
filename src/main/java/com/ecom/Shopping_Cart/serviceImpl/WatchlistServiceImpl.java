package com.ecom.Shopping_Cart.serviceImpl;

import com.ecom.Shopping_Cart.model.Product;
import com.ecom.Shopping_Cart.model.UserDtls;
import com.ecom.Shopping_Cart.model.Watchlist;
import com.ecom.Shopping_Cart.repository.ProductRepository;
import com.ecom.Shopping_Cart.repository.UserRepository;
import com.ecom.Shopping_Cart.repository.WatchlistRepository;
import com.ecom.Shopping_Cart.service.UserService;
import com.ecom.Shopping_Cart.service.WatchlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WatchlistServiceImpl implements WatchlistService {
    @Autowired
    private WatchlistRepository watchlistRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;


    public List<Product> getWishlistProducts(Integer userId) {
        // Fetch the watchlist entries and extract products
        List<Watchlist> watchlistEntries = watchlistRepository.findByUserId(userId);
        return watchlistEntries.stream()
                .map(Watchlist::getProduct)
                .collect(Collectors.toList());
    }

    public int getWishlistCount(Integer userId) {
        return watchlistRepository.countByUserId(userId);
    }


    public boolean removeProductFromWishlist(String userEmail, Integer productId) {
        UserDtls user = userRepository.findByEmail(userEmail);
        Watchlist watchlistItem = watchlistRepository.findByUserAndProductId(user, productId);
        if (watchlistItem != null) {
            watchlistRepository.delete(watchlistItem);
            return true;
        }
        return false;
    }

    public boolean toggleProductInWishlist(String userEmail, Integer productId) {
        UserDtls user = userRepository.findByEmail(userEmail);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        Product product = productRepository.findById(productId).orElseThrow(() -> new IllegalArgumentException("Product not found"));

        Watchlist watchlistItem = watchlistRepository.findByUserAndProductId(user, productId);
        if (watchlistItem != null) {
            watchlistRepository.delete(watchlistItem);
            return false; // Removed from wishlist
        } else {
            Watchlist newItem = new Watchlist();
            newItem.setUser(user);
            newItem.setProduct(product);
            watchlistRepository.save(newItem);
            return true; // Added to wishlist
        }
    }

    public boolean isProductInWatchlist(Integer userId, Integer productId) {
        Watchlist watchlist = watchlistRepository.findByUserIdAndProductId(userId, productId);
        return watchlist != null;
    }


}