package com.health.mediconnectx.exception;

/**
 * Thrown when a patient tries to book a slot that is no longer OPEN.
 * The controller advice maps this to HTTP 409 Conflict.
 */
public class SlotAlreadyTakenException extends RuntimeException {

    public SlotAlreadyTakenException(Long slotId) {
        super("Slot " + slotId + " is no longer available. Please choose another time.");
    }
}
