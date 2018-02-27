package com.gbahut;

import io.reactivex.Observable;
import io.reactivex.observables.ConnectableObservable;
import org.junit.Test;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by gbahut on 27/02/2018.
 */
public class Rx2Test
{

    private static final Logger logger = getLogger(Rx2Test.class);

    /**
     * Using ConnectableObservable to force each emission to go to all Observers
     * simultaneously is known as multicasting,
     */
    @Test
    public void connectableObservableTest()
    {

        ConnectableObservable<String> source =
            Observable.just("Hola", "que", "tal", "estas", "?").publish();

        source.subscribe(event -> logger.info("GOT {}", event));

        source.map(String::length)
              .subscribe(i -> logger.info("Length {}", i));

        source.connect();
    }
}
