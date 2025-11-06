package com.example.campusgo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;

import androidx.annotation.NonNull; // Asegúrate de tener esta
import androidx.annotation.Nullable; // Asegúrate de tener esta
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
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

import com.airbnb.lottie.LottieAnimationView;
import com.example.campusgo.adapter.ViajeListadoRecyclerViewAdapter;
import com.example.campusgo.adapter.ViajeListadoRecyclerViewAdapterAgregado;
import com.example.campusgo.databinding.FragmentNavPrincipalUsuarioBinding;
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


// --- CORRECCIÓN 1: Implementa la interfaz del adaptador ---
public class ViajesFragment extends Fragment implements ViajeListadoRecyclerViewAdapter.OnViajeDataChangedListener {

    FragmentViajesBinding binding;
    ViajeListadoRecyclerViewAdapter adapter;

    // --- CORRECCIÓN 2: Haz estas variables NO-ESTÁTICAS ---
    ViajeListadoRecyclerViewAdapterAgregado adapterAgregado;
    List<ViajeListadoData> list = new ArrayList<>();
    BottomSheetBehavior<View> bottomSheetBehavior;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentViajesBinding.inflate(inflater, container, false);

        //Instanciar el adapter
        adapter = new ViajeListadoRecyclerViewAdapter(getContext(), list);
        // --- CORRECCIÓN 3: Conecta el listener ---
        adapter.setOnViajeDataChangedListener(this);

        adapterAgregado = new ViajeListadoRecyclerViewAdapterAgregado(getContext(), ViajeListadoData.viajes);

        //Configurar el swipeRefresLayout
        binding.swipeRefreshLayoutViajes.setColorSchemeResources(R.color.blue_campusgo_2, R.color.orange_red, R.color._light_green);
        binding.swipeRefreshLayoutViajes.setOnRefreshListener(() -> {
            mostarViajes();
        });

        //Configurar el recyclerView
        binding.recyclerViewViajes.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewViajes.setAdapter(adapter);

        binding.recyclerViewViajesAgregado.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewViajesAgregado.setAdapter(adapterAgregado);

        // ... (El resto de tu código de chips y filtros está bien) ...
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
                    && event.getKeyCode() == KeyEvent.ACTION_DOWN;

