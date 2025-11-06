package com.example.campusgo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.campusgo.databinding.ActivityLoginBinding;
import com.example.campusgo.model.LoginData;
import com.example.campusgo.request.LoginRequest;
import com.example.campusgo.response.LoginResponse;
import com.example.campusgo.retrofit.ApiService;
import com.example.campusgo.retrofit.RetrofitClient;
import com.example.campusgo.sharedpreferences.LoginStorage;
import com.example.campusgo.util.Helper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });*/

        //Inflar el layout utilizando binding
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //LLamar al método leerCredencialesAlmacendas()
        leerCredencialesAlmacendas();

        //Implementar el botón login
        binding.btnLogin.setOnClickListener(v -> {
            login();
        });
    }

    private void leerCredencialesAlmacendas(){
        if (LoginStorage.autoLogin(LoginActivity.this)){ //Si se ha encontrado credenciales almacenadas
            String email = LoginStorage.getCredentials(LoginActivity.this)[0]; //0=email
            String clave = LoginStorage.getCredentials(LoginActivity.this)[1]; //1=clave

            //Setear en el layout de credenciales
            binding.txtEmail.setText(email);
            binding.txtPassword.setText(clave);

            //LLamar al botón login
            login();
        }
    }

    private void login() {
        String email = binding.txtEmail.getText().toString();
        String clave = binding.txtPassword.getText().toString();

        if (email.isEmpty()){
            Toast.makeText(this, "Ingrese el email", Toast.LENGTH_SHORT).show();
            return;
        }else if (clave.isEmpty()) {
            Toast.makeText(this, "Ingrese la contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        //Al momento de inicar la validación de credenciales contra el API REST, se muestra el progreso y oculta la layout de credenciales
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.layoutCredenciales.setVisibility(View.GONE);

        //Crear una instanciar del ApiService para llamar al endpoint
        ApiService apiService = RetrofitClient.createService();

        //Realizar la petición(request) al endpoint
        Call<LoginResponse> call = apiService.login(new LoginRequest(email, clave));
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful()){
                    String nombre = response.body().getData().getNombre();
                    int id = response.body().getData().getId();

                    Log.e("LOGIN", "NOMBRE: " + nombre);
                    Log.e("LOGIN", "ID: " + id);

                    //Almacenar el token de la sesión del usuario para que pueda ser utilizado posteriormente
                    RetrofitClient.API_TOKEN = response.body().getData().getToken();

                    //Almacenar los datos de la sesión del usuario para que pueda ser utilizado posteriormente
                    LoginData.DATOS_SESION_USUARIO = response.body().getData();

                    //Almacenas las credenciales del usuario, cuando el check "Recordar sesión" se encuentre activado
                    if (binding.chkRecordarSesion.isChecked()){
                        LoginStorage.saveCredentials(LoginActivity.this, email, clave);
                    }

                    //Llamar al activity MenuActivity
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
                            startActivity(intent);
                            finish(); //Cierra el activity login
                        }
                    }, 1000); //1000=un segundo

                    Toast.makeText(LoginActivity.this, "Login satisfactorio", Toast.LENGTH_SHORT).show();

                }else{ //http status = 400, 401, 500, etc.
                    //Si hay un error en las credenciales, entonces se muestra el layout de credenciales y se oculta la barra de progreso
                    binding.progressBar.setVisibility(View.GONE);
                    binding.layoutCredenciales.setVisibility(View.VISIBLE);

                    try {
                        //Mostrar el error que devuelve el endpoint
                        JSONObject jsonObject = new JSONObject(response.errorBody().string());
                        String message = jsonObject.getString("message");
                        Helper.mensajeError(LoginActivity.this, "Login error", message);

                        //Limpiar las credenciales ingresadas para que el usuario vuelva a ingresar
                        binding.txtEmail.setText("");
                        binding.txtPassword.setText("");
                        binding.txtEmail.requestFocus();
                    }catch (IOException e){
                        throw  new RuntimeException(e);
                    }catch (JSONException e){
                        throw  new RuntimeException(e);
                    }

                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Helper.mensajeError(LoginActivity.this, "Error de conexión al API REST", t.getMessage());
            }
        });
    }

}