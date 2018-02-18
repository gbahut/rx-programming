package com.gbahut.weatherStation;

/**
 * Created by gbahut on 18/02/2018.
 */
public class Weather
{
    private final Temperature t;
    private final Wind w;

    public Weather(Temperature t, Wind w)
    {
        this.t = t;
        this.w = w;
    }

    public Temperature getT()
    {
        return t;
    }

    public Wind getW()
    {
        return w;
    }

    @Override
    public String toString()
    {
        return String.format("Weather -> Temp %d, Wind %d", t.getTemperature(), w.getWind());
    }
}
