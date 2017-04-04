package com.prometheus.marcos.centraldomoticamodular;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class FragmentExterior extends Fragment{
    WebView webapp_exterior;

    public FragmentExterior(){
    }

    @Override
    public View onCreateView(LayoutInflater inflador, ViewGroup contenedor, Bundle savedInstanceState){
        View vista=inflador.inflate(R.layout.fragment_exterior, contenedor, false);
        webapp_exterior=(WebView)vista.findViewById(R.id.webapp_exterior);
        webapp_exterior.loadUrl("http://192.168.1.100/pages-webapp/modulo-exterior-webapp.html");
        WebSettings configuracionWeb = webapp_exterior.getSettings();
        configuracionWeb.setJavaScriptEnabled(true);
        webapp_exterior.setWebViewClient(new WebViewClient());
        return vista;
    }
}
