package com.stepaniuk.testhorizon.kafka;

import com.stepaniuk.testhorizon.event.auth.AuthEvent;
import com.stepaniuk.testhorizon.event.bugreport.BugReportEvent;
import com.stepaniuk.testhorizon.event.comment.CommentEvent;
import com.stepaniuk.testhorizon.event.feedback.FeedbackEvent;
import com.stepaniuk.testhorizon.event.file.FileEvent;
import com.stepaniuk.testhorizon.event.project.ProjectEvent;
import com.stepaniuk.testhorizon.event.rating.RatingEvent;
import com.stepaniuk.testhorizon.event.test.TestEvent;
import com.stepaniuk.testhorizon.event.testcase.TestCaseEvent;
import com.stepaniuk.testhorizon.event.user.UserEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaWebSocketNotifier {
    private final SimpMessagingTemplate simpMessagingTemplate;

    @KafkaListener(topics = "users", groupId = "user_group")
    public void handleUserEvents(UserEvent userEvent) {
        simpMessagingTemplate.convertAndSend("/topic/users", userEvent);
    }

    @KafkaListener(topics = "auth", groupId = "auth_group")
    public void handleAuthEvents(AuthEvent authEvent) {
        simpMessagingTemplate.convertAndSend("/topic/auth", authEvent);
    }

    @KafkaListener(topics = "test-cases", groupId = "test_case_group")
    public void handleTestCaseEvents(TestCaseEvent testCaseEvent) {
        simpMessagingTemplate.convertAndSend("/topic/test-cases", testCaseEvent);
    }

    @KafkaListener(topics = "tests", groupId = "test_group")
    public void handleTestEvents(TestEvent testEvent) {
        simpMessagingTemplate.convertAndSend("/topic/tests", testEvent);
    }

    @KafkaListener(topics = "ratings", groupId = "rating_group")
    public void handleRatingEvents(RatingEvent ratingEvent) {
        simpMessagingTemplate.convertAndSend("/topic/ratings", ratingEvent);
    }

    @KafkaListener(topics = "projects", groupId = "project_group")
    public void handleProjectEvents(ProjectEvent projectEvent) {
        simpMessagingTemplate.convertAndSend("/topic/projects", projectEvent);
    }

    @KafkaListener(topics = "comments", groupId = "comment_group")
    public void handleCommentEvents(CommentEvent commentEvent) {
        simpMessagingTemplate.convertAndSend("/topic/comments", commentEvent);
    }

    @KafkaListener(topics = "bug-reports", groupId = "bug_report_group")
    public void handleBugReportEvents(BugReportEvent bugReportEvent) {
        simpMessagingTemplate.convertAndSend("/topic/bug-reports", bugReportEvent);
    }

    @KafkaListener(topics = "files", groupId = "file_group")
    public void handleFileEvents(FileEvent fileEvent) {
        simpMessagingTemplate.convertAndSend("/topic/files", fileEvent);
    }

    @KafkaListener(topics = "feedbacks", groupId = "feedback_group")
    public void handleFeedbackEvents(FeedbackEvent feedbackEvent) {
        simpMessagingTemplate.convertAndSend("/topic/feedbacks", feedbackEvent);
    }
}
