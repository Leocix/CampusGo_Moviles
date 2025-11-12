package com.example.campusgo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.campusgo.model.VehiculoData;
import com.example.campusgo.request.ViajeRequest;
import com.example.campusgo.response.ViajeResponse;
import com.example.campusgo.retrofit.ApiService;
import com.example.campusgo.retrofit.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AgregarViajesViewModel extends ViewModel {

    private final ApiService apiService;

    private final MutableLiveData<List<VehiculoData>> _vehiculos = new MutableLiveData<>();
    public LiveData<List<VehiculoData>> vehiculos = _vehiculos;

    private final MutableLiveData<UiState> _uiState = new MutableLiveData<>(UiState.IDLE);
    public LiveData<UiState> uiState = _uiState;

    private final MutableLiveData<Event<String>> _toastMessage = new MutableLiveData<>();
    public LiveData<Event<String>> toastMessage = _toastMessage;

    private final MutableLiveData<Event<Boolean>> _navigationEvent = new MutableLiveData<>();
    public LiveData<Event<Boolean>> navigationEvent = _navigationEvent;


    public AgregarViajesViewModel() {
        apiService = RetrofitClient.createService();
    }

    public void cargarVehiculos(int conductorId) {
        _uiState.setValue(UiState.LOADING);
        apiService.listarVehiculosPorConductor(conductorId).enqueue(new Callback<List<VehiculoData>>() {
            @Override
            public void onResponse(Call<List<VehiculoData>> call, Response<List<VehiculoData>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    _vehiculos.setValue(response.body());
                    _uiState.setValue(UiState.IDLE);
                } else {
                    _toastMessage.setValue(new Event<>("No tienes vehículos registrados."));
                    _uiState.setValue(UiState.ERROR);
                }
            }

            @Override
            public void onFailure(Call<List<VehiculoData>> call, Throwable t) {
                _toastMessage.setValue(new Event<>("Error de red: " + t.getMessage()));
                _uiState.setValue(UiState.ERROR);
            }
        });
    }

    public void publicarViaje(ViajeRequest request) {
        _uiState.setValue(UiState.LOADING);
        apiService.registrarViaje(request).enqueue(new Callback<ViajeResponse>() {
            @Override
            public void onResponse(Call<ViajeResponse> call, Response<ViajeResponse> response) {
                if (response.isSuccessful()) {
                    _toastMessage.setValue(new Event<>("¡Viaje publicado exitosamente!"));
                    _navigationEvent.setValue(new Event<>(true));
                } else {
                    _toastMessage.setValue(new Event<>("Error al publicar el viaje."));
                }
                _uiState.setValue(UiState.IDLE);
            }

            @Override
            public void onFailure(Call<ViajeResponse> call, Throwable t) {
                _toastMessage.setValue(new Event<>("Error de conexión: " + t.getMessage()));
                _uiState.setValue(UiState.IDLE);
            }
        });
    }

    public enum UiState { IDLE, LOADING, ERROR }

    public static class Event<T> {
        private T content;
        private boolean hasBeenHandled = false;

        public Event(T content) { this.content = content; }

        public T getContentIfNotHandled() {
            if (hasBeenHandled) {
                return null;
            } else {
                hasBeenHandled = true;
                return content;
            }
        }
    }
}
