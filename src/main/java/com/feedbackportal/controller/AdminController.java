package com.feedbackportal.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import com.feedbackportal.dto.FeedbackDto;
import com.feedbackportal.entity.Feedback;
import com.feedbackportal.service.FeedbackService;
import com.feedbackportal.service.UserService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final FeedbackService feedbackService;
    private final UserService userService;

    public AdminController(FeedbackService feedbackService, UserService userService) {
        this.feedbackService = feedbackService;
        this.userService = userService;
    }

   
    @GetMapping("/users/ids")
    @ResponseBody
    public List<String> getUserIds(@RequestParam(required = false) String q) {
        return userService.getUserIds(q);
    }

    @GetMapping("/feedbacks")
    public String listFeedbacks(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            Model model) {

        List<Feedback> feedbacks = feedbackService.getFilteredFeedbacks(userId, dateFrom, dateTo);
        model.addAttribute("feedbacks", feedbacks);

        model.addAttribute("filterUserId", userId);
        model.addAttribute("filterDateFrom", dateFrom);
        model.addAttribute("filterDateTo", dateTo);

        return "admin/feedbacks";
    }


    @GetMapping("/feedbacks/edit/{id}")
    public String editForm(@PathVariable Long id,
                           @RequestParam(required = false) String userId,
                           @RequestParam(required = false) String dateFrom,
                           @RequestParam(required = false) String dateTo,
                           Model model) {
        Feedback fb = feedbackService.getById(id);
        FeedbackDto dto = new FeedbackDto();
        dto.setFeedback(fb.getFeedback());
        model.addAttribute("feedbackDto", dto);
        model.addAttribute("feedback", fb);

        model.addAttribute("filterUserId", userId);
        model.addAttribute("filterDateFrom", dateFrom);
        model.addAttribute("filterDateTo", dateTo);

        return "admin/edit";
    }

    @PostMapping("/feedbacks/edit/{id}")
    public String updateFeedback(@PathVariable Long id,
                                 @Valid @ModelAttribute("feedbackDto") FeedbackDto dto,
                                 BindingResult result,
                                 @RequestParam(required = false) String filterUserId,
                                 @RequestParam(required = false) String filterDateFrom,
                                 @RequestParam(required = false) String filterDateTo,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            Feedback fb = feedbackService.getById(id);
            model.addAttribute("feedback", fb);
            model.addAttribute("filterUserId", filterUserId);
            model.addAttribute("filterDateFrom", filterDateFrom);
            model.addAttribute("filterDateTo", filterDateTo);
            return "admin/edit";
        }

        feedbackService.updateFeedback(id, dto.getFeedback());
        redirectAttributes.addFlashAttribute("success", "Feedback updated successfully!");
        return "redirect:" + buildFilteredRedirectUrl(filterUserId, filterDateFrom, filterDateTo);
    }

    

    @PostMapping("/feedbacks/delete/{id}")
    public String deleteFeedback(@PathVariable Long id,
                                 @RequestParam(required = false) String filterUserId,
                                 @RequestParam(required = false) String filterDateFrom,
                                 @RequestParam(required = false) String filterDateTo,
                                 RedirectAttributes redirectAttributes) {
        feedbackService.deleteFeedback(id);
        redirectAttributes.addFlashAttribute("success", "Feedback deleted successfully!");
        return "redirect:" + buildFilteredRedirectUrl(filterUserId, filterDateFrom, filterDateTo);
    }


    private String buildFilteredRedirectUrl(String userId, String dateFrom, String dateTo) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/admin/feedbacks");
        if (userId != null && !userId.isBlank()) {
            builder.queryParam("userId", userId);
        }
        if (dateFrom != null && !dateFrom.isBlank()) {
            builder.queryParam("dateFrom", dateFrom);
        }
        if (dateTo != null && !dateTo.isBlank()) {
            builder.queryParam("dateTo", dateTo);
        }
        return builder.toUriString();
    }
}
