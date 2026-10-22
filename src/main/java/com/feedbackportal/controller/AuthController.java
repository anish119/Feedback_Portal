package com.feedbackportal.controller;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.feedbackportal.dto.RegistrationDto;
import com.feedbackportal.service.CustomUserDetailsService;
import com.feedbackportal.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Controller
public class AuthController {

    private final UserService userService;
    private final CustomUserDetailsService userDetailsService;
    private final SecurityContextRepository securityContextRepository;

    public AuthController(UserService userService,
                          CustomUserDetailsService userDetailsService,
                          SecurityContextRepository securityContextRepository) {
        this.userService = userService;
        this.userDetailsService = userDetailsService;
        this.securityContextRepository = securityContextRepository;
    }


    @GetMapping("/login")
    public String loginPage(HttpServletRequest request) {
        request.getSession(true);
        return "login";
    }


    @GetMapping("/register")
    public String registerPage(HttpServletRequest request, Model model) {
        request.getSession(true);
        model.addAttribute("registrationDto", new RegistrationDto());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registrationDto") RegistrationDto dto,
                           BindingResult result,
                           HttpServletRequest request,
                           HttpServletResponse response,
                           Model model) {

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.registrationDto", "Passwords do not match");
        }

        if (result.hasErrors()) {
            return "register";
        }

        try {
            userService.register(dto);

            request.getSession(true);
            request.changeSessionId();

            UserDetails userDetails = userDetailsService.loadUserByUsername(dto.getUserId().trim());
            UsernamePasswordAuthenticationToken authentication =
                    UsernamePasswordAuthenticationToken.authenticated(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            securityContextRepository.saveContext(context, request, response);

            return "redirect:/feedback/new";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "register";
        }
    }

}
