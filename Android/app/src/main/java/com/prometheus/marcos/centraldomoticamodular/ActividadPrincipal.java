package com.prometheus.marcos.centraldomoticamodular;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.NavigationView;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ActividadPrincipal extends AppCompatActivity {
    private String usuario="admin";
    private String contrasena="central";
    private int tiempoRetardoSalida=2000;
    private long tiempoDeInicioSalida;
    private Button buttonInicioSesion;
    private EditText editTextUsuario;
    private EditText editTextContrasena;
    private Toolbar barraAplicacion;
    private DrawerLayout webAppLayout;
    private NavigationView menuNavegacion;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_principal);

        editTextUsuario=(EditText)findViewById(R.id.editTextUsuario);
        editTextContrasena=(EditText)findViewById(R.id.editTextContrasena);
        buttonInicioSesion=(Button)findViewById(R.id.buttonInicioSesion);
        buttonInicioSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verificarUsuarioContrasena();
            }
        });
    }

    @Override
    public void onBackPressed(){
        if(webAppLayout!=null){
            if(webAppLayout.isDrawerOpen(GravityCompat.START)){
                webAppLayout.closeDrawers();
            }
            else{
                if(tiempoDeInicioSalida+tiempoRetardoSalida>System.currentTimeMillis()){
                    finish();
                }
                else{
                    Toast.makeText(getBaseContext(),"Presiona otra vez para salir",Toast.LENGTH_SHORT).show();
                }
                tiempoDeInicioSalida=System.currentTimeMillis();
            }
        }
        else{
            if(tiempoDeInicioSalida+tiempoRetardoSalida>System.currentTimeMillis()){
                finish();
            }
            else{
                Toast.makeText(getBaseContext(),"Presiona otra vez para salir",Toast.LENGTH_SHORT).show();
            }
            tiempoDeInicioSalida=System.currentTimeMillis();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                webAppLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void verificarUsuarioContrasena(){
        if((editTextUsuario.getText().toString().compareTo(usuario)==0)&&(editTextContrasena.getText().toString().compareTo(contrasena)==0)){
            Toast.makeText(getBaseContext(),"Has iniciado sesión como administrador",Toast.LENGTH_LONG).show();
            cerrarTeclado();
            desplegarInterfazWebapp();
        }
        else{
            Toast.makeText(getBaseContext(),"Datos incorrectos...",Toast.LENGTH_SHORT).show();
        }
    }

    public void cerrarTeclado(){
        InputMethodManager entradaTeclado=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        entradaTeclado.hideSoftInputFromWindow(editTextContrasena.getWindowToken(), 0);
    }

    public void desplegarInterfazWebapp(){
        setContentView(R.layout.webapp_central);
        barraAplicacion=(Toolbar)findViewById(R.id.appbar);
        setSupportActionBar(barraAplicacion);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_nav_menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        webAppLayout=(DrawerLayout)findViewById(R.id.drawer_layout);
        menuNavegacion=(NavigationView)findViewById(R.id.navview);
        menuNavegacion.getMenu().getItem(0).setChecked(true);
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new FragmentInicio()).commit();

        menuNavegacion.setNavigationItemSelectedListener(
            new NavigationView.OnNavigationItemSelectedListener(){
                @Override
                public boolean onNavigationItemSelected(MenuItem menuItem){
                    Fragment fragment = null;
                    switch (menuItem.getItemId()){
                        case R.id.menu_inicio:
                            fragment = new FragmentInicio();
                            break;
                        case R.id.menu_notificaciones:
                            fragment = new FragmentNotificaciones();
                            break;
                        case R.id.menu_habitacion:
                            fragment = new FragmentHabitacion();
                            break;
                        case R.id.menu_cocina:
                            fragment = new FragmentCocina();
                            break;
                        case R.id.menu_exterior:
                            fragment = new FragmentExterior();
                            break;
                        case R.id.menu_camara1:
                            fragment = new FragmentCamara1();
                            break;
                        case R.id.menu_camara2:
                            fragment = new FragmentCamara2();
                            break;
                        case R.id.menu_alarmas:
                            fragment = new FragmentAlarmas();
                            break;
                        case R.id.menu_acercade:
                            fragment = new FragmentAcercaDe();
                            break;
                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();
                    menuItem.setChecked(true);
                    getSupportActionBar().setTitle(menuItem.getTitle());
                    webAppLayout.closeDrawers();
                    return true;
                }
            });
    }
}
