package com.example.campusgo;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusgo.adapter.SugerenciasAdapter;
import com.example.campusgo.util.Helper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MapaSeleccionadoActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Button btnConfirmar;
    private PlacesClient placesClient;

    // --- Vistas para la búsqueda ---
    private SearchBar searchBar;
    private SearchView searchView;
    private RecyclerView recyclerViewSugerencias;
    private SugerenciasAdapter sugerenciasAdapter;

    // --- Lógica de Selección ---
    private Place lugarSeleccionado; // Almacena el lugar seleccionado de la búsqueda

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa_seleccion);

        // --- Inicialización de Vistas ---
        btnConfirmar = findViewById(R.id.btnConfirmarUbicacion);
        searchBar = findViewById(R.id.search_bar);
        searchView = findViewById(R.id.search_view);
        recyclerViewSugerencias = findViewById(R.id.recycler_view_sugerencias);

        // --- Inicialización de Places API ---
        try {
            if (!Places.isInitialized()) {
                String apiKey = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA).metaData.getString("com.google.android.geo.API_KEY");
                Places.initialize(getApplicationContext(), apiKey);
            }
            placesClient = Places.createClient(this);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: Clave de API no encontrada en AndroidManifest.", Toast.LENGTH_LONG).show();
            finish(); // Cierra la actividad si la clave no está
        }


        // --- Inicialización del Mapa ---
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // --- Configuración de Listeners ---
        btnConfirmar.setOnClickListener(v -> confirmarUbicacion());
        setupSearch();
    }

    private void setupSearch() {
        sugerenciasAdapter = new SugerenciasAdapter(sugerencia -> {
            obtenerDetallesDelLugar(sugerencia.getPlaceId());
            searchView.hide();
        });
        recyclerViewSugerencias.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewSugerencias.setAdapter(sugerenciasAdapter);

        searchView.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 2) {
                    buscarSugerencias(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void buscarSugerencias(String query) {
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .setCountries("PE") // Limita la búsqueda a Perú
                .setSessionToken(AutocompleteSessionToken.newInstance())
                .build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener(response -> {
            sugerenciasAdapter.actualizarSugerencias(response.getAutocompletePredictions());
        }).addOnFailureListener(exception -> {
            Toast.makeText(this, "Error al buscar: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void obtenerDetallesDelLugar(String placeId) {
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.DISPLAY_NAME, Place.Field.LOCATION, Place.Field.FORMATTED_ADDRESS);
        FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields).build();

        placesClient.fetchPlace(request).addOnSuccessListener(response -> {
            lugarSeleccionado = response.getPlace(); // GUARDAMOS EL LUGAR


            if (lugarSeleccionado != null && lugarSeleccionado.getLocation() != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lugarSeleccionado.getLocation(), 16f));
                //searchBar.setText(lugarSeleccionado.getAdrFormatAddress()); // Usamos getAddress()
                String direccionTxt = lugarSeleccionado.getFormattedAddress();
                if (direccionTxt == null) {
                    direccionTxt = lugarSeleccionado.getDisplayName();
                }
                searchBar.setText(direccionTxt);

            }
            // ------------------------

        }).addOnFailureListener(exception -> {
            lugarSeleccionado = null; // Limpiamos en caso de error
            Toast.makeText(this, "No se pudo obtener el lugar: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

        //Movimiento del usuario en el mapa
        mMap.setOnCameraMoveStartedListener(reason -> {
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                searchBar.setText("Buscando Dirección...");
                if (lugarSeleccionado != null) {
                    lugarSeleccionado = null;
                }
            }

        });

        //Movimiento del mapa completo
        mMap.setOnCameraIdleListener(() -> {
            LatLng centro = mMap.getCameraPosition().target;
            new Thread(() -> {
                try {
                    // Usamos tu Helper para obtener la dirección
                    String direccion = Helper.obtenerDireccionMapa(this, centro.latitude, centro.longitude);

                    // C. Volver al hilo principal para actualizar la UI (el SearchBar)
                    runOnUiThread(() -> {
                        if (direccion != null && !direccion.isEmpty()) {
                            searchBar.setText(direccion);
                        } else {
                            searchBar.setText("Ubicación desconocida");
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        });

        // Ubicación inicial (Chiclayo)
        LatLng ubicacionInicial = new LatLng(-6.77137, -79.84088);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionInicial, 16f));

    }

    private void confirmarUbicacion() {
        if (mMap == null) return;

        LatLng centroMapa = mMap.getCameraPosition().target;
        double lat = centroMapa.latitude;
        double lng = centroMapa.longitude;
        String direccion;

        // VERSIÓN MEJORADA: Usa la dirección del lugar seleccionado si existe y está cerca.
        if (lugarSeleccionado != null && isLocationClose(lugarSeleccionado.getLocation(), centroMapa)) {
            direccion = lugarSeleccionado.getFormattedAddress();
        } else {
            // Si no, usa el Helper como respaldo.
            try {
                direccion = Helper.obtenerDireccionMapa(this, lat, lng);
            } catch (Exception e) {
                direccion = "";
                e.printStackTrace();
            }
        }

        if (direccion == null || direccion.isEmpty()) {
            direccion = String.format(Locale.getDefault(), "Ubicación: %.5f, %.5f", lat, lng);
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra("latitud", lat);
        resultIntent.putExtra("longitud", lng);
        resultIntent.putExtra("direccion", direccion);

        setResult(RESULT_OK, resultIntent);
        finish();
    }

    /**
     * Comprueba si dos coordenadas están muy cerca (ej. menos de 20 metros).
     */
    private boolean isLocationClose(LatLng p1, LatLng p2) {
        if (p1 == null || p2 == null) return false;
        Location loc1 = new Location("");
        loc1.setLatitude(p1.latitude);
        loc1.setLongitude(p1.longitude);

        Location loc2 = new Location("");
        loc2.setLatitude(p2.latitude);
        loc2.setLongitude(p2.longitude);

        return loc1.distanceTo(loc2) < 20;
    }
}