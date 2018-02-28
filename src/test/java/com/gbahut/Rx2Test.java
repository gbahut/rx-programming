package com.gbahut;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observables.ConnectableObservable;
import org.junit.Test;
import org.slf4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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

    /**
     * How to wrap futures with Observables and handle timeouts.
     */
    @Test
    public void futureTest()
    {
        logger.info("Starting test");

        ExecutorService executor = Executors.newFixedThreadPool(1);
        Future<Integer> future = executor.submit(this::getFuture);

        logger.info("Checking result");
        Observable.fromFuture(future, 2, TimeUnit.SECONDS)
                  .subscribe(event -> logger.info("GOT {}", event),
                             (e) -> logger.error("Task timed out! ", e));

    }

    private Integer getFuture()
    {
        logger.info("this task run in the executor thread");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return 1;
    }

    /**
     * Stateful source that needs to create a separate state for each Observer
     * with defer(). When your Observable source is not capturing changes to the
     * things driving it, try putting it in Observable.defer()
     */

    private static int count = 5;

    @Test
    public void deferTest()
    {
        Observable<Integer> source = Observable.defer(() ->
                                                          Observable
                                                              .range(1, count));
        source.subscribe(i -> logger.info("Observer 1: " + i));
        //modify count
        count = 10;
        source.subscribe(i -> logger.info("Observer 2: " + i));
    }

    /**
     * If initializing your emission has a likelihood of throwing an error, you
     * should use Observable.fromCallable() instead of Observable.just(). Using
     * the former will throw an exception instead of propagating it downstream
     * to the Observer.
     */
    @Test
    public void fromCallableTest()
    {
        Observable.fromCallable(() -> 1 / 0)
                  .subscribe(event -> logger.info("This won't be logged"),
                             e -> logger.error(
                                 "The error is correctly propagated downstream to the Observer",
                                 e));
    }

    /**
     * This won't be used often... There is not even a new thread spun off.
     */
    @Test
    public void completableTest()
    {
        Completable.fromRunnable(() -> runProcess())
                   .subscribe(() -> logger.info("Done!"));
    }

    private void runProcess()
    {
        logger.info("This runs on main thread!");
    }

    /**
     * When you subscribe() to an Observable to receive emissions, a stream is
     * created to process these emissions through the Observable chain. Of
     * course, this uses resources. When we are done, we want to dispose of
     * these resources so that they can be garbage-collected.
     *
     * @throws InterruptedException
     */
    @Test
    public void disposingTest()
        throws InterruptedException
    {

        Observable<Long> observable = Observable.interval(1, TimeUnit.SECONDS);

        Disposable disposable =
            observable.subscribe(event -> logger.info("GOT {}", event));

        Thread.sleep(3000);
        disposable.dispose();

        logger.info("NO MORE EVENTS ARE LOGGED!");
        Thread.sleep(3000);

    }
}
