package com.feedbackportal.controller;

import com.feedbackportal.dto.FeedbackDto;
import com.feedbackportal.entity.Feedback;
import com.feedbackportal.service.FeedbackService;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    /* ── Add Feedback ────────────────────────────────────── */

    @GetMapping("/new")
    public String newFeedbackForm(Model model) {
        model.addAttribute("feedbackDto", new FeedbackDto());
        return "feedback/new";
    }

    @PostMapping("/new")
    public String submitFeedback(@Valid @ModelAttribute("feedbackDto") FeedbackDto dto,
                                 BindingResult result,
                                 @AuthenticationPrincipal UserDetails user,
                                 RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "feedback/new";
        }
        feedbackService.createFeedback(user.getUsername(), dto.getFeedback());
        redirectAttributes.addFlashAttribute("success", "Feedback submitted successfully!");
        return "redirect:/feedback/new";
    }

    /* ── My Feedbacks ────────────────────────────────────── */

    @GetMapping("/my")
    public String myFeedbacks(@AuthenticationPrincipal UserDetails user, Model model) {
        model.addAttribute("feedbacks", feedbackService.getUserFeedbacks(user.getUsername()));
        return "feedback/my";
    }

    /* ── Edit Feedback ───────────────────────────────────── */

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id,
                           @AuthenticationPrincipal UserDetails user,
                           Model model) {
        Feedback fb = feedbackService.getById(id);
        verifyOwnership(fb, user);
        FeedbackDto dto = new FeedbackDto();
        dto.setFeedback(fb.getFeedback());
        model.addAttribute("feedbackDto", dto);
        model.addAttribute("feedbackId", id);
        return "feedback/edit";
    }

    @PostMapping("/edit/{id}")
    public String updateFeedback(@PathVariable Long id,
                                 @Valid @ModelAttribute("feedbackDto") FeedbackDto dto,
                                 BindingResult result,
                                 @AuthenticationPrincipal UserDetails user,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        Feedback fb = feedbackService.getById(id);
        verifyOwnership(fb, user);

        if (result.hasErrors()) {
            model.addAttribute("feedbackId", id);
            return "feedback/edit";
        }

        feedbackService.updateFeedback(id, dto.getFeedback());
        redirectAttributes.addFlashAttribute("success", "Feedback updated successfully!");
        return "redirect:/feedback/my";
    }

    /** Security check: feedback must belong to the logged-in user. */
    private void verifyOwnership(Feedback fb, UserDetails user) {
        if (!fb.getUserId().equals(user.getUsername())) {
            throw new AccessDeniedException("Access denied: feedback does not belong to user");
        }
    }
}
