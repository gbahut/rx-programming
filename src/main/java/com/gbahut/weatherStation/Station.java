package com.gbahut.weatherStation;

import rx.Observable;

/**
 * Created by gbahut on 18/02/2018.
 */
public class Station implements  WeatherStation

{
    @Override
    public Observable<Temperature> temperature()
    {
        return Observable.just(10, 12, 14).map(Temperature::new);
    }

    @Override
    public Observable<Wind> wind()
    {
        return Observable.just(100, 120).map(Wind::new);
    }
}
