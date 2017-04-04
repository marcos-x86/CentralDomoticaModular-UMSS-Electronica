package com.prometheus.marcos.centraldomoticamodular;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class FragmentCocina extends Fragment{
    WebView webapp_cocina;

    public FragmentCocina(){
    }

    @Override
    public View onCreateView(LayoutInflater inflador, ViewGroup contenedor, Bundle savedInstanceState){
        View vista=inflador.inflate(R.layout.fragment_cocina, contenedor, false);
        webapp_cocina=(WebView)vista.findViewById(R.id.webapp_cocina);
        webapp_cocina.loadUrl("http://192.168.1.100/pages-webapp/modulo-cocina-webapp.html");
        WebSettings configuracionWeb = webapp_cocina.getSettings();
        configuracionWeb.setJavaScriptEnabled(true);
        webapp_cocina.setWebViewClient(new WebViewClient());
        return vista;
    }
}
