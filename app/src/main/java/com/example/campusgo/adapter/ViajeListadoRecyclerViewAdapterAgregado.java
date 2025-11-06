package com.example.campusgo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusgo.R;
import com.example.campusgo.model.ViajeListadoData;
import com.google.android.material.chip.Chip;

import java.util.List;

public class ViajeListadoRecyclerViewAdapterAgregado extends RecyclerView.Adapter<ViajeListadoRecyclerViewAdapterAgregado.ViewHolder>{
    private Context context;
    private List<ViajeListadoData> list;

    public ViajeListadoRecyclerViewAdapterAgregado(Context context, List<ViajeListadoData> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Permite enlazar el adapter con el archivo xml que contiene el cardview
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_reserva, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //Mostrar los datos en el cardview

        //Leer los datos del viaje según la posición del recyclerview
        ViajeListadoData v = list.get(position);

        //Mostrar
        holder.mostrarDatos(v);

    }

    @Override
    public int getItemCount() {
        //Define la cantidad de registros a mostrar en el recyclerView
        return list.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        //Declarar y enlazar los controles del cardview
        TextView txtDestino, txtPuntoPartida, txtFechaHoraSalida, txtAsientos, txtVehiculo, txtRestricciones;
        Chip chipEstado;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            //Enlazar los controles
            txtDestino = itemView.findViewById(R.id.txtDestino);
            txtPuntoPartida = itemView.findViewById(R.id.txtPuntoPartida);
            txtFechaHoraSalida = itemView.findViewById(R.id.txtFechaHoraSalida);
            txtAsientos = itemView.findViewById(R.id.txtAsientos);
            txtVehiculo = itemView.findViewById(R.id.txtVehiculo);
            txtRestricciones = itemView.findViewById(R.id.txtRestricciones);
            chipEstado = itemView.findViewById(R.id.chipEstado);

        }


        private void mostrarDatos(ViajeListadoData v){
            txtDestino.setText(v.getDestino());
            chipEstado.setText(v.getEstado());
            txtPuntoPartida.setText(v.getPuntoPartida());
            txtFechaHoraSalida.setText(v.getFechaHoraSalida());
            txtAsientos.setText(v.getAsientosDisponibles() + " / " + v.getAsientosOfertados() + " asientos disponibles");
            txtVehiculo.setText(v.getVehiculo().getMarca() + " " + v.getVehiculo().getModelo()  + " • " + v.getVehiculo().getPlaca() + " • " + v.getVehiculo().getColor());
            txtRestricciones.setText(v.getRestricciones());
        }
    }


}
