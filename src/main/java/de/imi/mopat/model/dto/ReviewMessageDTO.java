package de.imi.mopat.model.dto;

import java.sql.Timestamp;

public class ReviewMessageDTO {
    private Long id;
    private Long senderId;
    private Long receiverId;
    private String message;
    private Timestamp sentAt;

    private String senderName;
    private String senderInitials;
    private String receiverName;
    private String receiverInitials;

    public String getSenderName() {
        return senderName;
    }


    public String getSenderInitials() {
        return senderInitials;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public String getReceiverInitials() {
        return receiverInitials;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Timestamp getSentAt() {
        return sentAt;
    }

    public void setSentAt(Timestamp sentAt) {
        this.sentAt = sentAt;
    }

    public void setSenderDetails(UserDTO sender) {
        if (sender != null) {
            this.senderName = sender.getFirstname() + " " + sender.getLastname();
            this.senderInitials = getInitials(sender.getFirstname(), sender.getLastname());
        }
    }

    public void setReceiverDetails(UserDTO receiver) {
        if (receiver != null) {
            this.senderName = receiver.getFirstname() + " " + receiver.getLastname();
            this.senderInitials = getInitials(receiver.getFirstname(), receiver.getLastname());
        }
    }

    private String getInitials(String firstname, String lastname) {
        StringBuilder initials = new StringBuilder();

        if (firstname != null && !firstname.isBlank()) {
            initials.append(firstname.charAt(0));
        }
        if (lastname != null && !lastname.isBlank()) {
            initials.append(lastname.charAt(0));
        }

        return initials.toString().toUpperCase();
    }

}