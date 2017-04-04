package com.prometheus.marcos.centraldomoticamodular;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class FragmentHabitacion extends Fragment{
    WebView webapp_habitacion;

    public FragmentHabitacion(){
    }

    @Override
    public View onCreateView(LayoutInflater inflador, ViewGroup contenedor, Bundle savedInstanceState){
        View vista=inflador.inflate(R.layout.fragment_habitacion, contenedor, false);
        webapp_habitacion=(WebView)vista.findViewById(R.id.webapp_habitacion);
        webapp_habitacion.loadUrl("http://192.168.1.100/pages-webapp/modulo-habitacion-webapp.html");
        WebSettings configuracionWeb = webapp_habitacion.getSettings();
        configuracionWeb.setJavaScriptEnabled(true);
        webapp_habitacion.setWebViewClient(new WebViewClient());
        return vista;
    }
}
