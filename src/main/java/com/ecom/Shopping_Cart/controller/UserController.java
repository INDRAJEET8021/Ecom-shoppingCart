package com.ecom.Shopping_Cart.controller;

import com.ecom.Shopping_Cart.model.*;
import com.ecom.Shopping_Cart.service.*;
import com.ecom.Shopping_Cart.util.CommonUtil;
import com.ecom.Shopping_Cart.util.OrderStatus;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    public UserService userService;

    @Autowired
    public CategoryService categoryService;
    @Autowired
    public CartService cartService;
    @Autowired
    OrderService orderService;
    @Autowired
    CommonUtil commonUtil;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private WatchlistService watchlistService;

    @GetMapping("/")
    public String home() {
        return "/user/home";
    }

    @ModelAttribute
    private void getUserDetails(Principal principal, Model model) {
        if (principal != null) {
            String email = principal.getName();
            UserDtls userDtls = userService.getUserByEmail(email);
            model.addAttribute("user", userDtls);
            Integer countCart = cartService.getCountCart(userDtls.getId());
            model.addAttribute("countCart", countCart);
            Integer wishlistCount = watchlistService.getWishlistCount(userDtls.getId());
            model.addAttribute("wishlistCount", wishlistCount);
        }
        List<Category> allActiveCategory = categoryService.getAllActiveCategory();
        model.addAttribute("category", allActiveCategory);
    }

    @GetMapping("/addCart")
    public String addToCart(@RequestParam Integer pid,
                            @RequestParam Integer uid, HttpSession session) {
        Cart saveCart = cartService.saveCart(pid, uid);
        if (ObjectUtils.isEmpty(saveCart)) {
            session.setAttribute("errorMsg", "Product add to cart failed");
        } else {
            session.setAttribute("succMsg", "Product added to cart");
        }
        return "redirect:/product/" + pid;
    }

    @GetMapping("/cartQuantityUpdate")
    public String updateCartQuantity(@RequestParam String sy, @RequestParam Integer cid) {
        cartService.updateQuantity(sy, cid);
        return "redirect:/user/cart";
    }

    @GetMapping("/cart")
    public String loadCartPage(Principal p, Model model) {
        UserDtls user = getLoggedInUserDetails(p);
        List<Cart> carts = cartService.getCartsByUser(user.getId());
        if (carts.size() > 0) {
            Double totalOrderPrice = carts.get(carts.size() - 1).getTotalOrderPrice();
            model.addAttribute("totalOrderPrice", totalOrderPrice);
            model.addAttribute("carts", carts);
        }

        return "user/cart";

    }

    private UserDtls getLoggedInUserDetails(Principal p) {
        String email = p.getName();
        UserDtls userDtls = userService.getUserByEmail(email);
        return userDtls;
    }


    @GetMapping("/orders")
    public String orderPage(Principal p, Model m) {
        UserDtls user = getLoggedInUserDetails(p);
        List<Cart> carts = cartService.getCartsByUser(user.getId());
        m.addAttribute("carts", carts);
        if (carts.size() > 0) {
            Double orderPrice = carts.get(carts.size() - 1).getTotalOrderPrice();
            Double totalOrderPrice = carts.get(carts.size() - 1).getTotalOrderPrice() + 95 + 50;
            m.addAttribute("orderPrice", orderPrice);
            m.addAttribute("totalOrderPrice", totalOrderPrice);
        }
        return "user/order";
    }

    @PostMapping("/save-order")
    public String saveOrder(@ModelAttribute OrderRequest request, Principal p)
            throws MessagingException, UnsupportedEncodingException {

        UserDtls userDtls = getLoggedInUserDetails(p);
        orderService.saveOrder(userDtls.getId(), request);
        return "redirect:/user/success";
    }

    @GetMapping("/success")
    public String loadSuccess() {
        return "user/success";
    }

    @GetMapping("/user-orders")
    public String myOrder(Model m, Principal p) {
        UserDtls loginUser = getLoggedInUserDetails(p);
        List<ProductOrder> orders = orderService.getOrdersByUser(loginUser.getId());
        m.addAttribute("orders", orders);
        return "user/my_orders";
    }

    @GetMapping("/update-status")
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
        return "redirect:/user/user-orders";
    }

    @GetMapping("/profile")
    public String profile() {
        return "user/profile";
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
        return "redirect:/user/profile";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam String newPassword,
                                 @RequestParam String currentPassword,
                                 Principal p, HttpSession httpSession) {
        UserDtls loggedInUserDetails = getLoggedInUserDetails(p);
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
        return "redirect:/user/profile";

    }

    //    Watchlist Logic
    @GetMapping("/wishlist")
    public String viewWatchlist(Model model, Principal principal) {
        String email = principal.getName();

        UserDtls user = userService.getUserByEmail(email);

        List<Product> wishlistProducts = watchlistService.getWishlistProducts(user.getId());

        model.addAttribute("wishlistProducts", wishlistProducts);

        return "user/watchlist";
    }


    @GetMapping("/remove")
    public String removeFromWatchlist(@RequestParam("productId") Integer productId,
                                      Principal principal, HttpSession session) {
        String email = principal.getName();
        boolean removed = watchlistService.removeProductFromWishlist(email, productId);
        if (removed) {
            session.setAttribute("succMsg", "Product removed from watchlist.");
        } else {
            session.setAttribute("errorMsg", "Failed to remove product.");
        }
        return "redirect:/user/wishlist"; // Redirect back to the watchlist page
    }

    @PostMapping("/watchlist/toggle")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleWishlist(@RequestParam("productId") Integer productId,
                                                              Principal principal) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (productId == null) {
                throw new IllegalArgumentException("Product ID cannot be null");
            }
            String email = principal.getName();
            boolean added = watchlistService.toggleProductInWishlist(email, productId);
            response.put("success", true);
            response.put("added", added); // True if added, false if removed
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/check-watchlist-status")
    @ResponseBody
    public ResponseEntity<Boolean> checkWatchlistStatus(@RequestParam("productId") Integer productId, Principal principal) {
        String email = principal.getName();
        UserDtls user = userService.getUserByEmail(email);
        boolean isInWatchlist = watchlistService.isProductInWatchlist(user.getId(), productId);
        return ResponseEntity.ok(isInWatchlist);
    }

    @GetMapping("/get-wishlist-count")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getWishlistCount(Principal principal) {
        Map<String, Object> response = new HashMap<>();
        try {
            UserDtls user = userService.getUserByEmail(principal.getName());
            int wishlistCount = watchlistService.getWishlistCount(user.getId());
            response.put("success", true);
            response.put("wishlistCount", wishlistCount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


}
