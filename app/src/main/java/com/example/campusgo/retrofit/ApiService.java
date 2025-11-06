package com.example.campusgo.retrofit;

import com.example.campusgo.model.UsuarioData;
import com.example.campusgo.request.LoginRequest;
import com.example.campusgo.request.ReservaRequest;
import com.example.campusgo.request.ViajeListadoRequest;
import com.example.campusgo.response.LoginResponse;
import com.example.campusgo.response.ReservaResponse;
import com.example.campusgo.response.UsuarioResponse;
import com.example.campusgo.response.ViajeListadoResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ApiService {
    @POST("/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @GET("/usuario/foto/{id}")
    Call<ResponseBody> getFoto(@Path("id") int id);

    @POST("/usuario/registrar")
    Call<UsuarioResponse> registrarUsuario(@Body UsuarioData usuarioData);

    /**
     * Sube la foto de perfil de un usuario.
     * Esta es una llamada multipart, que envía datos en varias partes (un ID y un archivo).
     *
     * @param id   El ID del usuario como un RequestBody de tipo texto.
     * @param foto El archivo de la imagen como un MultipartBody.Part.
     * @return
     */
    @Multipart
    @PUT("usuario/actualizarfoto")
    Call<ResponseBody> subirFoto(@Part("id") RequestBody id, @Part MultipartBody.Part foto);

    @POST("/viaje/listar")
    Call<ViajeListadoResponse> listarViajes(@Body ViajeListadoRequest request);

    @POST("/reserva/registrar")
    Call<ReservaResponse> registrarReserva(@Body ReservaRequest request);



}
