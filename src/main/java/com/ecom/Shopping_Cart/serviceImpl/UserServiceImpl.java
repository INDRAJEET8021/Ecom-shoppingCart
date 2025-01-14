package com.ecom.Shopping_Cart.serviceImpl;

import com.cloudinary.Cloudinary;
import com.ecom.Shopping_Cart.model.UserDtls;
import com.ecom.Shopping_Cart.repository.UserRepository;
//import com.ecom.Shopping_Cart.service.FileService;
import com.ecom.Shopping_Cart.service.UserService;
import com.ecom.Shopping_Cart.util.AppConstant;
//import com.ecom.Shopping_Cart.util.BucketType;
import com.ecom.Shopping_Cart.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Service

public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private Cloudinary cloudinary;
    @Autowired
    private PasswordEncoder passwordEncoder;

    //    @Autowired
    @Lazy
    CommonUtil commonUtil;

    @Override
    public UserDtls saveUser(UserDtls user) {

        user.setRole("ROLE_USER");
        user.setEnable(true);
        user.setAccountNonLocked(true);
        user.setFailedAttempt(0);
        String encodePassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodePassword);
        UserDtls saveUser = userRepository.save(user);
        return saveUser;
    }

    @Override
    public UserDtls getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public List<UserDtls> getUsers(String role) {
        return userRepository.findByRole(role);
    }

    @Override
    public Boolean updateAccountStatus(Integer id, Boolean status) {
        Optional<UserDtls> findByUser = userRepository.findById(id);
        if (findByUser.isPresent()) {
            UserDtls userDtls = findByUser.get();
            userDtls.setEnable(status);
            userRepository.save(userDtls);
            return true;
        }
        return false;
    }

    @Override
    public void increaseFailedAttempt(UserDtls user) {
        int attempt = user.getFailedAttempt() + 1;
        user.setFailedAttempt(attempt);
        userRepository.save(user);
    }

    @Override
    public void userAccountLock(UserDtls user) {
        user.setAccountNonLocked(false);
        user.setLockTime(new Date());
        userRepository.save(user);
    }

    @Override
    public Boolean unlockAccountTimeExpired(UserDtls user) {
        Long lockTime = user.getLockTime().getTime();
        Long unLockTime = lockTime + AppConstant.UNLOCK_DURATION_TIME;
        Long currentTime = System.currentTimeMillis();
        if (unLockTime < currentTime) {
            user.setAccountNonLocked(true);
            user.setFailedAttempt(0);
            user.setLockTime(null);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Override
    public void resetAttempt(int userId) {

    }

    @Override
    public void updateUserResetToken(String email, String resetToken) {
        UserDtls findByEmail = userRepository.findByEmail(email);
        findByEmail.setResetToken(resetToken);
        userRepository.save(findByEmail);

    }

    @Override
    public UserDtls getUserByToken(String token) {
        return userRepository.findByResetToken(token);
    }

    @Override
    public UserDtls updateUser(UserDtls user) {
        return userRepository.save(user);
    }

    //    @Override
//    public UserDtls updateUserProfile(UserDtls user, MultipartFile img) {
//
//        UserDtls dbUser = userRepository.findById(user.getId()).get();
//
//        //Logic to save on local storage
////        if (!img.isEmpty()) {
////            dbUser.setProfileImage(img.getOriginalFilename());
////        }
//
//        //save in cloudinnary
//
//        if (!img.isEmpty()) {
//            try {
//                if (img.getSize() > 10 * 1024 * 1024) { // 10 MB limit
//                    throw new RuntimeException("File size exceeds the 10 MB limit");
//                } else {
//                    Map uploadResult = cloudinary.uploader().upload(img.getBytes(), Collections.emptyMap());
//                    String imageUrl = (String) uploadResult.get("url");
//                    dbUser.setProfileImage(imageUrl); // Save the URL in the database
//                }
//
//
//            } catch (IOException e) {
//                throw new RuntimeException("Image upload failed", e);
//            }
//
//        }
//
//
//        if (!ObjectUtils.isEmpty(dbUser)) {
//
//            dbUser.setName(user.getName());
//            dbUser.setMobileNumber(user.getMobileNumber());
//            dbUser.setAddress(user.getAddress());
//            dbUser.setCity(user.getCity());
//            dbUser.setState(user.getState());
//            dbUser.setPincode(user.getPincode());
//            dbUser = userRepository.save(dbUser);
//        }
//
//        try {
//            if (!img.isEmpty()) {
//                File saveFile = new ClassPathResource("static/img").getFile();
//
//                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "profile_img" + File.separator
//                        + img.getOriginalFilename());
//
////			System.out.println(path);
//                Files.copy(img.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return dbUser;
//    }
    @Override
    public UserDtls updateUserProfile(UserDtls user, MultipartFile img) {
        // Retrieve the user from the database
        UserDtls dbUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Upload the image to Cloudinary if provided
        if (!img.isEmpty()) {
            try {
                if (img.getSize() > 10 * 1024 * 1024) { // 10 MB limit
                    throw new RuntimeException("Image File is Too large ReUpload");
                }
                // Upload image to Cloudinary
                Map<String, Object> uploadResult = cloudinary.uploader().upload(img.getBytes(), Collections.emptyMap());
                String imageUrl = (String) uploadResult.get("url");

                // Save the Cloudinary URL to the user's profileImage field
                dbUser.setProfileImage(imageUrl);
            } catch (RuntimeException e) {
                throw e; // Re-throw the exception to be handled in the controller
            } catch (IOException e) {
                throw new RuntimeException("Image upload failed", e);
            }
        }

        // Update other user details
        dbUser.setName(user.getName());
        dbUser.setMobileNumber(user.getMobileNumber());
        dbUser.setAddress(user.getAddress());
        dbUser.setCity(user.getCity());
        dbUser.setState(user.getState());
        dbUser.setPincode(user.getPincode());

        // Save the updated user details to the database
        return userRepository.save(dbUser);
    }

    @Override
    public UserDtls saveAdmin(UserDtls user) {
        user.setRole("ROLE_ADMIN");
        user.setEnable(true);
        user.setAccountNonLocked(true);
        user.setFailedAttempt(0);
        String encodePassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodePassword);
        UserDtls saveUser = userRepository.save(user);
        return saveUser;
    }

    @Override
    public Boolean existEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public UserDtls findById(Integer id) {
        return userRepository.findById(id).orElse(null);
    }
}
