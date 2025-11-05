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

import com.example.campusgo.model.ViajeListadoData;
import com.example.campusgo.util.Helper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.example.campusgo.R;

import java.util.List;

public class ViajeListadoRecyclerViewAdapter extends RecyclerView.Adapter<ViajeListadoRecyclerViewAdapter.ViewHolder> {
    private Context context;
    private List<ViajeListadoData> list;

    public interface OnViajeDataChangedListener {
        void onViajeDataChanged();
    }

    private OnViajeDataChangedListener mDataChangedListener;

    public void setOnViajeDataChangedListener(OnViajeDataChangedListener listener) {
        this.mDataChangedListener = listener;
    }

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
        //Define la cantidad de registros a mostrar en el recyclerview
        return list.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        //Enlazar y declarar los controles del cardview
        TextView txtDestino, txtPuntoPartida, txtFechaHoraSalida, txtAsientos, txtVehiculo, txtRestricciones;
        Chip chipEstado;
        MaterialButton btnAgregar, btnVerRuta;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            //Enlazar controles
            txtDestino = itemView.findViewById(R.id.txtDestino);
            txtPuntoPartida = itemView.findViewById(R.id.txtPuntoPartida);
            txtFechaHoraSalida = itemView.findViewById(R.id.txtFechaHoraSalida);
            txtAsientos = itemView.findViewById(R.id.txtAsientos);
            txtVehiculo = itemView.findViewById(R.id.txtVehiculo);
            txtRestricciones = itemView.findViewById(R.id.txtRestricciones);
            chipEstado = itemView.findViewById(R.id.chipEstado);
            btnAgregar = itemView.findViewById(R.id.btnAgregar);
            btnVerRuta = itemView.findViewById(R.id.btnVerRuta);

            //Configurar al cardview para que reconozca el evento click
            itemView.setOnClickListener(this);
            btnAgregar.setOnClickListener(this);
            btnVerRuta.setOnClickListener(this);

        }

        private void mostrarDatos(ViajeListadoData v) {
            txtDestino.setText(v.getDestino());
            chipEstado.setText(v.getEstado());
            txtPuntoPartida.setText(v.getPunto_partida());
            txtFechaHoraSalida.setText(v.getFecha_hora_salida());
            txtAsientos.setText(v.getAsientos_disponibles() + " / " + v.getAsientos_ofertados() + " asientos disponibles");
            txtVehiculo.setText(v.getVehiculo().getMarca() + " " + v.getVehiculo().getModelo() + " • " + v.getVehiculo().getPlaca());
            txtRestricciones.setText(v.getRestricciones());

            // --- INICIO DE LA LÓGICA QUE FALTABA ---
            // Comprobamos el estado CADA VEZ que la vista se dibuja

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
                btnAgregar.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.blue_campusgo_1)));
                btnAgregar.setIconResource(R.drawable.calendar_add_on_24px);
            }
            // --- FIN DE LA LÓGICA QUE FALTABA ---
        }

        @Override
        public void onClick(View v) {

            int viajeID = list.get(getAbsoluteAdapterPosition()).getViaje_id();
            if (v.getId() == R.id.btnAgregar) {
                if (btnAgregar.getText().equals("Agregar")) {
                    btnAgregar.setText("Agregado");
                    btnAgregar.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.green)));
                    btnAgregar.setIconResource(R.drawable.ic_check);

                    //Agregue el viaje a la lista de viajes del usuario (pendiente)
                    ViajeListadoData viaje = list.get(getAbsoluteAdapterPosition());
                    ViajeListadoData.viajes.add(viaje);
                    Toast.makeText(context, "Viaje Agregado", Toast.LENGTH_SHORT).show();

                    //Mostrar Lista
                    mostrarLista();
                    // 2. --- AVISA AL FRAGMENT QUE LOS DATOS CAMBIARON ---
                    if (mDataChangedListener != null) {
                        mDataChangedListener.onViajeDataChanged();
                    }
                    // --------------------------------------------------

                } else { //Si el texto del botón es "Agredado", al hacerle clic debe mostrar un mensaje para quitar el viaje
                    Helper.mensajeConfirmacion(context, "Confirme", "¿Desea retirar el viaje de su lista?", "Sí", "No", () -> {
                        for (int i = 0; i < ViajeListadoData.viajes.size(); i++) {
                            if (ViajeListadoData.viajes.get(i).getViaje_id() == viajeID) {
                                ViajeListadoData.viajes.remove(i);
                            }

                        }
                        btnAgregar.setText("Agregar");
                        btnAgregar.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.blue_campusgo_1)));
                        btnAgregar.setIconResource(R.drawable.calendar_add_on_24px);
                        Toast.makeText(context, "Viaje Retirado", Toast.LENGTH_SHORT).show();
                        // 3. --- AVISA AL FRAGMENT QUE LOS DATOS CAMBIARON ---
                        if (mDataChangedListener != null) {
                            mDataChangedListener.onViajeDataChanged();
                        }
                        // --------------------------------------------------

                    });

                    //Mostrar Lista
                    mostrarLista();
                }

            } else if (v.getId() == R.id.btnVerRuta) {
                Toast.makeText(context, "Mostrar la ruta en google maps" + viajeID, Toast.LENGTH_SHORT).show();
            }
        }

        public void mostrarLista() {
            //Verificar su los viajes se han agregado a la lista
            for (int i = 0; i < ViajeListadoData.viajes.size(); i++) {
                Log.e("VIAJES LISTA",
                        "Destino: " + ViajeListadoData.viajes.get(i).getDestino() + "-" +
                                "Horario: " + ViajeListadoData.viajes.get(i).getFecha_hora_salida() + "-" +
                                "ID: " + ViajeListadoData.viajes.get(i).getViaje_id());
            }
        }

    }
}

