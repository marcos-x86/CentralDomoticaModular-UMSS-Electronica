package com.prometheus.marcos.centraldomoticamodular;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class FragmentAlarmas extends Fragment{
    WebView webapp_alarmas;

    public FragmentAlarmas(){
    }

    @Override
    public View onCreateView(LayoutInflater inflador, ViewGroup contenedor, Bundle savedInstanceState){
        View vista=inflador.inflate(R.layout.fragment_alarmas, contenedor, false);
        webapp_alarmas=(WebView)vista.findViewById(R.id.webapp_alarmas);
        webapp_alarmas.loadUrl("http://192.168.1.100/pages-webapp/alarmas-webapp.html");
        WebSettings configuracionWeb = webapp_alarmas.getSettings();
        configuracionWeb.setJavaScriptEnabled(true);
        webapp_alarmas.setWebViewClient(new WebViewClient());
        return vista;
    }
}
