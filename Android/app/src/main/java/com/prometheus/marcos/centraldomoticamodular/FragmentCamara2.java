package com.prometheus.marcos.centraldomoticamodular;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class FragmentCamara2 extends Fragment{
    Button buttonCamara2;

    public FragmentCamara2(){
    }

    @Override
    public View onCreateView(LayoutInflater inflador, ViewGroup contenedor, Bundle savedInstanceState){
        View vista=inflador.inflate(R.layout.fragment_camara2, contenedor, false);
        buttonCamara2=(Button)vista.findViewById(R.id.buttonCamara2);
        buttonCamara2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("rtsp://admin:admin@192.168.1.200:554/cam/realmonitor?channel=2&subtype=0"));
                startActivity(intent);
            }
        });
        return vista;
    }
}
