package com.kastner.richter.pokemongolocation;

import android.graphics.drawable.Drawable;

/**
 * Created by markk on 17.07.2016.
 */
public class Pokemon {
    private String name;
    private String code;
    private Drawable icon;
    public Pokemon(String name, String code, Drawable icon){
        this.name = name;
        this.code = code;
        this.icon = icon;
    }
    public String getName() {
        return name;
    }
    public Drawable getIcon() {
        return icon;
    }
    public String getCode() {
        return code;
    }
}
