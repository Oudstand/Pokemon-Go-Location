package com.kastner.richter.pokemongolocation;

import android.app.ListActivity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by markk on 17.07.2016.
 */
public class PokemoncodeActivity extends ListActivity {

    public static String RESULT_POKEMONCODE = "pokemoncode";
    public String[] pokeonnames, pokemoncodes;
    private TypedArray imgs;
    private List<Pokemon> pokemonList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        populatePokemonList();
        ArrayAdapter<Pokemon> adapter = new PokemonListArrayAdapter(this, pokemonList);
        setListAdapter(adapter);
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Pokemon p = pokemonList.get(position);
                Intent returnIntent = new Intent();
                returnIntent.putExtra(RESULT_POKEMONCODE, p.getCode());
                setResult(RESULT_OK, returnIntent);
                imgs.recycle(); //recycle images
                finish();
            }
        });
    }

    private void populatePokemonList() {
        pokemonList = new ArrayList<Pokemon>();
        pokeonnames = getResources().getStringArray(R.array.pokemon_names);
        pokemoncodes = getResources().getStringArray(R.array.pokemon_codes);
        imgs = getResources().obtainTypedArray(R.array.pokemon_icons);
        for (int i = 0; i < pokemoncodes.length; i++) {
            pokemonList.add(new Pokemon(pokeonnames[i], pokemoncodes[i], imgs.getDrawable(i)));
        }
    }

}
