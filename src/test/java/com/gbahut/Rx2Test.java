package com.gbahut;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.observables.GroupedObservable;
import org.junit.Test;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Random;
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

    @Test
    public void skipFilterTakeTest()
    {

        Observable.range(1, 100).filter(i -> i >= 10 && i <= 20)
                  .skip(1)
                  .take(3)
                  .subscribe(event -> logger.info("GOT {}", event));

    }

    @Test
    public void createObservableTest()
    {

        Observable<Integer> onTest = Observable.create(observableEmitter -> {
            try {
                for (int i = 0; i < 1000; i++) {
                    while (!observableEmitter.isDisposed()) {
                        observableEmitter.onNext(i);
                    }
                    if (observableEmitter.isDisposed())
                        return;

                }
                observableEmitter.onComplete();
            } catch (Throwable e) {
                observableEmitter.onError(e);
            }
        });
        logger.info("Starting subscription...");

        Disposable disposable = runDisposable(onTest);

        logger.info("Cancelling subscription...");

        disposable.dispose();
    }

    /**
     * This call is blocking. We must change it to run async.
     *
     * @param onTest
     * @return
     */
    private Disposable runDisposable(Observable<Integer> onTest)
    {
        return onTest.subscribe(event -> {
            logger.info("GOT event {}", event);
            Thread.sleep(1000);
        });
    }

    /**
     * onErrorReturn, intercepts an error and let you return an item. If you
     * don't need the throwable, then use onErrorReturnItem instead.
     */
    @Test
    public void onErrorReturnTest()
    {
        logger.info("Using onErrorReturn");
        Observable.just(5, 2, 4, 0, 3, 2, 8)
                  .map(i -> 10 / i)
                  .onErrorReturn(e -> {
                      logger.error("GOT ERROR", e);
                      return -1;
                  })
                  .subscribe(i -> System.out.println("RECEIVED: " + i),
                             e -> System.out.println("RECEIVED ERROR: " + e)
                  );

        logger.info("Using onErrorReturnItem");
        Observable.just(5, 2, 4, 0, 3, 2, 8)
                  .map(i -> 10 / i)
                  .onErrorReturnItem(-1)
                  .subscribe(i -> System.out.println("RECEIVED: " + i),
                             e -> System.out.println("RECEIVED ERROR: " + e)
                  );
    }

    /**
     * Retry will re-subscribe to the original Observable every time it
     * encounters an error. There are overloaded versions that will retry a
     * maximum number of times or accepts a Predicate to conditionally control
     * when retry is attempted.
     */
    @Test
    public void retryTest()
    {

        Random random = new Random();

        Observable.just("hola", "que", "tal")
                  .startWith("SUBSCRIBING!")
                  .doOnNext(event -> logger.info("got {}", event))
                  .map(String::length)
                  .doAfterNext(event -> logger.info("event processed"))
                  .map(i -> {
                      logger.info("got {}", i);
                      int divisor = random.nextInt() % 2;
                      logger.info("dividing by {}", divisor);
                      return i / divisor;
                  })
                  .retry()
                  .subscribe(event -> logger.info("GOT {}", event));
    }

    /**
     * The doOnNext, doAfterNext, doOnComplete, doOnError, ect (or the wrap all
     * doOnEach) operators does not affect the operation or transform the
     * emissions in any way. We just create a side-effect for each event that
     * occurs at that point in the chain
     */
    @Test
    public void onErrorTest()
    {
        Observable.just(1, 2, 3, 4, 5)
                  .doAfterTerminate(
                      () -> logger.info("Source completed emissions"))
                  .doOnNext(event -> logger.info("Got emission: {}", event))
                  .map(i -> i + 100)
                  .subscribe(event -> logger.info("GOT {}", event));

        Observable.fromCallable(() -> Arrays.asList(5, 2 / 0, 4, 0, 3, 2, 8))
                  .doOnError(e -> System.out.println("Source failed!"))
                  .map(i -> Observable.fromIterable(i))
                  .doOnError(e -> System.out.println("Division failed!"))
                  .subscribe(i -> System.out.println("RECEIVED: " + i),
                             e -> System.out.println("RECEIVED ERROR: " + e)
                  );
    }

    @Test
    public void groupingTest()
    {

        Observable<GroupedObservable<Integer, String>> grouping =
            Observable.just("hola", "que", "tal", "estas", "hoy")
                      .groupBy(s -> s.length());

        grouping.flatMapSingle(GroupedObservable::toList)
                .subscribe(event -> logger.info("GOT {}", event));

        grouping.flatMapSingle(grp -> grp
            .reduce("", (s1, s2) -> s1.equals("") ? s2 : s1 + "," + s2)
            .map(s -> grp.getKey() + ": " + s))
                .subscribe(event -> logger.info("GOT {}", event));
    }
}


