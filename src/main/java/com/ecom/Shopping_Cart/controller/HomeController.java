package com.ecom.Shopping_Cart.controller;

import com.cloudinary.Cloudinary;
import com.ecom.Shopping_Cart.model.Category;
import com.ecom.Shopping_Cart.model.Product;
import com.ecom.Shopping_Cart.model.UserDtls;
import com.ecom.Shopping_Cart.service.*;
//import com.ecom.Shopping_Cart.util.BucketType;
import com.ecom.Shopping_Cart.util.CommonUtil;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Controller

public class HomeController {
    @Autowired
    CategoryService categoryService;
    @Autowired
    Cloudinary cloudinary;
    @Autowired
    ProductService productService;
    @Autowired
    UserService userService;
    @Autowired
    private CommonUtil commonUtil;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private CartService cartService;
    @Autowired
    private WatchlistService watchlistService;


    @ModelAttribute
    private void getUserDetails(Principal principal, Model model) {
        if (principal != null) {
            String email = principal.getName();
            UserDtls userDtls = userService.getUserByEmail(email);
            model.addAttribute("user", userDtls);
            Integer countCart = cartService.getCountCart(userDtls.getId());
            model.addAttribute("countCart", countCart);
        }
        List<Category> allActiveCategory = categoryService.getAllActiveCategory();
        model.addAttribute("category", allActiveCategory);
    }

    @GetMapping("/")
    public String index(Model model) {
        List<Category> allActiveCategory = categoryService.getAllActiveCategory().stream()
                .sorted((c1, c2) -> c2.getId().compareTo(c1.getId()))
                .limit(6).toList();
        List<Product> allActiveProducts = productService.getAllActiveProducts("").stream()
                .sorted((p1, p2) -> p2.getId().compareTo(p1.getId())).limit(8).toList();
        model.addAttribute("category", allActiveCategory);
        model.addAttribute("products", allActiveProducts);
        return "index";
    }

    @GetMapping("/signin")
    public String login() {
        return "login";
    }

    @GetMapping("register")
    public String register() {
        return "register";
    }


    @GetMapping("/products")
    public String products(Model m, @RequestParam(value = "category", defaultValue = "") String category,
                           @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
                           @RequestParam(name = "pageSize", defaultValue = "9") Integer pageSize,
                           @RequestParam(defaultValue = "") String ch) {

        List<Category> categories = categoryService.getAllActiveCategory();
        m.addAttribute("paramValue", category);
        m.addAttribute("categories", categories);

//		List<Product> products = productService.getAllActiveProducts(category);
//		m.addAttribute("products", products);
        Page<Product> page = null;
        if (StringUtils.isEmpty(ch)) {
            page = productService.getAllActiveProductPagination(pageNo, pageSize, category);
        } else {
            page = productService.searchActiveProductPagination(pageNo, pageSize, category, ch);
        }

        List<Product> products = page.getContent();
        m.addAttribute("products", products);
        m.addAttribute("productsSize", products.size());

        m.addAttribute("pageNo", page.getNumber());
        m.addAttribute("pageSize", pageSize);
        m.addAttribute("totalElements", page.getTotalElements());
        m.addAttribute("totalPages", page.getTotalPages());
        m.addAttribute("isFirst", page.isFirst());
        m.addAttribute("isLast", page.isLast());

        return "product";
    }


    @GetMapping("/product/{id}")
    public String product(@PathVariable int id, Model model, Principal principal) {
        Product productById = productService.getProductById(id);
        model.addAttribute("product", productById);

        if (principal != null) {
            UserDtls user = userService.getUserByEmail(principal.getName());
            model.addAttribute("user", user);
            int watchlistCount = watchlistService.getWishlistCount(user.getId()); // Assuming you have a method like this
            model.addAttribute("wishlistCount", watchlistCount);
            System.out.println(watchlistCount);
        }

        return "view_product";
    }
//    Old One
//
//    @PostMapping("/saveUser")
//    public String saveUser(@ModelAttribute UserDtls user,
//                           @RequestParam("img") MultipartFile file,
//                           HttpSession session) throws IOException {
//        Boolean existsEmail = userService.existEmail(user.getEmail());
//        if (existsEmail) {
//            session.setAttribute("errorMsg", "Email already exist,Please enter new one");
//        } else {
////            String imageName = file.isEmpty() ? "Default.jpg" : file.getOriginalFilename();
//            String imageUrl = commonUtil.getImageUrl(file, BucketType.PROFILE.getId());
//            user.setProfileImage(imageUrl);
//            UserDtls saveUser = userService.saveUser(user);
//
//            if (!ObjectUtils.isEmpty(saveUser)) {
//                if (!file.isEmpty()) {
////                    File saveFile = new ClassPathResource("static/img").getFile();
////                    Path path = Paths.get(saveFile.getAbsolutePath() + File.
////                            separator + "profile_img"
////                            + File.separator + file.getOriginalFilename());
////                    Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
////                System.out.println(path);
//                    fileService.uploadFileS3(file, BucketType.PROFILE.getId());
//                }
//                session.setAttribute("succMsg", "Registered Success");
//
//            } else {
//                session.setAttribute("errorMsg", "Can't Update Internal error");
//            }
//        }
//
//        return "redirect:/register";
//    }