            if (actionId == EditorInfo.IME_ACTION_SEARCH || isEnter) {
                mostarViajes(); //Call API REST
                ocultarTeclado(v);
                return true;
            }
            return false;
        });
        binding.tilBuscar.setEndIconOnClickListener(v -> {
            binding.txtBuscar.setText("");
            mostarViajes(); //Call API REST
        });
        binding.chipFiltrar.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); //Vibrar al presionar en el chip
            mostarViajes();
        });

        //BottomSheet
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetResumen);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED); //Plegado

        //Implementar el botón confirmar
        binding.btnConfirmarReserva.setOnClickListener(v -> {
            confirmarReserva();
        });

        //Mostrar viajes
        mostarViajes();

        // --- CORRECCIÓN 4: Llama a esto para el estado inicial ---
        mostrarViajesAgregados();

        return binding.getRoot();
    }

    // --- CORRECCIÓN 5: Implementa el método de la interfaz ---
    @Override
    public void onViajeDataChanged() {
        // Este método se llamará DESDE el adaptador
        // ¡Esto soluciona que la lista no se actualice!
        mostrarViajesAgregados();
    }


    private void confirmarReserva() {
        //Validar que exista viajes en el resumen
        if (ViajeListadoData.viajes.isEmpty()) { //
            Toast.makeText(requireContext(), "No ha seleccionado viajes", Toast.LENGTH_SHORT).show();
            return;
        }

        Helper.mensajeConfirmacion(requireContext(), "Confirme", "¿Seguro de registrar la reserva?", "SI RESERVAR", "NO", () -> {

            // --- CORRECCIÓN 6: Arregla el ID de Pasajero y la Fecha ---
            ReservaRequest reservaRequest = new ReservaRequest();

            // NO USES ESTO (falla si la app se reinicia): reservaRequest.setPasajeroId(LoginData.DATOS_SESION_USUARIO.getId());
            reservaRequest.setPasajeroId(LoginStorage.getUserId(getContext())); // <-- USA ESTO

            // El backend espera AAAA-MM-DD
            reservaRequest.setFechaReserva(Helper.formatearDMA_to_AMD(Helper.obtenerFechaActual()));

            if (!binding.txtObservacion.getText().toString().isEmpty()) {
                reservaRequest.setObservacion(binding.txtObservacion.getText().toString());
            } else {
                reservaRequest.setObservacion("Sin observaciones"); // Evita enviar "ok" o null
            }

            //Recopilar los datos de cada uno de los viajes seleccionados
            List<DetalleViajeRequest> detalleViajeRequestList = new ArrayList<>();
            for (ViajeListadoData v : ViajeListadoData.viajes) {
                DetalleViajeRequest detalleViajeRequest = new DetalleViajeRequest(v.getViajeId(), 14);
                detalleViajeRequestList.add(detalleViajeRequest);
            }
            reservaRequest.setDetalleViaje(detalleViajeRequestList);


            //Llamada a la API REST
            ApiService apiService = RetrofitClient.createService();
            Call<ReservaResponse> call = apiService.registrarReserva(reservaRequest);

            call.enqueue(new Callback<ReservaResponse>() {
                @Override
                public void onResponse(Call<ReservaResponse> call, Response<ReservaResponse> response) {
                    if (response.isSuccessful()) {
                        ViajeListadoData.viajes.clear();
                        mostrarViajesAgregados();
                        mostarViajes(); //Refrescar el recyclerView principal

                        binding.txtObservacion.setText("");
                        Toast.makeText(requireContext(), "Reserva registrada satisfactoriamente", Toast.LENGTH_SHORT).show();
                        // mostrarAnimacion(); // Descomenta si tienes este método
                    } else {
                        try {
                            String errorBody = response.errorBody().string();
                            Log.e("ConfirmarReserva", "Error del API (HTTP " + response.code() + "): " + errorBody);
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
                    Log.e("ConfirmarReserva", "Fallo en la llamada (onFailure)", t);
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
        // ... (Tu código de `mostarViajes` está bien) ...
        binding.swipeRefreshLayoutViajes.setRefreshing(true);
        String textoBusqueda = binding.txtBuscar.getText() != null ? binding.txtBuscar.getText().toString().trim() : "";

        String desde = (binding.chipDesde.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) > 0 ? ""
                : Helper.formatearDMA_to_AMD(binding.chipDesde.getText().toString());

        String hasta = (binding.chipHasta.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) > 0 ? ""
                : Helper.formatearDMA_to_AMD(binding.chipHasta.getText().toString());

        Log.e("FECHA DESDE", desde);
        Log.e("FECHA HASTA", hasta);

        boolean asientosDisponibles = (binding.chipAsientosDisponibles.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) <= 0;
        boolean sinRestricciones = (binding.chipSinRestricciones.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) <= 0;

        Log.e("FECHA ASIENTOS DISPONIBLES", asientosDisponibles + "");
        Log.e("FECHA SIN RESTRICCIONES", sinRestricciones + "");


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

    // --- CORRECCIÓN 9: Haz este método NO-ESTÁTICO ---
    public void mostrarViajesAgregados() {
        if (adapterAgregado != null) {
            adapterAgregado.notifyDataSetChanged(); //Refrescar los registros en el recyclerview
        }

        // Controlar el estado del BottomSheet
        if (bottomSheetBehavior != null) {
            if (ViajeListadoData.viajes.isEmpty()) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            } else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        }
    }

    private void mostrarAnimacion() {
        //Configurar la animación de la app
        // (Asegúrate de que 'layoutControlesReserva' y 'layoutAnimacion' existan en tu fragment_viajes.xml)
        // binding.layoutControlesReserva.setVisibility(View.GONE);
        // binding.layoutAnimacion.setVisibility(View.VISIBLE);
        // binding.lottieAnimationView.setAnimation("ok2.json");
        // binding.lottieAnimationView.playAnimation();
        // binding.lottieAnimationView.setRepeatCount(1);

        //Cargar el siguiente activity (login)
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // binding.layoutControlesReserva.setVisibility(View.VISIBLE);
                // binding.layoutAnimacion.setVisibility(View.GONE);
                if (bottomSheetBehavior != null) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED); //Plegado
                }
            }
        }, 3000); //3 segundos (cambiado de 5000)
    }
}