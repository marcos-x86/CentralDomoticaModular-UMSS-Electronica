package com.prometheus.marcos.centraldomoticamodular;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class FragmentNotificaciones extends Fragment{

    WebView webapp_notificaciones;

    public FragmentNotificaciones(){

    }

    @Override
    public View onCreateView(LayoutInflater inflador, ViewGroup contenedor, Bundle savedInstanceState){
        View vista=inflador.inflate(R.layout.fragment_notificaciones, contenedor, false);
        webapp_notificaciones=(WebView)vista.findViewById(R.id.webapp_notificaciones);
        webapp_notificaciones.loadUrl("http://192.168.1.100/pages-webapp/notificaciones-webapp.html");
        WebSettings configuracionWeb = webapp_notificaciones.getSettings();
        configuracionWeb.setJavaScriptEnabled(true);
        webapp_notificaciones.setWebViewClient(new WebViewClient());
        return vista;
    }
}
