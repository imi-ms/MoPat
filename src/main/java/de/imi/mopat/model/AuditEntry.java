package de.imi.mopat.model;

import de.imi.mopat.model.enumeration.AuditPatientAttribute;
import de.imi.mopat.model.enumeration.AuditEntryActionType;
import de.imi.mopat.model.user.User;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Set;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Entity
@Table(name = "audit_entry")
public class AuditEntry implements Serializable {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(AuditEntry.class);
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "user_id")
    @NotNull
    private Long userId;
    @Column(name = "module")
    private String module;
    @Column(name = "action")
    private String method;
    @NotNull
    @NotEmpty
    @Column(name = "content", columnDefinition = "TEXT NOT NULL")
    private String content;
    @Column(name = "sender_receiver", columnDefinition = "TEXT", nullable = true)
    private String senderReceiver;
    @NotNull
    @Column(name = "log_time", nullable = false)
    private Timestamp logTime = new Timestamp(System.currentTimeMillis());
    @Transient
    private AuditEntryActionType action;

    public AuditEntry(final String module, final String method, final String caseNumber,
        final Set<AuditPatientAttribute> patientAttributes, final AuditEntryActionType action) {
        assert caseNumber != null : "The given case number was null";
        assert !caseNumber.trim().isEmpty() : "The given case number was empty";
        assert patientAttributes != null : "The set of given patient attributes to log was null";
        assert !patientAttributes.isEmpty() : "The given set of patient " + "attributes was empty";
        assert action != null : "The given action to audit log was null";

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            userId = -1L;
        } else {
            Object principal = authentication.getPrincipal();
            if (principal != null && principal instanceof User) {
                userId = ((User) principal).getId();
            } else {
                userId = -1L;
            }
        }
        this.module = module;
        this.method = method;
        this.action = action;

        JSONObject jsonContent = new JSONObject();
        JSONArray jsonPatientAttributes = new JSONArray(patientAttributes);
        try {
            jsonContent.put("caseNumber", caseNumber);
            jsonContent.put("patientAttributes", jsonPatientAttributes);
            jsonContent.put("action", action.toString());
        } catch (JSONException jsone) {
            Marker fatal = MarkerFactory.getMarker("FATAL");
            LOGGER.error(fatal, "Could not add affected patient data items to the audit "
                    + "log content. Audit log entry was supposed to "
                    + "contain: user ID: {}; module: {}; method: {}; "
                    + "case number: {}; patient attributes: {}; "
                    + "action: {}; timestamp: {}; reason: {}",
                new Object[]{userId, module, method, caseNumber, patientAttributes, action, logTime,
                    new JSONException("blabla")});
        }

        this.content = jsonContent.toString();
    }

    public AuditEntry(final String module, final String method, final String caseNumber,
        final Set<AuditPatientAttribute> patientAttributes, final AuditEntryActionType action,
        final String senderReceiver) {
        this(module, method, caseNumber, patientAttributes, action);
        if (action == AuditEntryActionType.RECEIVED || action == AuditEntryActionType.SENT) {
            assert
                senderReceiver != null :
                "The sender/receiver given was null although " + "the action to log was " + action;
            assert !senderReceiver.trim().isEmpty() :
                "The sender/receiver given was" + " empty although the action to log was " + action;
        }
        this.senderReceiver = senderReceiver;
    }

    protected AuditEntry() {
    }

    @Override
    public String toString() {
        String result = "";

        result = result.concat("id: " + this.getId());
        result = result.concat(", userId: " + this.getUserId());
        result = result.concat(", module: " + this.getModule());
        result = result.concat(", method: " + this.getMethod());
        result = result.concat(", content: " + this.getContent());
        result = result.concat(", sender/receiver: " + this.getSenderReceiver());
        result = result.concat(", logTime: " + this.getLogTime());

        return result;
    }

    public AuditEntryActionType getAction() {
        return action;
    }

    public String getSenderReceiver() {
        return senderReceiver;
    }

    public Long getId() {
        return this.id;
    }

    public Long getUserId() {
        return this.userId;
    }

    public String getContent() {
        return this.content;
    }

    public Timestamp getLogTime() {
        return this.logTime;
    }

    public String getMethod() {
        return this.method;
    }

    public String getModule() {
        return this.module;
    }
}