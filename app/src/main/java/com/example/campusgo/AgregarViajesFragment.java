package com.example.campusgo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.campusgo.databinding.FragmentAgregarViajesBinding;
import com.example.campusgo.model.VehiculoData;
import com.example.campusgo.request.ViajeRequest;
import com.example.campusgo.response.VehiculoListadoResponse;
import com.example.campusgo.response.ViajeResponse;
import com.example.campusgo.retrofit.ApiService;
import com.example.campusgo.retrofit.RetrofitClient;
import com.example.campusgo.sharedpreferences.LoginStorage;
import com.example.campusgo.util.Pickers;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AgregarViajesFragment extends Fragment {

    private FragmentAgregarViajesBinding binding;
    private ApiService apiService;

    // Variables para almacenar datos del formulario
    private double latOrigen = 0.0, lngOrigen = 0.0;
    private double latDestino = 0.0, lngDestino = 0.0;
    private int vehiculoIdSeleccionado = -1;
    private int asientosDelVehiculo = 0;

    // Control para saber si estamos eligiendo "origen" o "destino"
    private String tipoSeleccionMapa = "";

    // Launcher para recibir resultados del Mapa
    private final ActivityResultLauncher<Intent> mapaLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    double lat = data.getDoubleExtra("latitud", 0);
                    double lng = data.getDoubleExtra("longitud", 0);
                    String direccion = data.getStringExtra("direccion");

                    if ("origen".equals(tipoSeleccionMapa)) {
                        latOrigen = lat;
                        lngOrigen = lng;
                        binding.txtOrigen.setText(direccion);
                    } else if ("destino".equals(tipoSeleccionMapa)) {
                        latDestino = lat;
                        lngDestino = lng;
                        binding.txtDestino.setText(direccion);
                    }
                }
            }
    );

    public AgregarViajesFragment() {
        // Constructor vacío requerido
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAgregarViajesBinding.inflate(inflater, container, false);
        apiService = RetrofitClient.createService(); // Inicializamos Retrofit aquí
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupUI();

        // Cargar datos iniciales (Vehículos del conductor)
        if (getContext() != null) {
            int conductorId = LoginStorage.getUserId(requireContext());
            if (conductorId != 0) {
                cargarVehiculos(conductorId);
            } else {
                Toast.makeText(getContext(), "Error: No se pudo identificar al conductor.", Toast.LENGTH_LONG).show();
                NavHostFragment.findNavController(this).popBackStack();
            }
        }
    }

    private void setupUI() {
        // Listeners para el mapa
        binding.txtOrigen.setOnClickListener(v -> {
            tipoSeleccionMapa = "origen";
            abrirMapa();
        });
        binding.txtDestino.setOnClickListener(v -> {
            tipoSeleccionMapa = "destino";
            abrirMapa();
        });

        // Listeners para Fecha y Hora usando tu clase Pickers
        binding.txtFechaSalida.setOnClickListener(v -> {
            // "posterior" bloquea fechas pasadas
            Pickers.obtenerFecha(requireContext(), binding.txtFechaSalida, "posterior");
        });

        binding.txtHoraSalida.setOnClickListener(v -> {
            Pickers.obtenerHora(requireContext(), binding.txtHoraSalida);
        });

        // Botón de publicar
        binding.btnPublicarViaje.setOnClickListener(v -> intentarPublicarViaje());
    }

    // --- 1. Lógica para cargar vehículos (Directa con API) ---
    private void cargarVehiculos(int conductorId) {
        Call<VehiculoListadoResponse> call = apiService.listarVehiculosPorConductor(conductorId);

        call.enqueue(new Callback<VehiculoListadoResponse>() {
            @Override
            public void onResponse(@NonNull Call<VehiculoListadoResponse> call, @NonNull Response<VehiculoListadoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Obtenemos la lista desde el objeto de respuesta
                    List<VehiculoData> lista = response.body().getData();

                    if (lista != null && !lista.isEmpty()) {
                        configurarSpinner(lista);
                    } else {
                        Toast.makeText(getContext(), "No tienes vehículos registrados.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Error al obtener vehículos.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<VehiculoListadoResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void configurarSpinner(List<VehiculoData> vehiculos) {
        if (getContext() == null) return;

        // El toString() de VehiculoData definirá qué se muestra en la lista
        ArrayAdapter<VehiculoData> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, vehiculos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerVehiculos.setAdapter(adapter);

        binding.spinnerVehiculos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                VehiculoData v = vehiculos.get(position);
                vehiculoIdSeleccionado = v.getId();
                asientosDelVehiculo = v.getPasajeros();
                binding.txtAsientos.setText(String.valueOf(asientosDelVehiculo));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                vehiculoIdSeleccionado = -1;
            }
        });
    }

    // --- 2. Lógica para publicar viaje ---
    private void intentarPublicarViaje() {
        if (!validarCampos()) {
            return;
        }

        // Obtener los textos directos de la interfaz
        String fechaStr = binding.txtFechaSalida.getText().toString();
        String horaStr = binding.txtHoraSalida.getText().toString();

        // Convertir usando el método corregido
        String fechaHoraMySQL = convertirFechaHoraMySQL(fechaStr, horaStr);

        if (fechaHoraMySQL == null) {
            Toast.makeText(getContext(), "Error en el formato de fecha/hora. Intenta seleccionarlos de nuevo.", Toast.LENGTH_LONG).show();
            return;
        }

        // Crear el objeto Request
        ViajeRequest request = new ViajeRequest();
        request.setVehiculoId(vehiculoIdSeleccionado);
        request.setPuntoPartida(binding.txtOrigen.getText().toString());
        request.setDestino(binding.txtDestino.getText().toString());
        request.setLatPartida(latOrigen);
        request.setLngPartida(lngOrigen);
        request.setLatDestino(latDestino);
        request.setLngDestino(lngDestino);
        request.setFechaHoraSalida(fechaHoraMySQL); // Fecha ya formateada correctamente
        request.setAsientosOfertados(asientosDelVehiculo);

        String restricciones = binding.txtRestricciones.getText().toString();
        request.setRestricciones(restricciones.isEmpty() ? "Ninguna" : restricciones);
        request.setEstadoId(9); // 9 = Disponible

        // Llamada a la API
        Call<ViajeResponse> call = apiService.registrarViaje(request);
        call.enqueue(new Callback<ViajeResponse>() {
            @Override
            public void onResponse(@NonNull Call<ViajeResponse> call, @NonNull Response<ViajeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getContext(), response.body().getMessage(), Toast.LENGTH_LONG).show();
                    NavHostFragment.findNavController(AgregarViajesFragment.this).popBackStack();
                } else {
                    try {
                        String errorMsg = "Error desconocido";
                        if (response.errorBody() != null) {
                            JSONObject jsonError = new JSONObject(response.errorBody().string());
                            errorMsg = jsonError.optString("message", errorMsg);
                        }
                        Toast.makeText(getContext(), "Error: " + errorMsg, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Error al procesar respuesta del servidor", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ViajeResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validarCampos() {
        if (vehiculoIdSeleccionado == -1) {
            Toast.makeText(getContext(), "Debes seleccionar un vehículo.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (binding.txtOrigen.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Debes seleccionar un punto de origen.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (binding.txtDestino.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Debes seleccionar un destino.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (binding.txtFechaSalida.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Debes seleccionar una fecha.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (binding.txtHoraSalida.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Debes seleccionar una hora.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void abrirMapa() {
        // Usamos requireContext() para seguridad
        Intent intent = new Intent(requireContext(), MapaSeleccionadoActivity.class);
        mapaLauncher.launch(intent);
    }

    /**
     * Método auxiliar para convertir el formato visual de fecha y hora (de Pickers)
     * al formato estándar de base de datos (yyyy-MM-dd HH:mm:ss).
     */
    private String convertirFechaHoraMySQL(String fecha, String hora) {
        try {
            // 1. Limpieza de la hora para estandarizar
            // Tu Pickers devuelve "02:30 p.m." -> Queremos "02:30 PM"
            String horaLimpia = hora.replace(".", "")  // Quita puntos: "02:30 pm"
                    .toUpperCase()      // Mayúsculas: "02:30 PM"
                    .trim();            // Quita espacios extra

            String fechaHoraCombinada = fecha + " " + horaLimpia;

            // 2. Formato de lectura (Input)
            // ¡IMPORTANTE! Usamos Locale.US para asegurar que entienda "AM" y "PM"
            SimpleDateFormat formatoEntrada = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.US);

            // 3. Formato de escritura (Output para MySQL)
            SimpleDateFormat formatoSalida = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

            return formatoSalida.format(formatoEntrada.parse(fechaHoraCombinada));

        } catch (Exception e) {
            Log.e("FECHA_ERROR", "Error al convertir fecha: " + fecha + " hora: " + hora, e);
            return null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}