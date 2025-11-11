package com.example.campusgo.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import com.example.campusgo.R;
import com.example.campusgo.model.PasajeroReservaData;
import com.example.campusgo.model.ViajeListadoData;
import com.example.campusgo.retrofit.RetrofitClient;
import com.example.campusgo.util.Helper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.util.Arrays;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

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
    // --- Fin Corrección 1 ---


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

        // --- Controles para las fotos de pasajeros ---
        LinearLayout layoutUsuariosReservado;
        CircleImageView cimUsuariosReservado1;
        CircleImageView cimUsuariosReservado2;
        CircleImageView cimUsuariosReservado3;
        List<CircleImageView> iconosPasajeros;

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

            // --- Enlaces para las fotos ---
            layoutUsuariosReservado = itemView.findViewById(R.id.layoutUsuariosReservado);
            cimUsuariosReservado1 = itemView.findViewById(R.id.cimUsuariosReservado1);
            cimUsuariosReservado2 = itemView.findViewById(R.id.cimUsuariosReservado2);
            cimUsuariosReservado3 = itemView.findViewById(R.id.cimUsuariosReservado3);
            iconosPasajeros = Arrays.asList(cimUsuariosReservado1, cimUsuariosReservado2, cimUsuariosReservado3);

            //Configurar al cardview para que reconozca el evento click
            btnAgregar.setOnClickListener(this);
            btnVerRuta.setOnClickListener(this);
        }

        private void mostrarDatos(ViajeListadoData v) {
            // --- Usa los getters de tu modelo ViajeListadoData ---
            txtDestino.setText(v.getDestino());
            chipEstado.setText(v.getEstado());
            txtPuntoPartida.setText(v.getPuntoPartida());
            txtFechaHoraSalida.setText(v.getFechaHoraSalida());
            txtAsientos.setText(v.getAsientosDisponibles() + " / " + v.getAsientosOfertados() + " asientos disponibles");
            txtVehiculo.setText(v.getVehiculo().getMarca() + " " + v.getVehiculo().getModelo() + " • " + v.getVehiculo().getPlaca());
            txtRestricciones.setText(v.getRestricciones());

            // --- LÓGICA CLAVE DE BOTONES ---
            boolean yaAgregado = false;
            for (ViajeListadoData viajeReservado : ViajeListadoData.viajes) {
                if (viajeReservado.getViajeId() == v.getViajeId()) {
                    yaAgregado = true;
                    break;
                }
            }
            if (yaAgregado) {
                btnAgregar.setText("Agregado");
                btnAgregar.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.green)));
                btnAgregar.setIconResource(R.drawable.ic_check);
            } else {
                btnAgregar.setText("Agregar");
                btnAgregar.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.blue_campusgo_1)));
                btnAgregar.setIconResource(R.drawable.calendar_add_on_24px);
            }

            // --- LÓGICA PARA FOTOS DE PASAJEROS ---
            if (v.getPasajerosReservados() != null && !v.getPasajerosReservados().isEmpty()) {
                layoutUsuariosReservado.setVisibility(View.VISIBLE);
                actualizarIconosPasajeros(v.getPasajerosReservados());
            } else {
                layoutUsuariosReservado.setVisibility(View.GONE);
            }
        }

        private void actualizarIconosPasajeros(List<PasajeroReservaData> pasajeros) {
            for (int i = 0; i < iconosPasajeros.size(); i++) {
                CircleImageView icono = iconosPasajeros.get(i);

                if (i < pasajeros.size()) {
                    icono.setVisibility(View.VISIBLE);
                    PasajeroReservaData pasajero = pasajeros.get(i);

                    if (pasajero.getFoto() != null && !pasajero.getFoto().isEmpty() && !pasajero.getFoto().equals("x")) {
                        String imageUrl = RetrofitClient.BASE_URL + "/usuario/foto/" + pasajero.getId();

                        LazyHeaders.Builder headersBuilder = new LazyHeaders.Builder()
                                .addHeader("Authorization", "Bearer " + RetrofitClient.API_TOKEN);
                        GlideUrl glideUrl = new GlideUrl(imageUrl, headersBuilder.build());

                        Glide.with(context)
                                .load(glideUrl)
                                .placeholder(R.drawable.ic_user)
                                .error(R.drawable.ic_user)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .into(icono);
                    } else {
                        icono.setImageResource(R.drawable.ic_user);
                    }
                } else {
                    icono.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public void onClick(View v) {
            if (getAbsoluteAdapterPosition() == RecyclerView.NO_POSITION) {
                return;
            }
            int viajeID = list.get(getAbsoluteAdapterPosition()).getViajeId();

            if (v.getId() == R.id.btnAgregar) {

                // --- CORRECCIÓN 3: Llama al listener ---
                if (btnAgregar.getText().equals("Agregar")) {
                    btnAgregar.setText("Agregado");
                    btnAgregar.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.green)));
                    btnAgregar.setIconResource(R.drawable.ic_check);

                    ViajeListadoData viaje = list.get(getAbsoluteAdapterPosition());
                    ViajeListadoData.viajes.add(viaje);
                    Toast.makeText(context, "Viaje Agregado", Toast.LENGTH_SHORT).show();

                    // ¡Avisa al Fragment que los datos cambiaron!
                    if (mDataChangedListener != null) {
                        mDataChangedListener.onViajeDataChanged(); // <-- ESTO ARREGLA EL CRASH
                    }

                } else {
                    Helper.mensajeConfirmacion(context, "Confirme", "¿Desea retirar el viaje de su lista?", "Sí", "No", () -> {
                        for (int i = 0; i < ViajeListadoData.viajes.size(); i++) {
                            if (ViajeListadoData.viajes.get(i).getViajeId() == viajeID) {
                                ViajeListadoData.viajes.remove(i);
                                break;
                            }
                        }
                        btnAgregar.setText("Agregar");
                        btnAgregar.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.blue_campusgo_1)));
                        btnAgregar.setIconResource(R.drawable.calendar_add_on_24px);
                        Toast.makeText(context, "Viaje Retirado", Toast.LENGTH_SHORT).show();

                        // ¡Avisa al Fragment que los datos cambiaron!
                        if (mDataChangedListener != null) {
                            mDataChangedListener.onViajeDataChanged();
                        }
                    });
                }

                // mostrarLista(); // Para depuración

            } else if (v.getId() == R.id.btnVerRuta) {
                Toast.makeText(context, "Mostrar la ruta en google maps" + viajeID, Toast.LENGTH_SHORT).show();
            }
        }

        public void mostrarLista() {
            Log.d("VIAJES_LISTA", "--- Contenido de ViajeListadoData.viajes ---");
            for (ViajeListadoData viaje : ViajeListadoData.viajes) {
                Log.d("VIAJES_LISTA", "ID: " + viaje.getViajeId() + ", Destino: " + viaje.getDestino());
            }
        }
    }
}