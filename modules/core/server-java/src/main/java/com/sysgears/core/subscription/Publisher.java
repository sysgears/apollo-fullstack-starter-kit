package com.sysgears.core.subscription;

public interface Publisher<T> {
    void publish(T event);
}
