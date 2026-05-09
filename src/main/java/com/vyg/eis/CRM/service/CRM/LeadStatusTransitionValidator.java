package com.vyg.eis.CRM.service.CRM;

import com.vyg.eis.CRM.common.exception.InvalidStatusTransitionException;
import com.vyg.eis.CRM.domain.CRM.enums.LeadStatus;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Component
public class LeadStatusTransitionValidator {

    private static final Map<LeadStatus, Set<LeadStatus>> ALLOWED = new EnumMap<>(LeadStatus.class);

    static {
        ALLOWED.put(LeadStatus.NEW, EnumSet.of(
                LeadStatus.AI_CALL_SCHEDULED, LeadStatus.IN_FOLLOW_UP,
                LeadStatus.ON_HOLD, LeadStatus.CLOSED_LOST));

        ALLOWED.put(LeadStatus.AI_CALL_SCHEDULED, EnumSet.of(
                LeadStatus.AI_CALL_DONE_INTERESTED, LeadStatus.AI_CALL_DONE_NOT_INTERESTED));

        ALLOWED.put(LeadStatus.AI_CALL_DONE_INTERESTED, EnumSet.of(
                LeadStatus.IN_FOLLOW_UP, LeadStatus.DEMO_SCHEDULED));

        ALLOWED.put(LeadStatus.AI_CALL_DONE_NOT_INTERESTED, EnumSet.of(
                LeadStatus.CLOSED_LOST, LeadStatus.ON_HOLD, LeadStatus.IN_FOLLOW_UP));

        ALLOWED.put(LeadStatus.IN_FOLLOW_UP, EnumSet.of(
                LeadStatus.DEMO_SCHEDULED, LeadStatus.QUOTATION_SENT, LeadStatus.NEGOTIATION,
                LeadStatus.CLOSED_LOST, LeadStatus.ON_HOLD));

        ALLOWED.put(LeadStatus.DEMO_SCHEDULED, EnumSet.of(
                LeadStatus.DEMO_DONE, LeadStatus.IN_FOLLOW_UP, LeadStatus.CLOSED_LOST));

        ALLOWED.put(LeadStatus.DEMO_DONE, EnumSet.of(
                LeadStatus.QUOTATION_SENT, LeadStatus.CLOSED_LOST, LeadStatus.IN_FOLLOW_UP));

        ALLOWED.put(LeadStatus.QUOTATION_SENT, EnumSet.of(
                LeadStatus.NEGOTIATION, LeadStatus.CONVERTED_WON, LeadStatus.CLOSED_LOST));

        ALLOWED.put(LeadStatus.NEGOTIATION, EnumSet.of(
                LeadStatus.CONVERTED_WON, LeadStatus.CLOSED_LOST, LeadStatus.QUOTATION_SENT));

        ALLOWED.put(LeadStatus.CONVERTED_WON, EnumSet.noneOf(LeadStatus.class));

        ALLOWED.put(LeadStatus.CLOSED_LOST, EnumSet.of(
                LeadStatus.IN_FOLLOW_UP, LeadStatus.ON_HOLD));

        ALLOWED.put(LeadStatus.ON_HOLD, EnumSet.of(
                LeadStatus.IN_FOLLOW_UP, LeadStatus.NEW));
    }

    public void validate(LeadStatus from, LeadStatus to) {
        Set<LeadStatus> allowed = ALLOWED.getOrDefault(from, EnumSet.noneOf(LeadStatus.class));
        if (!allowed.contains(to)) {
            throw new InvalidStatusTransitionException(
                    String.format("Transition from %s to %s is not allowed. Allowed: %s", from, to, allowed));
        }
    }

    public Set<LeadStatus> getAllowedTransitions(LeadStatus from) {
        return ALLOWED.getOrDefault(from, EnumSet.noneOf(LeadStatus.class));
    }
}
