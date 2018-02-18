package com.gbahut;

import org.junit.Test;
import org.slf4j.Logger;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;

import java.time.DayOfWeek;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.slf4j.LoggerFactory.getLogger;
import static rx.Observable.interval;
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

        Observer o;
        Subscriber<String> s;
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
            return
                interval(15, MILLISECONDS)
                    .take(5)
                    .map(i -> "Sun-" + i);
        case MONDAY:
            return
                interval(1, SECONDS)
                    .take(5)
                    .map(i -> "Mon-" + i);
        default:
            return null;
        }
    }

    @Test
    public void mergeTest()
    {

        Observable<Integer> a = just(1, 2, 3, 4).map(this::applyDelay);

        Observable<Integer> b = just(5, 6, 7, 8).map(this::applyDelay);

        Observable.merge(a, b).subscribe(i -> logger.info("COMPLETED {}", i));

        a.mergeWith(b).subscribe(i -> logger.info("GOT {}", i));
    }

    private Integer applyDelay(Integer integer)
    {
        try {
            int delay = ThreadLocalRandom.current().nextInt(0, 3);
            logger.info("Got {} and using delay of {}s", integer, delay);
            Thread.sleep(delay * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return integer;
    }

    /**
     * The timestamp() operator wraps whatever the event type T was with
     * rx.schedulers.Timestamped<T> class having two attributes: original value
     * of type T and long timestamp when it was created.
     *
     * @throws InterruptedException
     */
    @Test
    public void zipTest()
        throws InterruptedException
    {

        Observable<Long> red =
            interval(10, TimeUnit.MILLISECONDS)
                .doOnNext(x -> logger.info("Got RED {}", x));
        Observable<Long> green =
            interval(11, TimeUnit.MILLISECONDS)
                .doOnNext(x -> logger.info("Got GREEN {}", x));
        Observable.zip(red.timestamp(),
                       green.timestamp(),
                       (r, g) -> r.getTimestampMillis() -
                                 g.getTimestampMillis())
                  .forEach(t -> logger.info("Diff: {}", t));

        SECONDS.sleep(10);
    }

    @Test
    public void combineLatestTest()
        throws InterruptedException
    {
        Observable.combineLatest(
            interval(17, MILLISECONDS).map(x -> "S" + x),
            interval(10, MILLISECONDS).map(x -> "F" + x), (s, f) -> f + ":" + s
        ).forEach(t -> logger.info("GOT: {}", t));
        SECONDS.sleep(1);
    }

    @Test
    public void ambTest()
        throws InterruptedException
    {
        Observable.amb(
            stream(100, 17, "S"),
            stream(200, 10, "F")
        ).subscribe(logger::info);

        SECONDS.sleep(1);
    }

    private Observable<String> stream(int initialDelay, int interval, String name)
    {
        return Observable
            .interval(initialDelay, interval, MILLISECONDS)
            .map(x -> name + x)
            .doOnSubscribe(() ->
                               logger.info("Subscribe to " + name))
            .doOnUnsubscribe(() ->
                                 logger.info("Unsubscribe from " + name));

    }
}
