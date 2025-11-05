package com.example.campusgo.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// import com.example.campusgo.ViajesFragment; // Ya no necesitas esta importación
import com.example.campusgo.model.ViajeListadoData;
import com.example.campusgo.util.Helper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.example.campusgo.R;

import java.util.List;

public class ViajeListadoRecyclerViewAdapter extends RecyclerView.Adapter<ViajeListadoRecyclerViewAdapter.ViewHolder> {
    private Context context;
    private List<ViajeListadoData> list;

    // --- CORRECCIÓN 1: Añade la interfaz y el listener ---
    public interface OnViajeDataChangedListener {
        void onViajeDataChanged();
    }

    private OnViajeDataChangedListener mDataChangedListener;

    public void setOnViajeDataChangedListener(OnViajeDataChangedListener listener) {
        this.mDataChangedListener = listener;
    }
    // --- Fin Corrección 1 ---

    public ViajeListadoRecyclerViewAdapter(Context context, List<ViajeListadoData> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_viaje, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ViajeListadoData v = list.get(position);
        holder.mostrarDatos(v);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // ... (declaraciones de tus vistas) ...
        TextView txtDestino, txtPuntoPartida, txtFechaHoraSalida, txtAsientos, txtVehiculo, txtRestricciones;
        Chip chipEstado;
        MaterialButton btnAgregar, btnVerRuta;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // ... (enlaces de tus vistas) ...
            txtDestino = itemView.findViewById(R.id.txtDestino);
            txtPuntoPartida = itemView.findViewById(R.id.txtPuntoPartida);
            txtFechaHoraSalida = itemView.findViewById(R.id.txtFechaHoraSalida);
            txtAsientos = itemView.findViewById(R.id.txtAsientos);
            txtVehiculo = itemView.findViewById(R.id.txtVehiculo);
            txtRestricciones = itemView.findViewById(R.id.txtRestricciones);
            chipEstado = itemView.findViewById(R.id.chipEstado);
            btnAgregar = itemView.findViewById(R.id.btnAgregar);
            btnVerRuta = itemView.findViewById(R.id.btnVerRuta);

            itemView.setOnClickListener(this);
            btnAgregar.setOnClickListener(this);
            btnVerRuta.setOnClickListener(this);
        }

        // --- CORRECCIÓN 2: Lógica del botón movida a `mostrarDatos` ---
        private void mostrarDatos(ViajeListadoData v) {
            txtDestino.setText(v.getDestino());
            chipEstado.setText(v.getEstado());
            txtPuntoPartida.setText(v.getPunto_partida());
            txtFechaHoraSalida.setText(v.getFecha_hora_salida());
            txtAsientos.setText(v.getAsientos_disponibles() + " / " + v.getAsientos_ofertados() + " asientos disponibles");
            txtVehiculo.setText(v.getVehiculo().getMarca() + " " + v.getVehiculo().getModelo() + " • " + v.getVehiculo().getPlaca());
            txtRestricciones.setText(v.getRestricciones());

            // --- LÓGICA CLAVE ---
            // Comprueba el estado CADA VEZ que la vista se dibuja

            boolean yaAgregado = false;
            // Busca si este viaje (v) existe en la lista estática de reservas
            for (ViajeListadoData viajeReservado : ViajeListadoData.viajes) {
                if (viajeReservado.getViaje_id() == v.getViaje_id()) {
                    yaAgregado = true;
                    break;
                }
            }

            // Ahora, dibuja el botón según el estado
            if (yaAgregado) {
                btnAgregar.setText("Agregado");
                btnAgregar.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.green)));
                btnAgregar.setIconResource(R.drawable.ic_check);
            } else {
                btnAgregar.setText("Agregar");
                // Asegúrate de que R.color.blue_campusgo_1 exista, si no, usa R.color.blue_campusgo_3
                btnAgregar.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.blue_campusgo_1)));
                btnAgregar.setIconResource(R.drawable.calendar_add_on_24px);
            }
            // --- FIN LÓGICA CLAVE ---
        }

        @Override
        public void onClick(View v) {

            int viajeID = list.get(getAbsoluteAdapterPosition()).getViaje_id();
            if (v.getId() == R.id.btnAgregar) {

                // --- CORRECCIÓN 3: Llama al listener en lugar de al fragment ---
                if (btnAgregar.getText().equals("Agregar")) {
                    btnAgregar.setText("Agregado");
                    btnAgregar.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.green)));
                    btnAgregar.setIconResource(R.drawable.ic_check);

                    ViajeListadoData viaje = list.get(getAbsoluteAdapterPosition());
                    ViajeListadoData.viajes.add(viaje);
                    Toast.makeText(context, "Viaje Agregado", Toast.LENGTH_SHORT).show();

                    if (mDataChangedListener != null) {
                        mDataChangedListener.onViajeDataChanged(); // <-- AVISA AL FRAGMENT
                    }

                } else {
                    Helper.mensajeConfirmacion(context, "Confirme", "¿Desea retirar el viaje de su lista?", "Sí", "No", () -> {
                        for (int i = 0; i < ViajeListadoData.viajes.size(); i++) {
                            if (ViajeListadoData.viajes.get(i).getViaje_id() == viajeID) {
                                ViajeListadoData.viajes.remove(i);
                                break; // Importante
                            }
                        }
                        btnAgregar.setText("Agregar");
                        btnAgregar.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.blue_campusgo_1)));
                        btnAgregar.setIconResource(R.drawable.calendar_add_on_24px);
                        Toast.makeText(context, "Viaje Retirado", Toast.LENGTH_SHORT).show();

                        if (mDataChangedListener != null) {
                            mDataChangedListener.onViajeDataChanged(); // <-- AVISA AL FRAGMENT
                        }
                    });
                }

                // (La llamada a mostrarLista() puede quedarse para depuración)
                //mostrarLista();

            } else if (v.getId() == R.id.btnVerRuta) {
                Toast.makeText(context, "Mostrar la ruta en google maps" + viajeID, Toast.LENGTH_SHORT).show();
            }
        }

        // ... (mostrarLista) ...
    }
}