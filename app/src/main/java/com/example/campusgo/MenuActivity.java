package com.example.campusgo;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.campusgo.databinding.ActivityMenuBinding;
import com.example.campusgo.model.LoginData;
import com.example.campusgo.request.LoginRequest;
import com.example.campusgo.response.LoginResponse;
import com.example.campusgo.retrofit.ApiService;
import com.example.campusgo.retrofit.RetrofitClient;
import com.example.campusgo.sharedpreferences.LoginStorage;
import com.example.campusgo.util.Helper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMenuBinding binding;

    // Controles de la cabecera del menú
    CircleImageView profile_image;
    TextView profile_nombre, profile_email, profile_rol;
    MaterialButton profile_btn_salir, profile_btn_estudiante, profile_btn_conductor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMenu.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                 R.id.nav_principal_usuario,R.id.nav_perfil_usuario)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_menu);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Enlazar los controles de la cabecera
        View headerView = navigationView.getHeaderView(0);
        profile_image = headerView.findViewById(R.id.profile_image);
        profile_nombre = headerView.findViewById(R.id.profile_nombre);
        profile_email = headerView.findViewById(R.id.profile_email);
        profile_rol = headerView.findViewById(R.id.profile_rol);
        profile_btn_estudiante = headerView.findViewById(R.id.profile_btn_estudiante);
        profile_btn_conductor = headerView.findViewById(R.id.profile_btn_conductor);
        profile_btn_salir = headerView.findViewById(R.id.profile_btn_salir);

        mostrarDatosUsuario();
        configurarBotonesRol();

        profile_btn_salir.setOnClickListener(v -> salirApp());

        //Imprimir el nombre del usuario en la barra de título de la navegación
        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController navController, @NonNull NavDestination navDestination, @Nullable Bundle bundle) {
                if (navDestination.getId() == R.id.nav_principal_usuario) {
                    binding.appBarMenu.toolbar.setTitle(LoginData.DATOS_SESION_USUARIO.getNombre());

                    //Gestionar el tamaño del texto
                    for (int i = 0; i < binding.appBarMenu.toolbar.getChildCount(); i++) {

                        View itemView = binding.appBarMenu.toolbar.getChildAt(i);
                        if (itemView instanceof  TextView){
                            TextView textView = (TextView) itemView;
                            textView.setTextSize(15);
                        }
                    }
                }
            }
        });
    }

        private void mostrarDatosUsuario () {
            if (LoginData.DATOS_SESION_USUARIO == null) return;

            profile_nombre.setText(LoginData.DATOS_SESION_USUARIO.getNombre());
            profile_email.setText(LoginData.DATOS_SESION_USUARIO.getEmail());

            String rol = LoginData.DATOS_SESION_USUARIO.getRolId() == 1 ? "Estudiante" : "Conductor";
            profile_rol.setText(rol);

            String fotoFileName = LoginData.DATOS_SESION_USUARIO.getFoto();
            Log.d("FOTO_DEBUG", "El valor recibido para la foto es: '" + fotoFileName + "'");

            if (fotoFileName != null && !fotoFileName.isEmpty() && !fotoFileName.equals("x")) {
                String imageUrl = RetrofitClient.BASE_URL + "/usuario/foto/" + LoginData.DATOS_SESION_USUARIO.getId();
                Log.d("FOTO_DEBUG", "Intentando cargar imagen desde URL: " + imageUrl);

                LazyHeaders.Builder headersBuilder = new LazyHeaders.Builder();
                if (RetrofitClient.API_TOKEN != null && !RetrofitClient.API_TOKEN.isEmpty()) {
                    headersBuilder.addHeader("Authorization", "Bearer " + RetrofitClient.API_TOKEN);
                }
                GlideUrl glideUrl = new GlideUrl(imageUrl, headersBuilder.build());

                Glide.with(this)
                        .load(glideUrl)
                        .placeholder(R.drawable.ic_user)
                        .error(R.drawable.ic_user)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                Log.e("FOTO_DEBUG", "Glide falló al cargar la imagen. URL: " + model, e);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                Log.d("FOTO_DEBUG", "Glide cargó la imagen correctamente desde: " + model);
                                return false;
                            }
                        })
                        .into(profile_image);
            } else {
                Log.d("FOTO_DEBUG", "La condición falló. Se usará la imagen por defecto.");
                profile_image.setImageResource(R.drawable.ic_user);
            }
        }

        private void configurarBotonesRol () {
            if (LoginData.DATOS_SESION_USUARIO == null) return;

            int rolIdActual = LoginData.DATOS_SESION_USUARIO.getRolId();
            List<Integer> rolesDisponibles = LoginData.DATOS_SESION_USUARIO.getRoles();

            if (rolIdActual == 1) { // Es Estudiante, configurar botón para Conductor (ID 2)
                profile_btn_estudiante.setVisibility(View.GONE);
                profile_btn_conductor.setVisibility(View.VISIBLE);

                boolean tieneRolConductor = rolesDisponibles != null && rolesDisponibles.contains(2);
                if (tieneRolConductor) {
                    profile_btn_conductor.setText("Cambiar a Conductor");
                    profile_btn_conductor.setOnClickListener(v -> mostrarDialogoCambioDeRol(2));
                } else {
                    profile_btn_conductor.setText("Crear Cuenta Conductor");
                    profile_btn_conductor.setOnClickListener(v -> mostrarDialogoNuevoRol(2));
                }
            } else { // Es Conductor (ID 2), configurar botón para Estudiante (ID 1)
                profile_btn_conductor.setVisibility(View.GONE);
                profile_btn_estudiante.setVisibility(View.VISIBLE);

                boolean tieneRolEstudiante = rolesDisponibles != null && rolesDisponibles.contains(1);
                if (tieneRolEstudiante) {
                    profile_btn_estudiante.setText("Cambiar a Estudiante");
                    profile_btn_estudiante.setOnClickListener(v -> mostrarDialogoCambioDeRol(1));
                } else {
                    profile_btn_estudiante.setText("Crear Cuenta Estudiante");
                    profile_btn_estudiante.setOnClickListener(v -> mostrarDialogoNuevoRol(1));
                }
            }
        }

        private void mostrarDialogoCambioDeRol ( int nuevoRolId){
            String nombreNuevoRol = nuevoRolId == 1 ? "Estudiante" : "Conductor";
            Helper.mensajeConfirmacion(this,
                    "Cambiar a sesión de " + nombreNuevoRol,
                    "¿Quieres iniciar sesión como " + nombreNuevoRol + "?",
                    "SÍ, CAMBIAR", "CANCELAR",
                    () -> cambiarDeRol(nuevoRolId));
        }

        private void cambiarDeRol ( int nuevoRolId){
            if (!LoginStorage.autoLogin(this)) {
                Toast.makeText(this, "Esta función requiere tener la sesión guardada.", Toast.LENGTH_LONG).show();
                return;
            }

            String[] credentials = LoginStorage.getCredentials(this);
            String email = credentials[0];
            String clave = credentials[1];

            ApiService apiService = RetrofitClient.createService();
            Call<LoginResponse> call = apiService.login(new LoginRequest(email, clave, nuevoRolId));

            call.enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    if (response.isSuccessful()) {
                        LoginData data = response.body().getData();
                        RetrofitClient.API_TOKEN = data.getToken();
                        LoginData.DATOS_SESION_USUARIO = data;

                        // Reiniciar la actividad para refrescar toda la UI con el nuevo rol
                        Intent intent = new Intent(MenuActivity.this, MenuActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Helper.mensajeError(MenuActivity.this, "Error al cambiar de rol", "No se pudo iniciar la nueva sesión.");
                    }
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    Helper.mensajeError(MenuActivity.this, "Error de conexión", t.getMessage());
                }
            });
        }

        private void mostrarDialogoNuevoRol ( int nuevoRolId){
            String nombreNuevoRol = nuevoRolId == 1 ? "Estudiante" : "Conductor";
            Helper.mensajeConfirmacion(this,
                    "Crear Cuenta de " + nombreNuevoRol,
                    "¿Quieres crear tu cuenta de " + nombreNuevoRol + "?",
                    "SÍ, CREAR", "CANCELAR",
                    () -> {
                        Intent intent = new Intent(MenuActivity.this, RegistrarActivity.class);
                        intent.putExtra("NUEVO_ROL_ID", nuevoRolId);
                        intent.putExtra("EMAIL_USUARIO", LoginData.DATOS_SESION_USUARIO.getEmail());
                        startActivity(intent);
                    });
        }

        private void salirApp () {
            Helper.mensajeConfirmacion(this, "Confirme", "¿Desea cerrar su sesión de usuario?", "SI", "NO", () -> {
                LoginStorage.clearCredentials(MenuActivity.this);
                Intent intent = new Intent(MenuActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }

        @Override
        public boolean onCreateOptionsMenu (Menu menu){
            getMenuInflater().inflate(R.menu.menu, menu);
            return true;
        }

        @Override
        public boolean onSupportNavigateUp () {
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_menu);
            return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                    || super.onSupportNavigateUp();
        }
    }
