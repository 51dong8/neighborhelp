package com.neighborhelp;

import com.neighborhelp.common.exception.BusinessException;
import com.neighborhelp.entity.Request;
import com.neighborhelp.service.support.RequestStatusRules;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RequestStatusRulesTest {

    @Test
    void rejectsManualActive() {
        Request r = new Request();
        r.setStatus("active");
        assertThrows(BusinessException.class, () -> RequestStatusRules.validateManualStatusTransition(r, "active"));
    }

    @Test
    void allowsCancelWhenUnclaimed() {
        Request r = new Request();
        r.setStatus("active");
        assertDoesNotThrow(() -> RequestStatusRules.validateManualStatusTransition(r, "cancelled"));
    }

    @Test
    void rejectsWhenAlreadyAccepted() {
        Request r = new Request();
        r.setStatus("accepted");
        r.setAcceptedBy(99L);
        assertThrows(BusinessException.class, () -> RequestStatusRules.validateManualStatusTransition(r, "cancelled"));
    }

    @Test
    void rejectsLegacyActiveWithAcceptor() {
        Request r = new Request();
        r.setStatus("active");
        r.setAcceptedBy(1L);
        assertThrows(BusinessException.class, () -> RequestStatusRules.validateManualStatusTransition(r, "cancelled"));
    }

    @Test
    void assertDeletableRequiresOpenAndUnclaimed() {
        Request r = new Request();
        r.setStatus("active");
        r.setAcceptedBy(null);
        assertDoesNotThrow(() -> RequestStatusRules.assertDeletable(r));

        r.setAcceptedBy(2L);
        assertThrows(BusinessException.class, () -> RequestStatusRules.assertDeletable(r));
    }
}
