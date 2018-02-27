package com.gbahut;


import io.reactivex.Observable;

import java.util.stream.IntStream;

/**
 * Created by gbahut on 11/02/2018.
 */
public class RxStaticFunctions
{
    static <T> Observable<T> just(T x)
    {
        return Observable.unsafeCreate(subscriber -> {
                                           subscriber.onNext(x);
                                           subscriber.onComplete();
                                       }
        );
    }

    static <T> Observable<T> never()
    {
        return Observable.unsafeCreate(s -> {});
    }

    static <T> Observable<T> empty(T x)
    {
        return Observable.unsafeCreate(subscriber -> subscriber.onComplete());
    }

    static Observable<Integer> range(Integer from, int number)
    {

        return Observable.unsafeCreate(subscriber -> {
            IntStream.rangeClosed(from, from + number)
                     .forEach(subscriber::onNext);
            subscriber.onComplete();
        });
    }


}
