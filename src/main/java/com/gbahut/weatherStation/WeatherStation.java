package com.gbahut.weatherStation;

import rx.Observable;

/**
 * Created by gbahut on 18/02/2018.
 */
public interface WeatherStation
{

    Observable<Temperature> temperature();
    Observable<Wind> wind();
}
