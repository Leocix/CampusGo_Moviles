package com.example.campusgo;

import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;

import androidx.annotation.NonNull; // Asegúrate de tener esta
import androidx.annotation.Nullable; // Asegúrate de tener esta
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.example.campusgo.adapter.ViajeListadoRecyclerViewAdapter;
import com.example.campusgo.adapter.ViajeListadoRecyclerViewAdapterAgregado;
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
import com.example.campusgo.sharedpreferences.LoginStorage; // <-- IMPORTANTE
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


public class ViajesFragment extends Fragment implements ViajeListadoRecyclerViewAdapter.OnViajeDataChangedListener {

    FragmentViajesBinding binding;
    ViajeListadoRecyclerViewAdapter adapter;
    ViajeListadoRecyclerViewAdapterAgregado adapterAgregado;
    List<ViajeListadoData> list = new ArrayList<>();
    BottomSheetBehavior<View> bottomSheetBehavior;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentViajesBinding.inflate(inflater, container, false);

        adapter = new ViajeListadoRecyclerViewAdapter(getContext(), list);
        adapter.setOnViajeDataChangedListener(this);

        adapterAgregado = new ViajeListadoRecyclerViewAdapterAgregado(getContext(), ViajeListadoData.viajes);

        binding.swipeRefreshLayoutViajes.setColorSchemeResources(R.color.blue_campusgo_2, R.color.orange_red, R.color._light_green);
        binding.swipeRefreshLayoutViajes.setOnRefreshListener(this::mostarViajes);

        binding.recyclerViewViajes.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewViajes.setAdapter(adapter);

        binding.recyclerViewViajesAgregado.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewViajesAgregado.setAdapter(adapterAgregado);

        // --- ¡CORRECCIÓN FINAL! ---
        binding.fabAgregarViaje.setOnClickListener(v -> {
            // Usamos la nueva acción que parte desde PrincipalUsuarioFragment
            NavHostFragment.findNavController(this).navigate(R.id.action_principal_to_agregarViaje);
        });

