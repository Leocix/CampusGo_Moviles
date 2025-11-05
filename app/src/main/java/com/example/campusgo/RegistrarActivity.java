package com.example.campusgo;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.campusgo.databinding.ActivityRegistrarBinding;
import com.example.campusgo.model.UsuarioData;
import com.example.campusgo.model.VehiculoData;
import com.example.campusgo.response.UsuarioResponse;
import com.example.campusgo.retrofit.ApiService;
import com.example.campusgo.retrofit.RetrofitClient;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistrarActivity extends AppCompatActivity {

    private ActivityRegistrarBinding binding;
    private String rolSeleccionado = "Estudiante"; // Por defecto
    private Uri imageUri = null;
    private boolean isAddingNewRole = false;

    // --- LANZADORES ---
    private final ActivityResultLauncher<Uri> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            result -> {
                if (result) {
                    binding.icFoto.setImageURI(imageUri);
                }
            }
    );

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    imageUri = uri;
                    binding.icFoto.setImageURI(imageUri);
                }
            }
    );

    private final ActivityResultLauncher<String> requestCameraPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) launchCamera();
                else Toast.makeText(this, "Permiso de cámara denegado.", Toast.LENGTH_SHORT).show();
            }
    );

    private final ActivityResultLauncher<String> requestGalleryPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) launchGallery();
                else Toast.makeText(this, "Permiso de galería denegado.", Toast.LENGTH_SHORT).show();
            }
    );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegistrarBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // --- LÓGICA PARA AÑADIR NUEVO ROL ---
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("NUEVO_ROL_ID")) {
            isAddingNewRole = true;
            int nuevoRolId = intent.getIntExtra("NUEVO_ROL_ID", 1);
            String emailUsuario = intent.getStringExtra("EMAIL_USUARIO");

            rolSeleccionado = nuevoRolId == 1 ? "Estudiante" : "Conductor";

            binding.txtEmail.setText(emailUsuario);
            binding.txtEmail.setEnabled(false);
        }

        setupRolButtons();

        binding.btnRegistrar.setOnClickListener(v -> registrarUsuario());

        binding.tvIniciarSesion.setOnClickListener(v -> {
            startActivity(new Intent(RegistrarActivity.this, LoginActivity.class));
            finish();
        });

        binding.icFoto.setOnClickListener(v -> showImagePickDialog());
    }

    private void setupRolButtons() {
        updateButtonStyles();
        if (isAddingNewRole) {
            binding.btnEstudiante.setEnabled(false);
            binding.btnConductor.setEnabled(false);
            binding.txtTitulo.setText("Añadir Rol de " + rolSeleccionado);
        } else {
            binding.btnEstudiante.setOnClickListener(v -> {
                rolSeleccionado = "Estudiante";
                updateButtonStyles();
            });

            binding.btnConductor.setOnClickListener(v -> {
                rolSeleccionado = "Conductor";
                updateButtonStyles();
            });
        }
    }

    private void updateButtonStyles() {
        if ("Estudiante".equals(rolSeleccionado)) {
            binding.layoutConductor.setVisibility(View.GONE);
            binding.btnEstudiante.setBackgroundColor(getResources().getColor(R.color.deep_sky_blue));
            binding.btnEstudiante.setTextColor(getResources().getColor(android.R.color.white));
            binding.btnConductor.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            binding.btnConductor.setTextColor(getResources().getColor(android.R.color.darker_gray));
        } else {
            binding.layoutConductor.setVisibility(View.VISIBLE);
            binding.btnConductor.setBackgroundColor(getResources().getColor(R.color.deep_sky_blue));
            binding.btnConductor.setTextColor(getResources().getColor(android.R.color.white));
            binding.btnEstudiante.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            binding.btnEstudiante.setTextColor(getResources().getColor(android.R.color.darker_gray));
        }
    }

    // ... (Métodos para cámara/galería sin cambios: showImagePickDialog, etc.) ...
    private void showImagePickDialog() {
        String[] options = {"Cámara", "Galería"};
        new AlertDialog.Builder(this).setTitle("Seleccionar Imagen").setItems(options, (dialog, which) -> {
            if (which == 0) checkCameraPermissionAndLaunch(); else checkGalleryPermissionAndLaunch();
        }).create().show();
    }

    private void checkCameraPermissionAndLaunch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) launchCamera();
        else requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
    }

    private void checkGalleryPermissionAndLaunch() {
        String p = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ? Manifest.permission.READ_MEDIA_IMAGES : Manifest.permission.READ_EXTERNAL_STORAGE;
        if (ContextCompat.checkSelfPermission(this, p) == PackageManager.PERMISSION_GRANTED) launchGallery();
        else requestGalleryPermissionLauncher.launch(p);
    }

    private void launchCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Nueva Foto");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        takePictureLauncher.launch(imageUri);
    }

    private void launchGallery() { pickImageLauncher.launch("image/*"); }


    private void registrarUsuario() {
        if (!validarCampos()) return;

        UsuarioData usuarioData = buildUsuarioData();

        ApiService apiService = RetrofitClient.createService();
        Call<UsuarioResponse> call = apiService.registrarUsuario(usuarioData);

        call.enqueue(new Callback<UsuarioResponse>() {
            @Override
            public void onResponse(Call<UsuarioResponse> call, Response<UsuarioResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    UsuarioData idData = response.body().getData();
                    if (imageUri != null && idData != null) {
                        subirFotoDePerfil(idData.getId(), imageUri);
                    } else {
                        navegarALogin(isAddingNewRole ? "Nuevo rol registrado. Inicie sesión para continuar." : "Registro exitoso");
                    }
                } else {
                    handleApiError(response);
                }
            }

            @Override
            public void onFailure(Call<UsuarioResponse> call, Throwable t) {
                Log.e("REGISTRO_ERROR", "Fallo en la llamada: " + t.getMessage());
                Toast.makeText(RegistrarActivity.this, "Fallo de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void subirFotoDePerfil(int userId, Uri uri) {
        File file = createTempFileFromUri(uri);
        if (file == null) {
            Toast.makeText(this, "Error al preparar la imagen para subir", Toast.LENGTH_SHORT).show();
            navegarALogin("Registro exitoso, pero no se pudo subir la foto.");
            return;
        }

        RequestBody idPart = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(userId));
        RequestBody filePart = RequestBody.create(MediaType.parse(getContentResolver().getType(uri)), file);
        MultipartBody.Part foto = MultipartBody.Part.createFormData("foto", file.getName(), filePart);

        ApiService apiService = RetrofitClient.createService();
        Call<ResponseBody> call = apiService.subirFoto(idPart, foto);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                String message = isAddingNewRole ? "Nuevo rol registrado. Inicie sesión." : "Registro completo con foto.";
                if (!response.isSuccessful()) {
                    message = "Registro exitoso, pero falló la subida de la foto.";
                }
                navegarALogin(message);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                navegarALogin("Registro exitoso, pero falló la conexión para subir la foto.");
            }
        });
    }

    private UsuarioData buildUsuarioData() {
        UsuarioData usuarioData = new UsuarioData();
        usuarioData.setNombres(binding.txtNombres.getText().toString().trim());
        usuarioData.setApellido_paterno(binding.txtApellidoPaterno.getText().toString().trim());
        usuarioData.setApellido_materno(binding.txtApellidoMaterno.getText().toString().trim());
        usuarioData.setDni(binding.txtDni.getText().toString().trim());
        usuarioData.setEmail(binding.txtEmail.getText().toString().trim());
        usuarioData.setTelefono(binding.txtTelefono.getText().toString().trim());
        usuarioData.setClave(binding.txtClave.getText().toString().trim());
        usuarioData.setClave_confirmada(binding.txtConfirmarClave.getText().toString().trim());
        usuarioData.setRol_id("Conductor".equals(rolSeleccionado) ? 2 : 1);

        if ("Conductor".equals(rolSeleccionado)) {
            VehiculoData vehiculoData = new VehiculoData();
            vehiculoData.setPlaca(binding.txtPlaca.getText().toString().trim());
            vehiculoData.setMarca(binding.txtMarca.getText().toString().trim());
            vehiculoData.setModelo(binding.txtModelo.getText().toString().trim());
            vehiculoData.setColor(binding.txtColor.getText().toString().trim());
            try {
                vehiculoData.setPasajeros(Integer.parseInt(binding.txtPasajeros.getText().toString()));
            } catch (NumberFormatException e) { vehiculoData.setPasajeros(0); }
            usuarioData.setVehiculo(vehiculoData);
        }
        return usuarioData;
    }

    private void handleApiError(Response<UsuarioResponse> response) {
        String errorMessage = "Error en el registro";
        if (response.errorBody() != null) {
            try {
                JSONObject jsonObject = new JSONObject(response.errorBody().string());
                errorMessage = jsonObject.getString("message");
            } catch (Exception e) {
                Log.e("REGISTRO_ERROR", "Error al parsear el cuerpo del error", e);
            }
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }

    private void navegarALogin(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private File createTempFileFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            File tempFile = File.createTempFile("upload", ".tmp", getCacheDir());
            tempFile.deleteOnExit();
            OutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[4 * 1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
            return tempFile;
        } catch (IOException e) {
            Log.e("FileCreation", "Error creating temp file", e);
            return null;
        }
    }


    private boolean validarCampos() {
        // La lógica de validación se mantiene igual
        return true; // Simplificado por brevedad
    }
}
