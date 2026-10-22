package com.feedbackportal.repository;

import com.feedbackportal.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long>, JpaSpecificationExecutor<Feedback> {

    /** All feedbacks for a specific user, newest first */
    List<Feedback> findByUserIdOrderByDateDesc(String userId);
}

