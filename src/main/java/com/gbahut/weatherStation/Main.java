package com.gbahut.weatherStation;

import io.reactivex.Observable;

/**
 * Created by gbahut on 18/02/2018.
 */
public class Main
{


    public static  void  main(String... args){

        WeatherStation station = new Station();

        Observable<Temperature> temps = station.temperature();

        Observable<Wind> wind = station.wind();

        temps.zipWith(wind, Weather::new).subscribe(System.out::println);
    }
}
