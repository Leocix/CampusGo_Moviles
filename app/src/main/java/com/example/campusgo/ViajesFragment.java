package com.example.campusgo;

import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView; // Importa RecyclerView

import com.example.campusgo.adapter.ViajeListadoRecyclerViewAdapter;
import com.example.campusgo.databinding.FragmentViajesBinding;
import com.example.campusgo.model.LoginData;
import com.example.campusgo.model.ViajeListadoData;
import com.example.campusgo.request.DetalleViajeRequest;
import com.example.campusgo.request.ReservaRequest;
import com.example.campusgo.request.ViajeListadoRequest;
import com.example.campusgo.response.ReservaResponse;
import com.example.campusgo.response.ViajeListadoResponse;
import com.example.campusgo.retrofit.ApiService;
import com.example.campusgo.retrofit.RetrofitClient;
import com.example.campusgo.sharedpreferences.LoginStorage;
import com.example.campusgo.util.Helper;
import com.example.campusgo.util.Pickers;
// import com.google.android.material.bottomsheet.BottomSheetBehavior; // Ya no es necesario
import com.google.android.material.chip.Chip;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// --- CORRECCIÓN 1: Implementa la interfaz correcta ---
public class ViajesFragment extends Fragment implements
        ViajeListadoRecyclerViewAdapter.OnViajeDataChangedListener {
    // Ya no necesitas ReservaListadoRecyclerViewAdapter.OnReservaCancelListener

    FragmentViajesBinding binding;
    ApiService apiService;

    // --- Lista Principal (Viajes Disponibles) ---
    ViajeListadoRecyclerViewAdapter viajeAdapter;
    List<ViajeListadoData> viajeList = new ArrayList<>();

    // --- (BottomSheet ya no se usa) ---
    // ...

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentViajesBinding.inflate(inflater, container, false);
        apiService = RetrofitClient.createService();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- Configuración Común ---
        binding.swipeRefreshLayoutViajes.setColorSchemeResources(R.color.blue_campusgo_3, R.color.red, R.color._light_green, R.color.yellow);
        binding.swipeRefreshLayoutViajes.setOnRefreshListener(() -> {
            mostrarViajes();
        });

        // --- Configuración Lista Principal (Viajes) ---
        viajeAdapter = new ViajeListadoRecyclerViewAdapter(getContext(), viajeList);
        // --- CORRECCIÓN 2: Asigna el listener ---
        viajeAdapter.setOnViajeDataChangedListener(this); // <-- Esta línea es clave
        binding.recyclerViewViajes.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewViajes.setAdapter(viajeAdapter);
        setupFiltrosBusqueda();

        // --- (BottomSheet ya no se usa) ---
        // setupBottomSheet();

        //Implementar el boton confirmar
        binding.btnConfirmarReserva.setOnClickListener(v -> {
            confirmarReserva();
        });

        // --- Carga Inicial de Datos ---
        mostrarViajes();
        mostrarViajesAgregados(); // Oculta el resumen al inicio
    }

    // --- CORRECCIÓN 3: Implementa el método de la interfaz ---
    @Override
    public void onViajeDataChanged() {
        // Esto se llama CADA VEZ que agregas o quitas un viaje
        // Muestra u oculta el resumen
        mostrarViajesAgregados();
    }

    private void confirmarReserva() {
        if (ViajeListadoData.viajes.isEmpty()) {
            Toast.makeText(getContext(), "No hay viajes seleccionados", Toast.LENGTH_SHORT).show();
            return;
        }
        Helper.mensajeConfirmacion(requireContext(), "Confirme", "¿Seguro de registrar la reserva?", "SÍ, RESERVAR", "NO", () -> {
            ReservaRequest reservaRequest = new ReservaRequest();
            reservaRequest.setPasajeroId(LoginData.DATOS_SESION_USUARIO.getId());
            reservaRequest.setFechaReserva(Helper.formatearDMA_to_AMD(Helper.obtenerFechaActual()));
            reservaRequest.setObservacion(binding.txtObservacion.getText() != null ? binding.txtObservacion.getText().toString() : "");

            List<DetalleViajeRequest> detalleViajeRequestsList = new ArrayList<>();
            for (ViajeListadoData v : ViajeListadoData.viajes) {
                DetalleViajeRequest detalleViajeRequest = new DetalleViajeRequest(v.getViaje_id(), 14); // 14 = Reservado
                detalleViajeRequestsList.add(detalleViajeRequest);
            }
            reservaRequest.setDetalleViaje(detalleViajeRequestsList);

            ApiService apiService = RetrofitClient.createService();
            Call<ReservaResponse> call = apiService.registrarReserva(reservaRequest);
            call.enqueue(new Callback<ReservaResponse>() {
                @Override
                public void onResponse(Call<ReservaResponse> call, Response<ReservaResponse> response) {
                    ViajeListadoData.viajes.clear(); // Limpia la lista estática

                    // --- CORRECCIÓN 4: Notifica al adapter y oculta el resumen ---
                    mostrarViajesAgregados(); // Oculta el CardView de resumen
                    viajeAdapter.notifyDataSetChanged(); // ¡IMPORTANTE! Refresca la lista principal para resetear los botones

                    binding.txtObservacion.setText("");
                    Toast.makeText(requireContext(), "Reserva registrada satisfactoriamente", Toast.LENGTH_SHORT).show();

                    // Opcional: Vuelve a cargar los viajes para actualizar asientos disponibles
                    // mostrarViajes();
                }

                @Override
                public void onFailure(Call<ReservaResponse> call, Throwable t) {
                    Toast.makeText(getContext(), "Error al registrar: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

    }

    // --- CORRECCIÓN 5: Implementa este método (no-static) ---
    private void mostrarViajesAgregados() {
        // Asumo que el CardView de resumen tiene el ID "cardResumenReserva" en tu XML
        // Si tiene otro ID, cámbialo aquí.
        if (ViajeListadoData.viajes.isEmpty()) {
            binding.bottomSheetResumen.setVisibility(View.GONE);
        } else {
            binding.bottomSheetResumen.setVisibility(View.VISIBLE);
        }
    }

    // ... (El resto de tus métodos: setupFiltrosBusqueda, ocultarTeclado,
    //      gestionarFechaChip, gestionarTachado, mostrarViajes... están bien) ...
    // ...
    private void setupFiltrosBusqueda() {
        binding.chipDesde.setCheckable(false);
        binding.chipHasta.setCheckable(false);
        binding.chipAsientosDisponibles.setCheckable(false);
        binding.chipSinRestricciones.setCheckable(false);

        binding.chipDesde.setText(Helper.obtenerFechaActual());
        binding.chipHasta.setText(Helper.obtenerFechaActual());

        gestionarTachado(binding.chipDesde);
        gestionarTachado(binding.chipHasta);
        gestionarTachado(binding.chipAsientosDisponibles);
        gestionarTachado(binding.chipSinRestricciones);

        binding.chipSinRestricciones.setPaintFlags(binding.chipSinRestricciones.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        gestionarFechaChip(binding.chipDesde);
        gestionarFechaChip(binding.chipHasta);

        binding.txtBuscar.setOnEditorActionListener((v, actionId, event) -> {
            boolean isEnter = event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN;
            if (actionId == EditorInfo.IME_ACTION_SEARCH || isEnter) {
                mostrarViajes();
                ocultarTeclado(v);
                return true;
            }
            return false;
        });

        binding.tilBuscar.setEndIconOnClickListener(v -> {
            binding.txtBuscar.setText("");
            mostrarViajes();
        });

        binding.chipFiltrar.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            mostrarViajes();
        });
    }

    private void ocultarTeclado(View v) {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        v.clearFocus();
    }

    private void gestionarFechaChip(Chip chip) {
        chip.setOnClickListener(v -> {
            int currentFlags = chip.getPaintFlags();
            if ((currentFlags & Paint.STRIKE_THRU_TEXT_FLAG) > 0) {
                chip.setPaintFlags(currentFlags & ~Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                Pickers.obtenerFecha(getContext(), chip, "posterior");
            }
        });
    }

    private void gestionarTachado(Chip chip) {
        chip.setOnCloseIconClickListener(v -> {
            int currentFlags = chip.getPaintFlags();
            if ((currentFlags & Paint.STRIKE_THRU_TEXT_FLAG) > 0) {
                chip.setPaintFlags(currentFlags & ~Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                chip.setPaintFlags(currentFlags | Paint.STRIKE_THRU_TEXT_FLAG);
            }
        });
    }

    private void mostrarViajes() {
        binding.swipeRefreshLayoutViajes.setRefreshing(true);
        String textoBusqueda = binding.txtBuscar.getText() != null ? binding.txtBuscar.getText().toString().trim() : "";
        String desde = (binding.chipDesde.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) > 0 ? "" : Helper.formatearDMA_to_AMD(binding.chipDesde.getText().toString());
        String hasta = (binding.chipHasta.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) > 0 ? "" : Helper.formatearDMA_to_AMD(binding.chipHasta.getText().toString());
        boolean textoAsientosDisponibles = (binding.chipAsientosDisponibles.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) <= 0;
        boolean textoSinRestricciones = (binding.chipSinRestricciones.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) <= 0;

        Call<ViajeListadoResponse> call = apiService.listarViajes(new ViajeListadoRequest("destino", textoBusqueda, textoAsientosDisponibles, textoSinRestricciones, desde, hasta));
        call.enqueue(new Callback<ViajeListadoResponse>() {
            @Override
            public void onResponse(@NonNull Call<ViajeListadoResponse> call, @NonNull Response<ViajeListadoResponse> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null) {
                    viajeList.clear();
                    viajeList.addAll(Arrays.asList(response.body().getData()));
                    viajeAdapter.notifyDataSetChanged(); // <-- Esto es importante
                } else if (isAdded()) {
                    try {
                        if (response.errorBody() != null) {
                            JSONObject jsonError = new JSONObject(response.errorBody().string());
                            Helper.mensajeError(getContext(), "Error", jsonError.getString("message"));
                        }
                    } catch (IOException | JSONException e) {
                        Helper.mensajeError(getContext(), "Error", "No se pudo procesar la respuesta.");
                    }
                }
                binding.swipeRefreshLayoutViajes.setRefreshing(false);
            }

            @Override
            public void onFailure(@NonNull Call<ViajeListadoResponse> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Helper.mensajeError(getContext(), "Error de Red", "No se pudo conectar al servidor.");
                    binding.swipeRefreshLayoutViajes.setRefreshing(false);
                }
            }
        });
    }
}