    //    Saving Cloudinary
    @PostMapping("/saveUser")
    public String saveUser(@ModelAttribute UserDtls user,
                           @RequestParam("img") MultipartFile file,
                           HttpSession session) throws IOException {

        // Check if the email already exists
        Boolean existsEmail = userService.existEmail(user.getEmail());

        if (existsEmail) {
            session.setAttribute("errorMsg", "Email already exists");
            return "redirect:/register";
        }

        // Handle the image upload to Cloudinary
        String imageName;
        if (!file.isEmpty()) {
            try {
                if (file.getSize() > 10 * 1024 * 1024) { // File size limit: 10 MB
                    session.setAttribute("errorMsg", "File size exceeds the 10 MB limit");
                    return "redirect:/register";
                }

                // Upload the file to Cloudinary
                Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), Collections.emptyMap());
                imageName = (String) uploadResult.get("url"); // Get the URL of the uploaded image
            } catch (Exception e) {
                session.setAttribute("errorMsg", "Image upload failed: " + e.getMessage());
                return "redirect:/register";
            }
        } else {
            imageName = "default.jpg"; // Use a default image for users who don't upload a profile picture
        }

        // Set the profile image and save the user
        user.setProfileImage(imageName);
        UserDtls saveUser = userService.saveUser(user);

        if (!ObjectUtils.isEmpty(saveUser)) {
            session.setAttribute("succMsg", "Registered successfully");
        } else {
            session.setAttribute("errorMsg", "Something went wrong on the server");
        }

        return "redirect:/register";
    }


//    Saving Image in local file->fetch
//    @PostMapping("/saveUser")
//    public String saveUser(@ModelAttribute UserDtls user, @RequestParam("img") MultipartFile file, HttpSession session)
//            throws IOException {
//
//        Boolean existsEmail = userService.existEmail(user.getEmail());
//
//        if (existsEmail) {
//            session.setAttribute("errorMsg", "Email already exist");
//        } else {
//            String imageName = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
//            user.setProfileImage(imageName);
//            UserDtls saveUser = userService.saveUser(user);
//
//            if (!ObjectUtils.isEmpty(saveUser)) {
//                if (!file.isEmpty()) {
//                    File saveFile = new ClassPathResource("static/img").getFile();
//
//                    Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "profile_img" + File.separator
//                            + file.getOriginalFilename());
//
////					System.out.println(path);
//                    Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
//                }
//                session.setAttribute("succMsg", "Register successfully");
//            } else {
//                session.setAttribute("errorMsg", "something wrong on server");
//            }
//        }
//
//        return "redirect:/register";
//    }

    @GetMapping("/forgot-password")
    public String showPassword() {
        return "forgot_password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email,
                                        HttpSession session,
                                        HttpServletRequest request) throws MessagingException, UnsupportedEncodingException {
        UserDtls userByEmail = userService.getUserByEmail(email);
        if (ObjectUtils.isEmpty(userByEmail)) {
            session.setAttribute("errorMsg", "Invalid Email enter again");
        } else {
            String resetToken = UUID.randomUUID().toString();
            userService.updateUserResetToken(email, resetToken);

            String url = CommonUtil.generateUrl(request) + "/reset-password?token=" + resetToken;
//            System.out.println(url);
            Boolean sendMail = commonUtil.sendMail(url, email);
            // System.out.println(sendMail);
            if (sendMail) {
                session.setAttribute("succMsg", "Link sent.. Check your mail");
            } else {
                session.setAttribute("errorMsg", "Unable to Reset");
            }
        }

        return "redirect:/forgot-password";
    }


    @GetMapping("/reset-password")
    public String showResetPassword(@RequestParam String token, Model model) {
        UserDtls userByToken = userService.getUserByToken(token);
        if (ObjectUtils.isEmpty(userByToken)) {
            model.addAttribute("msg", "Invalid Url or Your Link is expired");
            return "message";
        }
        model.addAttribute("token", token);

        return "reset_password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token, Model model,
                                @RequestParam String password,
                                HttpSession httpSession) {
        UserDtls userByToken = userService.getUserByToken(token);
        if (ObjectUtils.isEmpty(userByToken)) {
            model.addAttribute("msg", "Invalid Url or Your Link is expired");
            return "message";
        } else {
            userByToken.setPassword(passwordEncoder.encode(password));
            userByToken.setResetToken(null);
            userService.updateUser(userByToken);
            httpSession.setAttribute("succMsg", "Password Changed SuccessFully");
//            model.addAttribute("msg", "Password Changed SuccessFully")
            return "reset_password";
        }
    }

    @GetMapping("/search")
    public String searchProduct(@RequestParam String ch, Model model) {
        List<Product> searchProducts = productService.searchProduct(ch);
        model.addAttribute("products", searchProducts);

        List<Category> categories = categoryService.getAllActiveCategory();
        model.addAttribute("categories", categories);
        return "product";
    }

    @GetMapping("/admin-login")
    public String adminLogin() {
        return "admin_login";
    }
}
