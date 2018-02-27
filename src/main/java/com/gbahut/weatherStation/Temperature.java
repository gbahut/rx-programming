package com.gbahut.weatherStation;

/**
 * Created by gbahut on 18/02/2018.
 */
public class Temperature
{
    private final int temperature;

    public Temperature(int temperature){
        this.temperature = temperature;
    }

    public int getTemperature()
    {
        return temperature;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Temperature that = (Temperature) o;

        return temperature == that.temperature;
    }

    @Override
    public int hashCode()
    {
        return temperature;
    }
}
