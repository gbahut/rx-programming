package com.gbahut;

import io.reactivex.Observable;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by gbahut on 11/02/2018.
 */
public class RxStaticFunctionsTest
{

    @Test
    public void testJustObservable()
    {

        Observable<Integer> test = RxStaticFunctions.just(4);

        test.subscribe(x -> {
            System.out.println("Got " + x);
            assertThat(x).isEqualTo(4);
        });
    }

    @Test
    public void testNeverObservable()
    {
        Observable<Integer> test = RxStaticFunctions.never();

        System.out.println("Starting subscription...");
        test.subscribe();
        System.out.println("End.");

        Observable<Void> t = Observable.never();
        System.out.println("Starting subscription...");

        t.subscribe();
        System.out.println("End.");
    }

    @Test
    public void testEmptyObservable()
    {
        Observable<Integer> test = RxStaticFunctions.empty(10);

        test.subscribe(x -> System.out.println("Getting something???" + x),
                       e -> System.out.println("ERROR!"),
                       () -> System.out.println("Completed!"));
    }

    @Test
    public void testRangeObservable()
    {
        Observable<Integer> test = RxStaticFunctions.range(10, 2);

        test.subscribe(i -> System.out.println("Got integer " + i),
                       e -> System.out.println("ERROR!"),
                       () -> System.out.println("Completed"));

    }
}
