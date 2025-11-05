package com.example.campusgo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusgo.R;
import com.example.campusgo.model.ViajeListadoData;
import com.example.campusgo.util.Helper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.util.List;

public class ReservaListadoRecyclerViewAdapter extends RecyclerView.Adapter<ReservaListadoRecyclerViewAdapter.ViewHolder> {

    private Context context;
    private List<ViajeListadoData> list;

    // Interface para comunicar el clic de 'cancelar' de vuelta al Fragment
    public interface OnReservaCancelListener {
        void onCancelReservaClick(int viajeId);
    }

    private OnReservaCancelListener mListener;


    public ReservaListadoRecyclerViewAdapter(Context context, List<ViajeListadoData> list, OnReservaCancelListener listener) {
        this.context = context;
        this.list = list;
        this.mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Enlazamos con el nuevo layout de tarjeta que creamos
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_reserva, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Obtenemos los datos del viaje reservado
        ViajeListadoData v = list.get(position);
        holder.mostrarDatos(v);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        // Controles del cardview_reserva.xml
        TextView txtDestino, txtPuntoPartida, txtFechaHoraSalida, txtVehiculo;
        Chip chipEstado;
        MaterialButton btnCancelarReserva;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Enlazamos los controles
            txtDestino = itemView.findViewById(R.id.txtDestino);
            txtPuntoPartida = itemView.findViewById(R.id.txtPuntoPartida);
            txtFechaHoraSalida = itemView.findViewById(R.id.txtFechaHoraSalida);
            txtVehiculo = itemView.findViewById(R.id.txtVehiculo);
            chipEstado = itemView.findViewById(R.id.chipEstado);
            btnCancelarReserva = itemView.findViewById(R.id.btnCancelarReserva);

            // Configuramos el clic para el botón de cancelar
            btnCancelarReserva.setOnClickListener(v -> {
                int position = getAbsoluteAdapterPosition();
                if (position != RecyclerView.NO_POSITION && mListener != null) {
                    int viajeId = list.get(position).getViaje_id();

                    // Usamos un diálogo de confirmación para seguridad
                    Helper.mensajeConfirmacion(context,
                            "Confirmar Cancelación",
                            "¿Estás seguro de que quieres cancelar esta reserva?",
                            "SÍ, CANCELAR",
                            "NO",
                            () -> mListener.onCancelReservaClick(viajeId)
                    );
                }
            });
        }

        public void mostrarDatos(ViajeListadoData v) {
            txtDestino.setText(v.getDestino());
            chipEstado.setText("RESERVADO"); // Siempre será "RESERVADO" en esta lista
            txtPuntoPartida.setText(v.getPunto_partida());
            txtFechaHoraSalida.setText(v.getFecha_hora_salida());

            // Comprobamos si el vehículo existe para evitar errores
            if (v.getVehiculo() != null) {
                txtVehiculo.setText(v.getVehiculo().getMarca() + " " + v.getVehiculo().getModelo() + " • " + v.getVehiculo().getPlaca());
            } else {
                txtVehiculo.setText("Vehículo no asignado");
            }
        }
    }
}