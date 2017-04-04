package com.prometheus.marcos.centraldomoticamodular;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class FragmentCamara1 extends Fragment{
    Button buttonCamara1;

    public FragmentCamara1(){
    }

    @Override
    public View onCreateView(LayoutInflater inflador, ViewGroup contenedor, Bundle savedInstanceState){
        View vista=inflador.inflate(R.layout.fragment_camara1, contenedor, false);
        buttonCamara1=(Button)vista.findViewById(R.id.buttonCamara1);
        buttonCamara1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("rtsp://admin:admin@192.168.1.200:554/cam/realmonitor?channel=1&subtype=0"));
                startActivity(intent);
            }
        });
        return vista;
    }
}
