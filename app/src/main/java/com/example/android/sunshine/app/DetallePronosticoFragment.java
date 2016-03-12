package com.example.android.sunshine.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetallePronosticoFragment extends Fragment {

    private static final String PRONOSTICO_SHARE_HSHTAG= " #SunshineApp";
    private String mPronosticoStr;

    public DetallePronosticoFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootview= inflater.inflate(R.layout.fragment_detalle_pronostico, container, false);
        Intent intent= getActivity().getIntent();
        if(intent!=null && intent.hasExtra(Intent.EXTRA_TEXT)){
            mPronosticoStr= intent.getStringExtra(Intent.EXTRA_TEXT);
            ((TextView)rootview.findViewById(R.id.detalle_clima)).setText(mPronosticoStr);
        }
        return rootview;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detalle_fragment, menu);
        MenuItem menuItem= menu.findItem(R.id.action_share);
        ShareActionProvider mShareActionProvider= (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        if(mShareActionProvider!=null){
            mShareActionProvider.setShareIntent(crearSharePronosticoIntent());
        }else{
            Log.d("Detalle pronostico", "Share Action provider es null?");
        }
    }

    private Intent crearSharePronosticoIntent(){
        Intent shareIntent= new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mPronosticoStr+PRONOSTICO_SHARE_HSHTAG);
        return shareIntent;
    }
}
