package com.ecom.Shopping_Cart.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service

public interface CloudinaryService {
    public String uploadFile(MultipartFile file);
}
