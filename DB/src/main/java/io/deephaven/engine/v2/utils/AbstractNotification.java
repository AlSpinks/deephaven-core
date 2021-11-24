/*
 * Copyright (c) 2016-2021 Deephaven Data Labs and Patent Pending
 */

package io.deephaven.engine.v2.utils;

import io.deephaven.base.log.LogOutput;
import io.deephaven.io.log.impl.LogOutputStringImpl;
import io.deephaven.engine.updategraph.NotificationQueue;
import org.jetbrains.annotations.NotNull;

/**
 * Common base class for notifications.
 */
public abstract class AbstractNotification implements NotificationQueue.Notification {

    private final boolean isTerminal;

    private NotificationQueue.Notification next;
    private NotificationQueue.Notification prev;

    protected AbstractNotification(final boolean isTerminal) {
        this.isTerminal = isTerminal;
        next = prev = this;
    }

    @Override
    public boolean mustExecuteWithLtmLock() {
        return false;
    }

    @Override
    public String toString() {
        return new LogOutputStringImpl().append(this).toString();
    }

    @Override
    public LogOutput append(LogOutput output) {
        return output.append("Notification{").append(System.identityHashCode(this)).append("}");
    }

    @Override
    public boolean isTerminal() {
        return isTerminal;
    }

    @NotNull
    @Override
    public NotificationQueue.Notification getNext() {
        return next;
    }

    @Override
    public void setNext(@NotNull final NotificationQueue.Notification other) {
        next = other;
    }

    @NotNull
    @Override
    public NotificationQueue.Notification getPrev() {
        return prev;
    }

    @Override
    public void setPrev(@NotNull final NotificationQueue.Notification other) {
        prev = other;
    }
}
