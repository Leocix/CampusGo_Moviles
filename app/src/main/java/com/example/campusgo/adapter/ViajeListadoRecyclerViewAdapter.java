package com.example.campusgo.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusgo.ViajesFragment;
import com.example.campusgo.model.ViajeListadoData;
import com.example.campusgo.util.Helper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.example.campusgo.R;

import java.util.List;

public class ViajeListadoRecyclerViewAdapter extends RecyclerView.Adapter<ViajeListadoRecyclerViewAdapter.ViewHolder>{
    private Context context;
    private List<ViajeListadoData> list;

    public ViajeListadoRecyclerViewAdapter(Context context, List<ViajeListadoData> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Permite enlazar el adapter con el archivo xml que contiene el cardview
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_viaje, parent, false);
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


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        //Declarar y enlazar los controles del cardview
        TextView txtDestino, txtPuntoPartida, txtFechaHoraSalida, txtAsientos, txtVehiculo, txtRestricciones;
        Chip chipEstado;
        MaterialButton btnAgregar, btnVerRuta;

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
            btnAgregar = itemView.findViewById(R.id.btnAgregar);
            btnVerRuta = itemView.findViewById(R.id.btnVerRuta);

            //Configurar al cardview para que reconozca el evento clic
            itemView.setOnClickListener(this);
            btnAgregar.setOnClickListener(this);
            btnVerRuta.setOnClickListener(this);

        }


        private void mostrarDatos(ViajeListadoData v){
            txtDestino.setText(v.getDestino());
            chipEstado.setText(v.getEstado());
            txtPuntoPartida.setText(v.getPuntoPartida());
            txtFechaHoraSalida.setText(v.getFechaHoraSalida());
            txtAsientos.setText(v.getAsientosDisponibles() + " / " + v.getAsientosOfertados() + " asientos disponibles");
            txtVehiculo.setText(v.getVehiculo().getMarca() + " " + v.getVehiculo().getModelo()  + " • " + v.getVehiculo().getPlaca() + " • " + v.getVehiculo().getColor());
            txtRestricciones.setText(v.getRestricciones());

            //Setear al botón agregar la presentación inicial
            btnAgregar.setText("Agregar");
            btnAgregar.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.blue_campusgo_1)));
            btnAgregar.setIconResource(R.drawable.calendar_add_on_24px);

            //Setear la presentación del botón agregar, dependiendo si el viaje se encuentra agregado o no en el resumen
            for (int i = 0; i < ViajeListadoData.viajes.size(); i++) {
                if (ViajeListadoData.viajes.get(i).getViajeId() == v.getViajeId()){
                    btnAgregar.setText("Agregado");
                    btnAgregar.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.green)));
                    btnAgregar.setIconResource(R.drawable.calendar_add_on_24px);
                }
            }

        }

        @Override
        public void onClick(View v) {
            int viajeID = list.get(getAbsoluteAdapterPosition()).getViajeId();
            if (v.getId() == R.id.btnAgregar){
                if (btnAgregar.getText().equals("Agregar")){
                    btnAgregar.setText("Agregado");
                    btnAgregar.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.green)));
                    btnAgregar.setIconResource(R.drawable.calendar_add_on_24px);

                    //Agregue el viaje a la lista de viajes del usuario (pendiente)
                    ViajeListadoData viaje = list.get(getAbsoluteAdapterPosition());
                    ViajeListadoData.viajes.add(viaje);

                    ViajesFragment.mostrarViajesAgregados();

                    //Mostrar un mensaje con Toast
                    Toast.makeText(context, "Viaje agregado", Toast.LENGTH_SHORT).show();

                    //Mostrar la lista de viajes
                    //mostrarLista();

                }else{ //Si el texto del botón es "Agregado", al hacerle clic debe mostrar un mensaje para quitar el viaje de la lista
                    Helper.mensajeConfirmacion(context, "Confirme", "¿Desea retirar el viaje de su lista?", "SI", "NO", () -> {
                        for (int i = 0; i<ViajeListadoData.viajes.size(); i++){
                            if (ViajeListadoData.viajes.get(i).getViajeId() == viajeID){
                                ViajeListadoData.viajes.remove(i);
                            }
                        }

                        btnAgregar.setText("Agregar");
                        btnAgregar.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.blue_campusgo_1)));
                        btnAgregar.setIconResource(R.drawable.calendar_add_on_24px);

                        ViajesFragment.mostrarViajesAgregados();

                        //Mostrar un mensaje con Toast
                        Toast.makeText(context, "Se ha retirado el viaje de su lista", Toast.LENGTH_SHORT).show();
                    });

                    //Mostrar la lista de viajes
                    //mostrarLista();
                }

            }else if (v.getId() == R.id.btnVerRuta){
                Toast.makeText(context, "Mostrar la ruta en google maps. Viaje ID: " + viajeID, Toast.LENGTH_SHORT).show();
            }
        }


    }


}
