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
import android.widget.TextView; // Asegúrate de que TextView esté importado
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusgo.adapter.ReservaListadoRecyclerViewAdapter;
import com.example.campusgo.adapter.ViajeListadoRecyclerViewAdapter;
import com.example.campusgo.databinding.FragmentViajesBinding;
import com.example.campusgo.model.LoginData; // Importa LoginData
import com.example.campusgo.model.ViajeListadoData;
import com.example.campusgo.request.DetalleViajeRequest; // Importa las clases Request
import com.example.campusgo.request.ReservaRequest;
import com.example.campusgo.request.ViajeListadoRequest;
import com.example.campusgo.response.ReservaResponse; // Importa ReservaResponse
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

// --- CORRECCIÓN 1: Implementa AMBAS interfaces ---
public class ViajesFragment extends Fragment implements
        ReservaListadoRecyclerViewAdapter.OnReservaCancelListener,
        ViajeListadoRecyclerViewAdapter.OnViajeDataChangedListener {

    FragmentViajesBinding binding;
    ApiService apiService;

    // --- Lista Principal (Viajes Disponibles) ---
    ViajeListadoRecyclerViewAdapter viajeAdapter;
    List<ViajeListadoData> viajeList = new ArrayList<>();

    // --- BottomSheet (Mis Reservas/Resumen) ---
    ReservaListadoRecyclerViewAdapter reservaAdapter;
    List<ViajeListadoData> reservaList = new ArrayList<>(); // Lista de datos para el BottomSheet
    BottomSheetBehavior<View> bottomSheetBehavior;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentViajesBinding.inflate(inflater, container, false);
        apiService = RetrofitClient.createService();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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

        // --- Configuración BottomSheet (Reservas) ---
        setupBottomSheet();

        //Implementar el boton confirmar
        binding.btnConfirmarReserva.setOnClickListener(v -> {
            confirmarReserva();
        });

        // --- Carga Inicial de Datos ---
        mostrarViajes();
        actualizarResumenReservas(); // <-- Carga el estado inicial del BottomSheet
    }

    // --- CORRECCIÓN 3: Método de la interfaz "OnViajeDataChangedListener" ---
    @Override
    public void onViajeDataChanged() {
        // Esto se llama CADA VEZ que agregas o quitas un viaje
        // ¡Esto soluciona tu problema de que la reserva no aparece!
        actualizarResumenReservas();
    }

    // --- CORRECCIÓN 4: Método de la interfaz "OnReservaCancelListener" ---
    @Override
    public void onCancelReservaClick(int viajeId) {
        // 1. Elimina el viaje de la lista de datos central
        java.util.Iterator<ViajeListadoData> iterator = ViajeListadoData.viajes.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getViaje_id() == viajeId) {
                iterator.remove();
                break;
            }
        }

        // 2. Refresca el BottomSheet (para quitar el item)
        actualizarResumenReservas();

        // 3. Refresca la lista principal (para resetear el botón "Agregar")
        if (viajeAdapter != null) {
            viajeAdapter.notifyDataSetChanged();
        }
        Toast.makeText(getContext(), "Reserva quitada", Toast.LENGTH_SHORT).show();
    }

    // --- CORRECCIÓN 5: `setupBottomSheet` usa los IDs correctos ---
    private void setupBottomSheet() {
        // 1. Usa el ID del LinearLayout: "bottomSheetResumen"
        View bottomSheetView = binding.bottomSheetResumen; // <-- Sin .getRoot()

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        // 2. Usa el ID del RecyclerView: "recyclerViewViajesAgregado"
        RecyclerView recyclerViewReservas = binding.recyclerViewViajesAgregado;

        // 3. Configura el adaptador para el RecyclerView del BottomSheet
        reservaAdapter = new ReservaListadoRecyclerViewAdapter(getContext(), reservaList, this);

        if (recyclerViewReservas != null) {
            recyclerViewReservas.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerViewReservas.setAdapter(reservaAdapter);
        }
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
            reservaRequest.setObservacion(binding.txtObservacion.getText() != null ? binding.txtObservacion.getText().toString():"");

            List<DetalleViajeRequest> detalleViajeRequestsList = new ArrayList<>();
            for (ViajeListadoData v : ViajeListadoData.viajes) {
                DetalleViajeRequest detalleViajeRequest = new DetalleViajeRequest(v.getViaje_id(), 14 ); // 14 = Reservado
                detalleViajeRequestsList.add(detalleViajeRequest);
            }
            reservaRequest.setDetalleViaje(detalleViajeRequestsList);

            ApiService apiService = RetrofitClient.createService();

            // --- ¡IMPORTANTE! Asegúrate de tener este endpoint en ApiService.java ---
            // Call<ReservaResponse> registrarReserva(@Body ReservaRequest request);
            Call<ReservaResponse> call = apiService.registrarReserva(reservaRequest);

            call.enqueue(new Callback<ReservaResponse>() {
                @Override
                public void onResponse(Call<ReservaResponse> call, Response<ReservaResponse> response) {
                    if(response.isSuccessful()){
                        ViajeListadoData.viajes.clear(); // Limpia la lista estática

                        // --- CORRECCIÓN 6: Sincroniza ambos adaptadores ---
                        actualizarResumenReservas(); // Oculta el BottomSheet y limpia su lista
                        viajeAdapter.notifyDataSetChanged(); // Resetea los botones "Agregar"

                        binding.txtObservacion.setText("");
                        Toast.makeText(requireContext(),"Reserva registrada satisfactoriamente",Toast.LENGTH_SHORT).show();

                        // Opcional: Vuelve a cargar los viajes para actualizar asientos disponibles
                        // mostrarViajes();
                    } else {
                        Toast.makeText(requireContext(),"Error al registrar la reserva",Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ReservaResponse> call, Throwable t) {
                    Toast.makeText(getContext(), "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    // --- CORRECCIÓN 7: Método para actualizar el BottomSheet (la lista de resumen) ---
    private void actualizarResumenReservas() {
        if (!isAdded() || reservaAdapter == null) return; // Comprobación de seguridad

        // 1. Limpia la lista actual del BottomSheet
        reservaList.clear();

        // 2. Carga todos los datos desde la lista estática
        reservaList.addAll(ViajeListadoData.viajes);

        // 3. Notifica al adaptador del BottomSheet
        reservaAdapter.notifyDataSetChanged();

        // 4. Controla la visibilidad y estado del BottomSheet
        if (ViajeListadoData.viajes.isEmpty()) {
            // Si no hay viajes, colapsa
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            // Si hay viajes, muéstralo expandido
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
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
                    viajeAdapter.notifyDataSetChanged(); // Esto actualizará los botones "Agregar"
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