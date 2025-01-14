package com.ecom.Shopping_Cart.repository;

import com.ecom.Shopping_Cart.model.Watchlist;
import com.ecom.Shopping_Cart.model.UserDtls;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WatchlistRepository extends JpaRepository<Watchlist, Integer> {
    List<Watchlist> findByUser(UserDtls user);

    Watchlist findByUserAndProductId(UserDtls user, Integer productId);

    List<Watchlist> findByUserId(Integer userId);

    int countByUserId(Integer userId);

    Watchlist findByUserIdAndProductId(Integer userId, Integer productId);
}


