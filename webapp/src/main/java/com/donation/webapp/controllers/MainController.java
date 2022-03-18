package com.donation.webapp.controllers;

import com.donation.common.core.exception.InvalidMessageException;
import com.donation.data.handler.service.UserAccountDetailsService;
import com.donation.management.dto.UserAccountDetailDto;
import com.donation.management.dto.UserRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import static com.donation.common.core.constant.UrlMapping.ADMIN_USER;

@Controller
public class MainController {

    @Autowired
    private UserAccountDetailsService userAccountDetailsService;

    // Convert trim input strings,
    // remove leading and trailing whitespace
    @InitBinder
    protected void initBinder(WebDataBinder dataBinder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
        dataBinder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }

    @GetMapping("/admin")
    public String showAdmin() {

        return "redirect:" + ADMIN_USER;
    }

    @GetMapping("/home")
    public String showHomePage() {

        return "redirect:/project";
    }

    @GetMapping("/login")
    public String showLogin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return "views/login";
        }
        return "redirect:/";
    }

    @RequestMapping("/access-denied")
    public String showAccessDenied(Model theModel) {
        theModel.addAttribute("error", "authority");
        return "views/error/access_denied";
    }

    @GetMapping("/logout")
    public String processLogout() {
        return "redirect:/login?logout";
    }

    @GetMapping("/signup")
    public String showRegisterForm(Model theModel) {
        theModel.addAttribute("pageTitle", "Registration");
        theModel.addAttribute("userRequestDto", new UserRequestDto());
        return "views/register/register_form";
    }

    @PostMapping("/signup/user")
    public String createUser(UserRequestDto userRequestDto, Model theModel, RedirectAttributes attributes,
                             HttpServletRequest servletRequest) {
        try {
            userAccountDetailsService.createUser(servletRequest, userRequestDto);
            theModel.addAttribute("pageTitle", "Registration Succeeded!");
            return "views/register/register_success";
        } catch (InvalidMessageException e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/signup";
        }
    }

    @GetMapping("/verify")
    public String verifyAccount(@RequestParam String token) {
        boolean verified = userAccountDetailsService.verify(token);
        return "views/register/" + (verified ? "verify_success" : "verify_fail");
    }


    @GetMapping("/user/forgot-password")
    public String showRequestForm() {
        return "views/user/forgot_password_form";
    }

    @PostMapping("/user/forgot-password")
    public String processRequestForm(HttpServletRequest servletRequest, Model theModel) {
        String email = servletRequest.getParameter("email");
        try {
            userAccountDetailsService.updateResetPasswordToken(servletRequest, email);

            theModel.addAttribute("message",
                    "The reset password link was sent to your email. Please check your email.");
        } catch (InvalidMessageException e) {
            theModel.addAttribute("error", e.getMessage());
        }
        return "views/user/forgot_password_form";
    }

    @GetMapping("/user/reset-password")
    public String showResetForm(@RequestParam String token, Model theModel) {
        UserAccountDetailDto userAccountDetailDto =
                userAccountDetailsService.getByResetPasswordToken(token);
        if (Objects.nonNull(userAccountDetailDto)) {
            theModel.addAttribute("token", token);
        } else {
            theModel.addAttribute("pageTitle", "Invalid Token");
            theModel.addAttribute("message", "Invalid Token");
            return "views/message";
        }

        return "views/user/reset_password_form";
    }

    @PostMapping("/user/reset-password")
    public String processResetForm(@RequestParam("token") String token,
                                   @RequestParam("password") String password,
                                   Model theModel) {
        try {
            userAccountDetailsService.updatePassword(token, password);

            theModel.addAttribute("pageTitle", "Reset Your Password");
            theModel.addAttribute("title", "Reset Your Password");
            theModel.addAttribute("message", "You have successfully changed your password.");

        } catch (InvalidMessageException e) {
            theModel.addAttribute("pageTitle", "Invalid Token");
            theModel.addAttribute("message", e.getMessage());
        }
        return "views/message";
    }
}
