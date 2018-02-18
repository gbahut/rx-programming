package com.gbahut;

import org.junit.Test;
import org.slf4j.Logger;
import rx.Observable;

import java.time.DayOfWeek;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogManager;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.slf4j.LoggerFactory.getLogger;
import static rx.Observable.just;

/**
 * Created by gbahut on 17/02/2018.
 */
public class MiscTest
{
    private static final Logger logger = getLogger(MiscTest.class);

    @Test
    public void delayedObservableTest()
        throws InterruptedException
    {
        logger.info("Starting...");
        just("Hola", "que", "tal", "estas")
            .delay(1, SECONDS)
            .subscribe(logger::info);

        just(10L, 1L)
            .flatMap(x ->
                         just(x).delay(x, TimeUnit.SECONDS))
            .subscribe(System.out::println);

        SECONDS.sleep(10);
    }

    @Test
    public void timerTest()
        throws InterruptedException
    {

        logger.info("starting...");
        Observable.timer(1, SECONDS).subscribe(i -> logger.info("GOT " + i));

        SECONDS.sleep(5);
        logger.info("DONE");
    }

    @Test
    public void flatMapTest()
        throws InterruptedException
    {
        just(DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.FRIDAY)
            .flatMap(this::loadRecordsFor)
            .subscribe(System.out::println,
                       System.out::println);

        Observable.timer(1, SECONDS)
                  .subscribe(i -> System.out.println("GOT " + i));

        SECONDS.sleep(10);

    }

    private Observable<String> loadRecordsFor(DayOfWeek dow)
    {
        switch (dow) {
        case SUNDAY:
            return Observable
                .interval(15, MILLISECONDS)
                .take(5)
                .map(i -> "Sun-" + i);
        case MONDAY:
            return Observable
                .interval(1, SECONDS)
                .take(5)
                .map(i -> "Mon-" + i);
        default:
            return null;
        }
    }

}
