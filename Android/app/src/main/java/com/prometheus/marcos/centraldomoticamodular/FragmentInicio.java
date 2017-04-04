package com.prometheus.marcos.centraldomoticamodular;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class FragmentInicio extends Fragment{
    WebView webapp_inicio;

    public FragmentInicio(){
    }

    @Override
    public View onCreateView(LayoutInflater inflador, ViewGroup contenedor, Bundle savedInstanceState){
        View vista=inflador.inflate(R.layout.fragment_inicio, contenedor, false);
        webapp_inicio=(WebView)vista.findViewById(R.id.webapp_inicio);
        webapp_inicio.loadUrl("http://192.168.1.100/pages-webapp/menuprincipal-webapp.html");
        WebSettings configuracionWeb = webapp_inicio.getSettings();
        configuracionWeb.setJavaScriptEnabled(true);
        webapp_inicio.setWebViewClient(new WebViewClient());
        return vista;
    }
}
