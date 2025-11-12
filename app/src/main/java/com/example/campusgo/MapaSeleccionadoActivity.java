package com.example.campusgo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.example.campusgo.util.Helper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.Locale;

public class MapaSeleccionadoActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Button btnConfirmar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa_seleccion); // Asegúrate que este sea el nombre de tu XML del mapa

        btnConfirmar = findViewById(R.id.btnConfirmarUbicacion); // ID de tu botón en el XML

        // Inicializar el fragmento del mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map); // ID del fragment en el XML
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Configurar el click del botón
        btnConfirmar.setOnClickListener(v -> confirmarUbicacion());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Configuración inicial del mapa
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Mover la cámara a una ubicación por defecto (ej. Plaza de Armas de Chiclayo)
        // Puedes cambiar esto para que use la ubicación actual del GPS si tienes permisos
        LatLng ubicacionInicial = new LatLng(-6.77137, -79.84088);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionInicial, 16f));
    }

    private void confirmarUbicacion() {
        if (mMap == null) {
            Toast.makeText(this, "El mapa no está listo", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Obtener la coordenada del CENTRO exacto de la pantalla
        // (Como el pin es una imagen fija en el centro, esto nos da la posición del pin)
        LatLng centroMapa = mMap.getCameraPosition().target;

        double lat = centroMapa.latitude;
        double lng = centroMapa.longitude;

        // 2. Obtener la dirección legible (Calle, Ciudad...) usando tu Helper
        String direccion = "";
        try {
            // Tu Helper ya maneja el Geocoder
            direccion = Helper.obtenerDireccionMapa(this, lat, lng);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Fallback si no se encuentra dirección (ej. sin internet)
        if (direccion == null || direccion.isEmpty()) {
            direccion = String.format(Locale.getDefault(), "Ubicación: %.5f, %.5f", lat, lng);
        }

        // 3. Devolver los datos a AgregarViajesFragment
        Intent resultIntent = new Intent();
        resultIntent.putExtra("latitud", lat);
        resultIntent.putExtra("longitud", lng);
        resultIntent.putExtra("direccion", direccion);

        setResult(RESULT_OK, resultIntent);
        finish(); // Cierra esta actividad y regresa
    }
}