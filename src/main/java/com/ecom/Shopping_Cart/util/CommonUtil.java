package com.ecom.Shopping_Cart.util;

import com.ecom.Shopping_Cart.model.ProductOrder;
import com.ecom.Shopping_Cart.model.UserDtls;
import com.ecom.Shopping_Cart.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.UnsupportedEncodingException;
import java.security.Principal;

@Component

public class CommonUtil {
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private UserService userService;
//    @Value("${aws.s3.bucket.category}")
//    private String categoryBucket;
//
//    @Value("${aws.s3.bucket.product}")
//    private String productBucket;
//
//    @Value("${aws.s3.bucket.profile}")
//    private String profileBucket;

    public Boolean sendMail(String url, String reciepentEmail) throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom("indrajeetrai903@gmail.com", "Shopping Cart");
        helper.setTo(reciepentEmail);

        String content = "<p>Hello,</p>" + "<p>You have requested to reset your password.</p>"
                + "<p>Click the link below to change your password:</p>" + "<p><a href=\"" + url
                + "\">Change my password</a></p>";
        helper.setSubject("Password Reset");
        helper.setText(content, true);
        mailSender.send(message);
        return true;
    }

    public static String generateUrl(HttpServletRequest request) {
        String siteUrl = request.getRequestURL().toString();
        System.out.println(siteUrl);
        return siteUrl.replace(request.getServletPath(), "");
    }


    public Boolean sendMailForProductOrder(ProductOrder productOrder, String status)
            throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom("indrajeetrai903@gmail.com", "Shopping Cart");
        helper.setTo(productOrder.getOrderAddress().getEmail());

        String msg;

        // Handle the "Cancelled" status
        if ("Cancelled".equals(status)) {
            msg = "<div style='font-family: Arial, sans-serif; color: #333;'>"
                    + "<p style='font-size: 18px; font-weight: bold;'>Hello [[name]],</p>"
                    + "<p>We regret to inform you that your order has been <b style='color: #dc3545;'>Cancelled</b>.</p>"
                    + "<p><b>Product Details:</b></p>"
                    + "<ul style='list-style-type: none; padding-left: 0;'>"
                    + "<li><b>Product Name:</b> [[productName]]</li>"
                    + "<li><b>Category:</b> [[category]]</li>"
                    + "<li><b>Quantity:</b> [[quantity]]</li>"
                    + "<li><b>Price:</b> ₹[[price]]</li>"
                    + "<li><b>Payment Method:</b> [[paymentType]]</li>"
                    + "</ul>"
                    + "<p>If you have any questions or need further assistance, please <a href='mailto:support@example.com' style='color: #007bff;'>contact us</a>.</p>"
                    + "<p style='font-size: 16px; font-weight: bold;'>We apologize for the inconvenience.</p>"
                    + "<p style='color: #555;'>Best regards,<br/>The Shopping Cart Team</p>"
                    + "</div>";
        } else {
            msg = "<div style='font-family: Arial, sans-serif; color: #333;'>"
                    + "<p style='font-size: 18px; font-weight: bold;'>Hello [[name]],</p>"
                    + "<p>Thank you for your order! We are excited to inform you that your order is now <b style='color: #28a745;'>[[orderStatus]]</b>.</p>"
                    + "<p><b>Product Details:</b></p>"
                    + "<ul style='list-style-type: none; padding-left: 0;'>"
                    + "<li><b>Product Name:</b> [[productName]]</li>"
                    + "<li><b>Category:</b> [[category]]</li>"
                    + "<li><b>Quantity:</b> [[quantity]]</li>"
                    + "<li><b>Price:</b> ₹[[price]]</li>"
                    + "<li><b>Payment Method:</b> [[paymentType]]</li>"
                    + "</ul>"
                    + "<p>If you have any questions or need further assistance, feel free to <a href='mailto:support@example.com' style='color: #007bff;'>contact us</a>.</p>"
                    + "<p style='font-size: 16px; font-weight: bold;'>Thank you for choosing us!</p>"
                    + "<p style='color: #555;'>Best regards,<br/>The Shopping Cart Team</p>"
                    + "</div>";
        }

        // Replace placeholders with actual values
        msg = msg.replace("[[name]]", productOrder.getOrderAddress().getFirstName());
        msg = msg.replace("[[orderStatus]]", status);
        msg = msg.replace("[[productName]]", productOrder.getProduct().getTitle());
        msg = msg.replace("[[category]]", productOrder.getProduct().getCategory());
        msg = msg.replace("[[quantity]]", productOrder.getQuantity().toString());
        msg = msg.replace("[[price]]", productOrder.getPrice().toString());
        msg = msg.replace("[[paymentType]]", productOrder.getPaymentType());

        helper.setSubject("Product Order Status");
        helper.setText(msg, true);
        mailSender.send(message);

        return true;
    }

    public UserDtls getLoggedInUserDetails(Principal p) {
        String email = p.getName();
        UserDtls userDtls = userService.getUserByEmail(email);
        return userDtls;
    }

//    public String getImageUrl(MultipartFile file, Integer bucketType) {
//
//        String bucketName = null;
//
//        if (bucketType == 1) {
//            bucketName = categoryBucket;
//        } else if (bucketType == 2) {
//            bucketName = productBucket;
//        } else {
//            bucketName = profileBucket;
//        }
//        // https://shooping-cart-category.s3.amazonaws.com/mobile.jpg
//
//        String imageName = file != null ? file.getOriginalFilename() : "default.jpg";
//
//        String url = "https://" + bucketName + ".s3.amazonaws.com/" + imageName;
//        return url;
//    }


}
