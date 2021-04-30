package com.sysgears.core.subscription;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.functions.Predicate;
import io.reactivex.observables.ConnectableObservable;

public abstract class AbstractPubSubService<T> implements Publisher<T>, Subscriber<T> {
    private final Flowable<T> publisher;
    private ObservableEmitter<T> emitter;

    public AbstractPubSubService() {
        Observable<T> observable = Observable.create(emitter -> this.emitter = emitter);

        ConnectableObservable<T> connectableObservable = observable.share().publish();
        connectableObservable.connect();

        publisher = connectableObservable.toFlowable(BackpressureStrategy.BUFFER);
    }

    @Override
    public Flowable<T> subscribe(Predicate<T> predicate) {
        return this.publisher.filter(predicate);
    }

    @Override
    public void publish(T event) {
        this.emitter.onNext(event);
    }
}
