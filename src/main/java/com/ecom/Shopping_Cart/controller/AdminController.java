package com.ecom.Shopping_Cart.controller;


import com.cloudinary.Cloudinary;
import com.ecom.Shopping_Cart.model.Category;
import com.ecom.Shopping_Cart.model.Product;
import com.ecom.Shopping_Cart.model.ProductOrder;
import com.ecom.Shopping_Cart.model.UserDtls;
import com.ecom.Shopping_Cart.service.*;
import com.ecom.Shopping_Cart.util.CommonUtil;
import com.ecom.Shopping_Cart.util.OrderStatus;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
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

@Controller
@RequestMapping("/admin")

public class AdminController {
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;
    @Autowired
    private CartService cartService;
    @Autowired
    protected OrderService orderService;

    @Autowired
    CommonUtil commonUtil;


    @Autowired
    private PasswordEncoder passwordEncoder;

    @ModelAttribute
    private void getUserDetails(Principal principal, Model model) {
        try {
            if (principal != null) {
                String email = principal.getName();
                UserDtls userDtls = userService.getUserByEmail(email);

                if (userDtls != null) {
                    model.addAttribute("user", userDtls);
                    Integer countCart = cartService.getCountCart(userDtls.getId());
                    model.addAttribute("countCart", countCart);
                } else {
                    model.addAttribute("user", null);
                    model.addAttribute("countCart", 0); // Assuming default value if user is not found
                }
            }

            List<Category> allActiveCategory = categoryService.getAllActiveCategory();
            model.addAttribute("category", allActiveCategory);

        } catch (Exception e) {
            model.addAttribute("error", "An unexpected error occurred: " + e.getMessage());
            // Log the error for debugging
            System.err.println("Error in getUserDetails: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @GetMapping("/")
    public String index() {
        return "admin/index";
    }

    @GetMapping("/loadAddProduct")
    public String loadAddProduct(Model model) {
        List<Category> categories = categoryService.getAllCategory();
        model.addAttribute("categories", categories);

        return "admin/add_product";
    }

    //    @GetMapping("/category")
//    public String category(Model model, @RequestParam(name = "pageNo", defaultValue = "0")
//    Integer pageNo, @RequestParam(name = "pageSize", defaultValue = "3") Integer pageSize) {
//        Page<Category> page = categoryService.getAllCategoryPagination(pageNo, pageSize);
//        List<Category> categorys = page.getContent();
//
//        model.addAttribute("categorys", categorys);
//        model.addAttribute("pageNo", page.getNumber());
//        model.addAttribute("pageSize", pageSize);
//        model.addAttribute("totalElement", page.getTotalElements());
//        model.addAttribute("totalPages", page.getTotalPages());
//        model.addAttribute("isFirst", page.isFirst());
//        model.addAttribute("isLast", page.isLast());
//
//        return "admin/category";
//    }
    @GetMapping("/category")
    public String category(Model model) {
        List<Category> categories = categoryService.getAllCategory();

        model.addAttribute("categorys", categoryService.getAllCategory());
        return "admin/category";
    }

//Old one
//    @PostMapping("/saveCategory")
//    public String saveCategory(@RequestParam("name") String name,
//                               @RequestParam("isActive") boolean isActive,
//                               @RequestParam("file") MultipartFile file,
//                               HttpSession session) {
//
//        try {
//            if (categoryService.existCategory(name)) {
//                session.setAttribute("errorMsg", "Category Name already exists");
//                return "redirect:/admin/category";
//            }
//            Category category = new Category();
//            category.setName(name);
//            category.setActive(isActive);
//
//            String imageUrl = commonUtil.getImageUrl(file, BucketType.CATEGORY.getId());
//            category.setImageName(imageUrl);
//
//            if (!file.isEmpty()) {
//                String imageName = file.getOriginalFilename();
//                Path imagePath = Paths.get("src/main/resources/static/img/category_img/" + imageName);
//                Files.write(imagePath, file.getBytes());
//                category.setImageName(imageName);
//            }
////
//            Category saveCategory = categoryService.saveCategory(category);
////            fileService.uploadFileS3(file, 1);
//            session.setAttribute("succMsg", "Category saved successfully");
//
//        } catch (Exception e) {
//            session.setAttribute("errorMsg", "Failed to save category");
//        }
//
//        return "redirect:/admin/category";
//    }


    @PostMapping("/saveCategory")
    public String saveCategory(@RequestParam("name") String name,
                               @RequestParam("isActive") boolean isActive,
                               @RequestParam("file") MultipartFile file,
                               HttpSession session) {

        try {
            // Check if the category already exists
            if (categoryService.existCategory(name)) {
                session.setAttribute("errorMessage", "Category Name already exists");
                return "redirect:/admin/category";
            }

            // Validate if file is empty
            if (file.isEmpty()) {
                session.setAttribute("errorMessage", "Please upload a valid category image");
                return "redirect:/admin/category";
            }

            // Create and populate category object
            Category category = new Category();
            category.setName(name);
            category.setActive(isActive);

            if (file.getSize() > 10 * 1024 * 1024) { // 10 MB limit
                throw new RuntimeException("File size exceeds the 10 MB limit");
            }

//            Cloudinary Image save
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), Collections.emptyMap());
            String imageUrl = (String) uploadResult.get("url");


            // Handle file upload Normally
//            String imageName = file.getOriginalFilename();
//            Path imagePath = Paths.get("src/main/resources/static/img/category_img/" + imageName);
//            Files.write(imagePath, file.getBytes());
//            category.setImageName(imageName);

            category.setImageName(imageUrl);
            // Save category
            categoryService.saveCategory(category);

            // Success message
            session.setAttribute("successMessage", "Category saved successfully");
        } catch (IOException e) {
            session.setAttribute("errorMessage", "Error occurred while uploading the file");
        } catch (Exception e) {
            session.setAttribute("errorMessage", "Failed to save category. Please try again.");
        }

        return "redirect:/admin/category";
    }


    @GetMapping("/deleteCategory/{id}")
    public String deleteCategory(@PathVariable int id, HttpSession httpSession) {
        Boolean deleteCategory = categoryService.deleteCategory(id);

        if (deleteCategory) {
            httpSession.setAttribute("succMsg", "Category Deleted Succefully");
        } else {
            httpSession.setAttribute("errorMsg", "Can't Detele Internal Error");
        }
        return "redirect:/admin/category";
    }

    @GetMapping("/loadEditCategory/{id}")
    public String loadEditCategory(@PathVariable int id, Model model) {
        model.addAttribute("category", categoryService.getCategoryById(id));
        return "admin/edit_category";
    }

//    @PostMapping("/updateCategory")
//    public String updateCategory(@RequestParam("id") int id,
//                                 @RequestParam("name") String name,
//                                 @RequestParam("isActive") boolean isActive,
//                                 @RequestParam("file") MultipartFile file,
//                                 HttpSession session) {
//        try {
//            // Retrieve the existing category by ID
//            Category oldCategory = categoryService.getCategoryById(id);
//            String imageUrl = commonUtil.getImageUrl(file, BucketType.CATEGORY.getId());
//
//            if (oldCategory == null) {
//                session.setAttribute("errorMsg", "Category not found");
//                return "redirect:/admin/loadEditCategory/" + id;
//            }
//
//            // Update the category details
//            oldCategory.setName(name);
//            oldCategory.setActive(isActive);
//
//            // Handle file upload if a new file is provided
//            if (!file.isEmpty()) {
//                oldCategory.setImageName(imageUrl);
//
//                String imageName = file.getOriginalFilename();
//                oldCategory.setImageName(imageName);
//
////                 Save the file to the designated path
//                File saveFile = new ClassPathResource("static/img").getFile();
//                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator
//                        + "category_img" + File.separator + imageName);
//                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
//
////                fileService.uploadFileS3(file, 1);
//
//            }
//
//            // Save the updated category
//            categoryService.saveCategory(oldCategory);
//            session.setAttribute("succMsg", "Updated Successfully");
//
//        } catch (Exception e) {
//            session.setAttribute("errorMsg", "An error occurred while updating the category: " + e.getMessage());
//            e.printStackTrace();
//        }
//
//        return "redirect:/admin/loadEditCategory/" + id;
//    }

    //New One
    @PostMapping("/updateCategory")
    public String updateCategory(@RequestParam("id") int id,
                                 @RequestParam("name") String name,
                                 @RequestParam("isActive") boolean isActive,
                                 @RequestParam("file") MultipartFile file,
                                 HttpSession session) {
        try {
            // Retrieve the existing category by ID
            Category existingCategory = categoryService.getCategoryById(id);

            if (existingCategory == null) {
                session.setAttribute("errorMsg", "Category not found");
                return "redirect:/admin/loadEditCategory/" + id;
            }


            // Update the category details
            existingCategory.setName(name);
            existingCategory.setActive(isActive);

            // Handle file upload
            if (!file.isEmpty()) {

                if (file.getSize() > 10 * 1024 * 1024) { // 10 MB limit
                    throw new RuntimeException("File size exceeds the 10 MB limit");
                }

//            Cloudinary Image save
                Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), Collections.emptyMap());
                String imageUrl = (String) uploadResult.get("url");
                existingCategory.setImageName(imageUrl);

//                Below Are the method to upload in local file

//                String imageName = file.getOriginalFilename();
//                // Save the file to the designated path
//                Path imagePath = Paths.get("src/main/resources/static/img/category_img/" + imageName);
//                Files.write(imagePath, file.getBytes());
//                // Update the category's image name
//                existingCategory.setImageName(imageName);
            }

            // Save the updated category
            categoryService.saveCategory(existingCategory);
            session.setAttribute("succMsg", "Category updated successfully");
        } catch (Exception e) {
            session.setAttribute("errorMsg", "An error occurred while updating the category");
            e.printStackTrace();
        }

