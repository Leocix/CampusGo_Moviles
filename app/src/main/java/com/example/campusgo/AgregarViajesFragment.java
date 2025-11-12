package com.example.campusgo;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.campusgo.response.ViajeResponse;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.campusgo.databinding.FragmentAgregarViajesBinding;
import com.example.campusgo.model.VehiculoData;
import com.example.campusgo.request.ViajeRequest;
import com.example.campusgo.response.VehiculoListadoResponse; // Asegúrate de tener esta clase respuesta
import com.example.campusgo.retrofit.ApiService;
import com.example.campusgo.retrofit.RetrofitClient;
import com.example.campusgo.sharedpreferences.LoginStorage;
import com.example.campusgo.util.Helper;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AgregarViajesFragment extends Fragment {

    private FragmentAgregarViajesBinding binding;
    private ApiService apiService; // Variable para la API

    private double latOrigen = 0.0, lngOrigen = 0.0;
    private double latDestino = 0.0, lngDestino = 0.0;
    private int vehiculoIdSeleccionado = -1;
    private int asientosDelVehiculo = 0;

    private String tipoSeleccionMapa = "";
    private final Calendar calendario = Calendar.getInstance();

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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAgregarViajesBinding.inflate(inflater, container, false);
        apiService = RetrofitClient.createService(); // Inicializamos Retrofit
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
                cargarVehiculos(conductorId); // Llamada directa al método local
            } else {
                Toast.makeText(getContext(), "Error: No se pudo identificar al conductor.", Toast.LENGTH_LONG).show();
                NavHostFragment.findNavController(this).popBackStack();
            }
        }
    }

    private void setupUI() {
        binding.txtOrigen.setOnClickListener(v -> {
            tipoSeleccionMapa = "origen";
            abrirMapa();
        });
        binding.txtDestino.setOnClickListener(v -> {
            tipoSeleccionMapa = "destino";
            abrirMapa();
        });
        binding.txtFechaSalida.setOnClickListener(v -> mostrarDatePicker());
        binding.txtHoraSalida.setOnClickListener(v -> mostrarTimePicker());
        binding.btnPublicarViaje.setOnClickListener(v -> intentarPublicarViaje());
    }

    // --- 1. Lógica para cargar vehículos ---
    private void cargarVehiculos(int conductorId) {
        Call<VehiculoListadoResponse> call = apiService.listarVehiculosPorConductor(conductorId);

        call.enqueue(new Callback<VehiculoListadoResponse>() {
            @Override
            public void onResponse(@NonNull Call<VehiculoListadoResponse> call, @NonNull Response<VehiculoListadoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
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

        ArrayAdapter<VehiculoData> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, vehiculos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerVehiculos.setAdapter(adapter);

        binding.spinnerVehiculos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                VehiculoData v = vehiculos.get(position);
                vehiculoIdSeleccionado = v.getId();
                // Asumiendo que VehiculoData tiene getPasajeros()
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

        // Preparar fecha para MySQL (yyyy-MM-dd HH:mm:ss)
        SimpleDateFormat sdfMySQL = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String fechaHoraMySQL = sdfMySQL.format(calendario.getTime());

        ViajeRequest request = new ViajeRequest();
        request.setVehiculoId(vehiculoIdSeleccionado);
        request.setPuntoPartida(binding.txtOrigen.getText().toString());
        request.setDestino(binding.txtDestino.getText().toString());
        request.setLatPartida(latOrigen);
        request.setLngPartida(lngOrigen);
        request.setLatDestino(latDestino);
        request.setLngDestino(lngDestino);
        request.setFechaHoraSalida(fechaHoraMySQL);
        request.setAsientosOfertados(asientosDelVehiculo);

        String restricciones = binding.txtRestricciones.getText().toString();
        request.setRestricciones(restricciones.isEmpty() ? "Ninguna" : restricciones);

        request.setEstadoId(9); // 9 = Disponible
        request.setConductorId(LoginStorage.getUserId(requireContext())); // <-- CAMPO AÑADIDO

        // Llamada directa a la API
        // --- CORRECCIÓN DE TIPO ---
        Call<ViajeResponse> call = apiService.registrarViaje(request);
        call.enqueue(new Callback<ViajeResponse>() {
            @Override
            public void onResponse(@NonNull Call<ViajeResponse> call, @NonNull Response<ViajeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Usamos el mensaje de la respuesta del servidor
                    Toast.makeText(getContext(), response.body().getMessage(), Toast.LENGTH_LONG).show();
                    NavHostFragment.findNavController(AgregarViajesFragment.this).popBackStack();
                } else {
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            JSONObject jsonError = new JSONObject(errorBody);
                            String error = jsonError.optString("message", "Error desconocido");
                            Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Error al procesar respuesta", Toast.LENGTH_SHORT).show();
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
        if (calendario.getTimeInMillis() < System.currentTimeMillis() - 60000) {
            Toast.makeText(getContext(), "La fecha y hora de salida no pueden ser en el pasado.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void abrirMapa() {
        Intent intent = new Intent(requireContext(), MapaSeleccionadoActivity.class);
        mapaLauncher.launch(intent);
    }

    private void mostrarDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            calendario.set(Calendar.YEAR, year);
            calendario.set(Calendar.MONTH, month);
            calendario.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            binding.txtFechaSalida.setText(sdf.format(calendario.getTime()));
        }, calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH), calendario.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void mostrarTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(), (view, hourOfDay, minute) -> {
            calendario.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendario.set(Calendar.MINUTE, minute);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            binding.txtHoraSalida.setText(sdf.format(calendario.getTime()));
        }, calendario.get(Calendar.HOUR_OF_DAY), calendario.get(Calendar.MINUTE), true);
        timePickerDialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}