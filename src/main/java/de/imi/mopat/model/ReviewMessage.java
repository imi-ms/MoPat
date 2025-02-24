package de.imi.mopat.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.sql.Timestamp;

@Entity
@Table(name = "review_message")
public class ReviewMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "sent_at", nullable = false)
    private Timestamp sentAt = new Timestamp(System.currentTimeMillis());

    public ReviewMessage() {
    }

    public ReviewMessage(Review review, Long senderId, Long receiverId, String message) {
        this.review = review;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public Review getReview() {
        return review;
    }

    public Long getSenderId() {
        return senderId;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public String getMessage() {
        return message;
    }

    public Timestamp getSentAt() {
        return sentAt;
    }
}