        return "redirect:/admin/loadEditCategory/" + id;
    }

//    Old One
//    @PostMapping("/saveProduct")
//    public String saveProduct(@ModelAttribute Product product,
//                              @RequestParam("file") MultipartFile image,
//                              HttpSession session) throws IOException {
//        String imageName = image.isEmpty() ? "default.jpg" : image.getOriginalFilename();
//        product.setImage(imageName);
//
////        ?\FOR AWS
////        String imageUrl = commonUtil.getImageUrl(image, BucketType.PRODUCT.getId());
////        product.setImage(imageUrl);
//        product.setDiscount(0);
//        product.setDiscountPrice(product.getPrice());
//
//        Product saveProduct = productService.saveProduct(product);
//
//        if (!ObjectUtils.isEmpty(saveProduct)) {
//
//            File saveFile = new ClassPathResource("static/img").getFile();
//            Path path = Paths.get(saveFile.getAbsolutePath() + File.
//                    separator + "product_img"
//                    + File.separator + image.getOriginalFilename());
//            System.out.println(path);
//            Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
//
////            fileService.uploadFileS3(image, BucketType.PRODUCT.getId());
//            session.setAttribute("succMsg", "Successfully Saved Product");
//        } else {
//            session.setAttribute("errorMsg", "Product Save Failed");
//        }
//
//        return "redirect:/admin/loadAddProduct";
//
//    }

    @PostMapping("/saveProduct")
    public String saveProduct(@ModelAttribute Product product, @RequestParam("file") MultipartFile image,
                              HttpSession session) throws IOException {

        try {
            if (image.isEmpty()) {
                session.setAttribute("errorMessage", "Please Upload an Image");
                return "redirect:/admin/loadAddProduct";

            }

            if (image.getSize() > 10 * 1024 * 1024) {
                session.setAttribute("errorMessage", "File Is Too large Please ReUpload");
                return "redirect:/admin/loadAddProduct";
            }
//            Cloudinary Image save
            Map<String, Object> uploadResult = cloudinary.uploader().upload(image.getBytes(), Collections.emptyMap());
            String imageUrl = (String) uploadResult.get("url");

            product.setDiscount(0);
            product.setDiscountPrice(product.getPrice());
            product.setImage(imageUrl);
            Product saveProduct = productService.saveProduct(product);


            if (saveProduct != null) {
                session.setAttribute("succMsg", "Product saved successfully");
            } else {
                session.setAttribute("errorMessage", "Failed to save the product. Please try again.");
            }

        } catch (IOException e) {
            session.setAttribute("errorMessage", "Error occurred while uploading the file");
        } catch (Exception e) {
            session.setAttribute("errorMessage", "Failed to save category. Please try again.");
        }

//        Oldest Methdod
//        String imageName = image.isEmpty() ? "default.jpg" : image.getOriginalFilename();
//        product.setImage(imageName);
//        if (!ObjectUtils.isEmpty(saveProduct)) {
//
//            File saveFile = new ClassPathResource("static/img").getFile();
//            Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "product_img" + File.separator
//                    + image.getOriginalFilename());
//
//            // System.out.println(path);
//            Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
//
//            session.setAttribute("succMsg", "Product Saved Success");
//        } else {
//            session.setAttribute("errorMsg", "something wrong on server");
//        }

        return "redirect:/admin/loadAddProduct";
    }

    @GetMapping("/ViewProducts")
    public String loadViewProducts(Model model, @RequestParam(defaultValue = "") String ch,
                                   @RequestParam(name = "pageNo", defaultValue = "0")
                                   Integer pageNo, @RequestParam(name = "pageSize", defaultValue = "5")
                                   Integer pageSize) {
//        List<Product> products = null;
//        if (ch != null && ch.length() > 0) {
//            products = productService.searchProduct(ch);
//        } else {
//            products = productService.getAllProducts();
//        }
//        model.addAttribute("products", products);

        Page<Product> page = null;
        if (ch != null && ch.length() > 0) {
            page = productService.searchProductPagination(pageNo, pageSize, ch);

        } else {
            page = productService.getAllProductsPagination(pageNo, pageSize);
        }
        model.addAttribute("products", page.getContent());
        model.addAttribute("pageNo", page.getNumber());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalElement", page.getTotalElements());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("isFirst", page.isFirst());
        model.addAttribute("isLast", page.isLast());
        return "admin/products";
    }

    @GetMapping("/deleteProduct/{id}")
    public String DeleteProduct(@PathVariable int id, HttpSession session) {
        Boolean deleteProduct = productService.deleteProduct(id);
        if (deleteProduct) {
            session.setAttribute("succMsg", "Product Deleted Successfully");
        } else {
            session.setAttribute("errorMsg", "Error :Can't Delete Product");
        }
        return "redirect:/admin/ViewProducts";
    }

    @GetMapping("/editProduct/{id}")
    public String editProduct(@PathVariable int id, Model m) {
        m.addAttribute("product", productService.getProductById(id));
        m.addAttribute("categories", categoryService.getAllCategory());
        return "admin/edit_product";
    }

    @PostMapping("/updateProduct")
    public String updateProduct(@ModelAttribute Product product, @RequestParam("file") MultipartFile image,
                                HttpSession session, Model m) {

        if (product.getDiscount() < 0 || product.getDiscount() > 100) {
            session.setAttribute("errorMsg", "invalid Discount");
        } else {
            Product updateProduct = productService.updateProduct(product, image);
            if (!ObjectUtils.isEmpty(updateProduct)) {
                session.setAttribute("succMsg", "Product update success");
            } else {
                session.setAttribute("errorMsg", "Something wrong on server");
            }
        }
        return "redirect:/admin/editProduct/" + product.getId();
    }

    @GetMapping("/users")
    public String getAllUsers(Model model, @RequestParam Integer type) {
        List<UserDtls> users = null;
        if (type == 1) {
            users = userService.getUsers("ROLE_USER");
        } else {
            users = userService.getUsers("ROLE_ADMIN");
        }
        model.addAttribute("userType", type);
        model.addAttribute("users", users);
        return "admin/users";
    }

    @GetMapping("/updateSts")
    public String updateUserAccountStatus(@RequestParam Boolean status,
                                          @RequestParam Integer id,
                                          HttpSession session, @RequestParam Integer type) {
        Boolean f = userService.updateAccountStatus(id, status);
        if (f) {
            session.setAttribute("succMsg", "Account Status Updated");
        } else {
            session.setAttribute("errorMsg", "Something wrong on server");
        }
        return "redirect:/admin/users?type=" + type;
    }

    @GetMapping("/orders")
    public String getAllOrders(Model model, @RequestParam(defaultValue = "") String ch,
                               @RequestParam(name = "pageNo", defaultValue = "0")
                               Integer pageNo, @RequestParam(name = "pageSize", defaultValue = "5")
                               Integer pageSize) {
//        List<ProductOrder> allOrders = orderService.getAllOrders();
//        model.addAttribute("orders", allOrders);
//        model.addAttribute("srch", false);
        Page<ProductOrder> page = orderService.getAllOrdersPagination(pageNo, pageSize);
        model.addAttribute("orders", page.getContent());
        model.addAttribute("srch", false);

        model.addAttribute("pageNo", page.getNumber());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("totalElement", page.getTotalElements());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("isFirst", page.isFirst());
        model.addAttribute("isLast", page.isLast());

        return "admin/orders";
    }

    @PostMapping("/update-order-status")
    public String updateOrderStatus(@RequestParam Integer id,
                                    @RequestParam Integer st,
                                    HttpSession session) {

        OrderStatus[] values = OrderStatus.values();
        String status = null;

        for (OrderStatus orderSt : values) {
            if (orderSt.getId().equals(st)) {
                status = orderSt.getName();
            }
        }

        ProductOrder updateOrder = orderService.updateOrderStatus(id, status);

        try {
            commonUtil.sendMailForProductOrder(updateOrder, status);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        if (!ObjectUtils.isEmpty(updateOrder)) {
            session.setAttribute("succMsg", "Status Updated");
        } else {
            session.setAttribute("errorMsg", "status not updated");
        }
        return "redirect:/admin/orders";
    }

    @GetMapping("/search-order")
    public String searchProduct(@RequestParam String orderId,
                                Model model, HttpSession session,
                                @RequestParam(name = "pageNo", defaultValue = "0")
                                Integer pageNo, @RequestParam(name = "pageSize", defaultValue = "5")
                                Integer pageSize) {

        if (orderId != null && orderId.length() > 0) {
            ProductOrder order = orderService.getOrdersByOrderId(orderId.trim());
            if (ObjectUtils.isEmpty(order)) {
                session.setAttribute("errorMsg", "Incorrect Order Id");
                model.addAttribute("orderDtls", null);
            } else {
                model.addAttribute("orderDtls", order);
            }
            model.addAttribute("srch", true);
        } else {
//            List<ProductOrder> allOrders = orderService.getAllOrders();
//            model.addAttribute("orders", allOrders);
//            model.addAttribute("srch", false);

            Page<ProductOrder> page = orderService.getAllOrdersPagination(pageNo, pageSize);
            model.addAttribute("orders", page);
            model.addAttribute("srch", false);

            model.addAttribute("pageNo", page.getNumber());
            model.addAttribute("pageSize", pageSize);
            model.addAttribute("totalElement", page.getTotalElements());
            model.addAttribute("totalPages", page.getTotalPages());
            model.addAttribute("isFirst", page.isFirst());
            model.addAttribute("isLast", page.isLast());
        }
        return "admin/orders";
    }

    @GetMapping("/add-admin")
    public String loadAdminAdd() {
        return "admin/add_admin";
    }

    @PostMapping("/save-admin")
    public String saveAdmin(@ModelAttribute UserDtls user,
                            @RequestParam("img") MultipartFile file,
                            HttpSession session) throws IOException {


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
                return "redirect:/admin/add-admin";
            }
        } else {
            imageName = "default.jpg"; // Use a default image for users who don't upload a profile picture
        }

        // Set the profile image and save the user
        user.setProfileImage(imageName);
        UserDtls saveAdmin = userService.saveAdmin(user);

        if (!ObjectUtils.isEmpty(saveAdmin)) {
            session.setAttribute("succMsg", "Registered successfully");
        } else {
            session.setAttribute("errorMsg", "Something went wrong on the server");
        }

        return "redirect:/admin/add-admin";
    }


