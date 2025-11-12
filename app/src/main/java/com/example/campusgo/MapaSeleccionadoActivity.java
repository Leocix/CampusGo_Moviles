package com.example.campusgo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.campusgo.util.Helper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

public class MapaSeleccionadoActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private Button btnConfirmar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa_seleccion);

        btnConfirmar = findViewById(R.id.btnConfirmarUbicacion);

        // Inicializar el mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnConfirmar.setOnClickListener(v -> confirmarUbicacion());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Ubicación por defecto (Ej: Chiclayo)
        LatLng chiclayo = new LatLng(-6.77137, -79.84088);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(chiclayo, 15));

        // Opcional: Si tienes permisos de ubicación, activa el botón de "mi ubicación"
        // if (permisosConcedidos) mMap.setMyLocationEnabled(true);
    }

    private void confirmarUbicacion() {
        if (mMap == null) return;

        // 1. Obtener la coordenada del CENTRO de la pantalla (donde está el pin)
        LatLng centroMapa = mMap.getCameraPosition().target;

        double lat = centroMapa.latitude;
        double lng = centroMapa.longitude;

        // 2. Obtener la dirección legible (Calle, Ciudad...) usando tu Helper
        // Nota: Esto idealmente va en un hilo secundario, pero Helper lo maneja básico.
        String direccion = Helper.obtenerDireccionMapa(this, lat, lng);

        if (direccion.isEmpty()) {
            direccion = "Ubicación seleccionada (" + lat + ", " + lng + ")";
        }

        // 3. Devolver los datos al Fragmento
        Intent resultIntent = new Intent();
        resultIntent.putExtra("latitud", lat);
        resultIntent.putExtra("longitud", lng);
        resultIntent.putExtra("direccion", direccion);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

}
