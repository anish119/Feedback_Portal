package com.feedbackportal.service;

import com.feedbackportal.entity.Feedback;
import com.feedbackportal.repository.FeedbackRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;

    public FeedbackService(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    /** Create a new feedback for the given user. */
    public void createFeedback(String userId, String feedbackText) {
        Feedback fb = new Feedback();
        fb.setUserId(userId);
        fb.setFeedback(feedbackText.trim());
        fb.setDate(LocalDateTime.now());
        feedbackRepository.save(fb);
    }

    /** Get all feedbacks for a specific user, newest first. */
    public List<Feedback> getUserFeedbacks(String userId) {
        return feedbackRepository.findByUserIdOrderByDateDesc(userId);
    }

    /** Find a single feedback by id. */
    public Feedback getById(Long id) {
        return feedbackRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Feedback not found"));
    }

    /** Update the feedback text (keeps original date). */
    public void updateFeedback(Long id, String newText) {
        Feedback fb = getById(id);
        fb.setFeedback(newText.trim());
        feedbackRepository.save(fb);
    }

    /** Delete a feedback by id. */
    public void deleteFeedback(Long id) {
        feedbackRepository.deleteById(id);
    }

    /**
     * Admin: get feedbacks with optional filters.
     * All parameters may be null (= no filter).
     */
    public List<Feedback> getFilteredFeedbacks(String userId, LocalDate dateFrom, LocalDate dateTo) {
        LocalDateTime from = (dateFrom != null) ? dateFrom.atStartOfDay() : null;
        LocalDateTime to = (dateTo != null) ? dateTo.atTime(LocalTime.MAX) : null;
        String userIdParam = (userId != null && !userId.isBlank()) ? userId.trim() : null;

        Specification<Feedback> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (userIdParam != null) {
                String escaped = userIdParam.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
                String pattern = "%" + escaped.toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("userId")), pattern, '\\'));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("date"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("date"), to));
            }
            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };

        return feedbackRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "date"));
    }
}
