package com.sysgears.core.subscription;

import io.reactivex.Flowable;
import io.reactivex.functions.Predicate;

public interface Subscriber<T> {
    Flowable<T> subscribe(Predicate<T> predicate);
}