//    @PostMapping("/save-admin")
//    public String saveAdmin(@ModelAttribute UserDtls user,
//                            @RequestParam("img") MultipartFile file,
//                            HttpSession session) throws IOException {
//        String imageName = file.isEmpty() ? "Default.jpg" : file.getOriginalFilename();
//        user.setProfileImage(imageName);
////        String imageUrl = commonUtil.getImageUrl(file, BucketType.CATEGORY.getId());
////        user.setProfileImage(imageUrl);
//        UserDtls saveUser = userService.saveAdmin(user);
//
//        if (!ObjectUtils.isEmpty(saveUser)) {
//            if (!file.isEmpty()) {
//                File saveFile = new ClassPathResource("static/img").getFile();
//                Path path = Paths.get(saveFile.getAbsolutePath() + File.
//                        separator + "profile_img"
//                        + File.separator + file.getOriginalFilename());
//                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
////                System.out.println(path);
//
////                fileService.uploadFileS3(file, BucketType.PROFILE.getId());
//
//            }
//            session.setAttribute("succMsg", "Registered Success");
//
//        } else {
//            session.setAttribute("errorMsg", "Can't Update Internal error");
//        }
//        return "redirect:/admin/add-admin";
//    }

    @GetMapping("/profile")
    public String profile() {
        return "admin/profile";
    }

    @PostMapping("/update-profile")
    public String updateProfile(@ModelAttribute UserDtls user,
                                @RequestParam MultipartFile img,
                                HttpSession httpSession) {

        try {
            // Call the service to update the profile
            UserDtls updateUserDetails = userService.updateUserProfile(user, img);
            if (ObjectUtils.isEmpty(updateUserDetails)) {
                httpSession.setAttribute("errorMsg", "Update failed");
            } else {
                httpSession.setAttribute("succMsg", "Profile Updated Successfully");
            }
        } catch (RuntimeException e) {
            // Handle the exception and set the error message in the session
            httpSession.setAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/profile";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam String newPassword,
                                 @RequestParam String currentPassword,
                                 Principal p, HttpSession httpSession) {
        UserDtls loggedInUserDetails = commonUtil.getLoggedInUserDetails(p);
        boolean matches = passwordEncoder.matches(currentPassword, loggedInUserDetails.getPassword());
        if (matches) {
            String encodePassword = passwordEncoder.encode(newPassword);
            loggedInUserDetails.setPassword(encodePassword);
            UserDtls updateUser = userService.updateUser(loggedInUserDetails);
            if (ObjectUtils.isEmpty(updateUser)) {
                httpSession.setAttribute("errorMsg", "Password not updated | Error in server");
            } else {
                httpSession.setAttribute("succMsg", "Password Updated Successfully");
            }
        } else {
            httpSession.setAttribute("errorMsg", "Current Password  is in correct try again");

        }
        return "redirect:/admin/profile";

    }
}
