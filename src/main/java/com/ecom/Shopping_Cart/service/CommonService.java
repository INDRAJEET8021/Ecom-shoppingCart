package com.ecom.Shopping_Cart.service;

import org.springframework.stereotype.Service;

@Service

public interface CommonService {
    public default void removeSessionMessage() {
    }
}