        binding.chipDesde.setCheckable(false);
        binding.chipHasta.setCheckable(false);
        binding.chipAsientosDisponibles.setCheckable(false);
        binding.chipSinRestricciones.setCheckable(false);
        binding.chipDesde.setText(Helper.obtenerFechaActual());
        binding.chipHasta.setText(Helper.obtenerFechaActual());
        gestionarTachadoX(binding.chipDesde);
        gestionarTachadoX(binding.chipHasta);
        gestionarTachadoX(binding.chipAsientosDisponibles);
        gestionarTachadoX(binding.chipSinRestricciones);
        binding.chipSinRestricciones.setPaintFlags(binding.chipSinRestricciones.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        gestionarFechaChip(binding.chipDesde);
        gestionarFechaChip(binding.chipHasta);
        binding.txtBuscar.setOnEditorActionListener((v, actionId, event) -> {
            boolean isEnter = event != null
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN;

            if (actionId == EditorInfo.IME_ACTION_SEARCH || isEnter) {
                mostarViajes();
                ocultarTeclado(v);
                return true;
            }
            return false;
        });
        binding.tilBuscar.setEndIconOnClickListener(v -> {
            binding.txtBuscar.setText("");
            mostarViajes();
        });
        binding.chipFiltrar.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            mostarViajes();
        });

        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetResumen);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        binding.btnConfirmarReserva.setOnClickListener(v -> {
            confirmarReserva();
        });

        mostarViajes();
        mostrarViajesAgregados();

        return binding.getRoot();
    }

    @Override
    public void onViajeDataChanged() {
        mostrarViajesAgregados();
    }

    private void confirmarReserva() {
        if (ViajeListadoData.viajes.isEmpty()) {
            Toast.makeText(requireContext(), "No ha seleccionado viajes", Toast.LENGTH_SHORT).show();
            return;
        }

        Helper.mensajeConfirmacion(requireContext(), "Confirme", "¿Seguro de registrar la reserva?", "SI RESERVAR", "NO", () -> {
            ReservaRequest reservaRequest = new ReservaRequest();
            reservaRequest.setPasajeroId(LoginStorage.getUserId(getContext()));
            reservaRequest.setFechaReserva(Helper.formatearDMA_to_AMD(Helper.obtenerFechaActual()));

            if (!binding.txtObservacion.getText().toString().isEmpty()) {
                reservaRequest.setObservacion(binding.txtObservacion.getText().toString());
            } else {
                reservaRequest.setObservacion("Sin observaciones");
            }

            List<DetalleViajeRequest> detalleViajeRequestList = new ArrayList<>();
            for (ViajeListadoData v : ViajeListadoData.viajes) {
                DetalleViajeRequest detalleViajeRequest = new DetalleViajeRequest(v.getViajeId(), 14);
                detalleViajeRequestList.add(detalleViajeRequest);
            }
            reservaRequest.setDetalleViaje(detalleViajeRequestList);

            ApiService apiService = RetrofitClient.createService();
            Call<ReservaResponse> call = apiService.registrarReserva(reservaRequest);

            call.enqueue(new Callback<ReservaResponse>() {
                @Override
                public void onResponse(Call<ReservaResponse> call, Response<ReservaResponse> response) {
                    if (response.isSuccessful()) {
                        ViajeListadoData.viajes.clear();
                        mostrarViajesAgregados();
                        mostarViajes();

                        binding.txtObservacion.setText("");
                        Toast.makeText(requireContext(), "Reserva registrada satisfactoriamente", Toast.LENGTH_SHORT).show();
                    } else {
                        try {
                            String errorBody = response.errorBody().string();
                            JSONObject jsonError = new JSONObject(errorBody);
                            String error = jsonError.getString("message");
                            Helper.mensajeError(getContext(), "Error al registrar la reserva", error);
                        } catch (IOException | JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

                @Override
                public void onFailure(Call<ReservaResponse> call, Throwable t) {
                    Helper.mensajeError(getContext(), "Error al acceder al servicio de registro de reservas", t.getMessage());
                }
            });
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
            final int STRIKE_FLAG = Paint.STRIKE_THRU_TEXT_FLAG;

            if ((currentFlags & STRIKE_FLAG) > 0) {
                chip.setPaintFlags(currentFlags & ~STRIKE_FLAG);
            } else {
                Pickers.obtenerFecha(requireContext(), chip, "posterior");
            }
        });
    }

    private void gestionarTachadoX(Chip chip) {
        chip.setOnCloseIconClickListener(v -> {
            int currentFlags = chip.getPaintFlags();
            final int STRIKE_FLAG = Paint.STRIKE_THRU_TEXT_FLAG;

            if ((currentFlags & STRIKE_FLAG) > 0) {
                chip.setPaintFlags(currentFlags & ~STRIKE_FLAG);
            } else {
                chip.setPaintFlags(currentFlags | STRIKE_FLAG);
            }
        });
    }

    private void mostarViajes() {
        binding.swipeRefreshLayoutViajes.setRefreshing(true);
        String textoBusqueda = binding.txtBuscar.getText() != null ? binding.txtBuscar.getText().toString().trim() : "";

        String desde = (binding.chipDesde.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) > 0 ? ""
                : Helper.formatearDMA_to_AMD(binding.chipDesde.getText().toString());

        String hasta = (binding.chipHasta.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) > 0 ? ""
                : Helper.formatearDMA_to_AMD(binding.chipHasta.getText().toString());

        boolean asientosDisponibles = (binding.chipAsientosDisponibles.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) <= 0;
        boolean sinRestricciones = (binding.chipSinRestricciones.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) <= 0;

        ApiService apiService = RetrofitClient.createService();
        Call<ViajeListadoResponse> call = apiService.listarViajes(new ViajeListadoRequest("destino", textoBusqueda, asientosDisponibles, sinRestricciones, desde, hasta));
        call.enqueue(new Callback<ViajeListadoResponse>() {
            @Override
            public void onResponse(Call<ViajeListadoResponse> call, Response<ViajeListadoResponse> response) {
                if (response.isSuccessful()) {
                    list.clear();
                    list.addAll(Arrays.asList(response.body().getData()));
                    adapter.notifyDataSetChanged();
                    binding.swipeRefreshLayoutViajes.setRefreshing(false);
                } else {
                    try {
                        JSONObject jsonError = new JSONObject(response.errorBody().string());
                        String error = jsonError.getString("message");
                        Helper.mensajeError(getContext(), "Error al acceder al listado de viajes", error);
                    } catch (IOException | JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            @Override
            public void onFailure(Call<ViajeListadoResponse> call, Throwable t) {
                Helper.mensajeError(getContext(), "Error al acceder al servicio de listado de viajes", t.getMessage());
            }
        });
    }

    public void mostrarViajesAgregados() {
        if (adapterAgregado != null) {
            adapterAgregado.notifyDataSetChanged();
        }

        if (bottomSheetBehavior != null) {
            if (ViajeListadoData.viajes.isEmpty()) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            } else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        }
    }
}