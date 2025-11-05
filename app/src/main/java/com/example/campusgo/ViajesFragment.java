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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusgo.adapter.ReservaListadoRecyclerViewAdapter;
import com.example.campusgo.adapter.ViajeListadoRecyclerViewAdapter;
import com.example.campusgo.databinding.FragmentViajesBinding;
import com.example.campusgo.model.ViajeListadoData;
import com.example.campusgo.request.ViajeListadoRequest;
import com.example.campusgo.response.ViajeListadoResponse;
import com.example.campusgo.retrofit.ApiService;
import com.example.campusgo.retrofit.RetrofitClient;
import com.example.campusgo.sharedpreferences.LoginStorage;
import com.example.campusgo.util.Helper;
import com.example.campusgo.util.Pickers;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
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

public class ViajesFragment extends Fragment implements ReservaListadoRecyclerViewAdapter.OnReservaCancelListener {

    FragmentViajesBinding binding;
    ApiService apiService;

    // --- Lista Principal (Viajes Disponibles) ---
    ViajeListadoRecyclerViewAdapter viajeAdapter;
    List<ViajeListadoData> viajeList = new ArrayList<>();

    // --- BottomSheet (Mis Reservas) ---
    ReservaListadoRecyclerViewAdapter reservaAdapter;
    List<ViajeListadoData> reservaList = new ArrayList<>();
    BottomSheetBehavior<View> bottomSheetBehavior;
    TextView txtTituloBottomSheet;

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
            cargarViajesReservados(); // Refrescar también las reservas
        });

        // --- Configuración Lista Principal (Viajes) ---
        viajeAdapter = new ViajeListadoRecyclerViewAdapter(getContext(), viajeList);
        binding.recyclerViewViajes.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewViajes.setAdapter(viajeAdapter);
        setupFiltrosBusqueda();

        // --- Configuración BottomSheet (Reservas) ---
        setupBottomSheet();

        // --- Carga Inicial de Datos ---
        mostrarViajes();
        cargarViajesReservados();
    }

    private void setupBottomSheet() {
        View bottomSheetView = binding.getRoot().findViewById(R.id.bottom_sheet_reservas);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        RecyclerView recyclerViewReservas = bottomSheetView.findViewById(R.id.recyclerViewReservas);
        txtTituloBottomSheet = bottomSheetView.findViewById(R.id.txtTituloBottomSheet);

        reservaAdapter = new ReservaListadoRecyclerViewAdapter(getContext(), reservaList, this);
        recyclerViewReservas.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewReservas.setAdapter(reservaAdapter);
    }

    private void cargarViajesReservados() {
        // Obtenemos el ID del usuario de forma segura desde LoginStorage
        int usuarioId = LoginStorage.getUserId(getContext());
        if (usuarioId == 0) {
            // Si no hay usuario, no hay nada que cargar
            if (isAdded()) txtTituloBottomSheet.setText("Inicia sesión para ver tus reservas");
            return;
        }

        binding.swipeRefreshLayoutViajes.setRefreshing(true);

        Call<ViajeListadoResponse> call = apiService.listarReservas(usuarioId);
        call.enqueue(new Callback<ViajeListadoResponse>() {
            @Override
            public void onResponse(@NonNull Call<ViajeListadoResponse> call, @NonNull Response<ViajeListadoResponse> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null) {
                    // CASO DE ÉXITO...
                    reservaList.clear();
                    reservaList.addAll(Arrays.asList(response.body().getData()));
                    reservaAdapter.notifyDataSetChanged();
                    txtTituloBottomSheet.setText(reservaList.isEmpty() ? "No tienes viajes reservados" : "Mis Viajes Reservados");
                } else if (isAdded()) {
                    // --- MANEJO DE ERROR DEFINITIVO ---
                    String errorMensaje = "Ocurrió un error desconocido.";
                    if (response.errorBody() != null) {
                        try {
                            // Leemos el cuerpo del error UNA SOLA VEZ.
                            String errorBodyString = response.errorBody().string();
                            try {
                                // Intentamos interpretarlo como JSON.
                                JSONObject jsonError = new JSONObject(errorBodyString);
                                errorMensaje = jsonError.getString("message");
                            } catch (JSONException e) {
                                // Si falla, el error era texto plano. Lo usamos directamente.
                                errorMensaje = errorBodyString;
                            }
                        } catch (IOException e) {
                            errorMensaje = "Error al leer la respuesta del servidor.";
                        }
                    } else {
                        errorMensaje = "Respuesta no exitosa. Código: " + response.code();
                    }
                    Helper.mensajeError(getContext(), "Error de Reservas", errorMensaje);
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

    @Override
    public void onCancelReservaClick(int viajeId) {
        Toast.makeText(getContext(), "Cancelando reserva para el viaje ID: " + viajeId, Toast.LENGTH_SHORT).show();
        // Aquí iría la llamada a la API para cancelar la reserva.
        // Al completarse, deberías llamar a cargarViajesReservados() para refrescar la lista.
    }

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
                    viajeAdapter.notifyDataSetChanged();
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
