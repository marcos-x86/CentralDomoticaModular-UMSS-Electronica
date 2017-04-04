package com.prometheus.marcos.centraldomoticamodular;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class FragmentAcercaDe extends Fragment{
    WebView webapp_acercade;

    public FragmentAcercaDe(){
    }

    @Override
    public View onCreateView(LayoutInflater inflador, ViewGroup contenedor, Bundle savedInstanceState){
        View vista=inflador.inflate(R.layout.fragment_acercade, contenedor, false);
        webapp_acercade=(WebView)vista.findViewById(R.id.webapp_acercade);
        webapp_acercade.loadUrl("http://192.168.1.100/pages-webapp/acercade-webapp.html");
        WebSettings configuracionWeb = webapp_acercade.getSettings();
        configuracionWeb.setJavaScriptEnabled(true);
        webapp_acercade.setWebViewClient(new WebViewClient());
        return vista;
    }
}
