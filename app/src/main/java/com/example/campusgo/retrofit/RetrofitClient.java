package com.example.campusgo.retrofit;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    public static final String URL_API_SERVICE = "https://campus-go.onrender.com";
    public static String API_TOKEN;
    public static String BASE_URL = URL_API_SERVICE;

    private static class AuthInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request original = chain.request();
            Request.Builder requestBuilder = original.newBuilder().header("Content-Type", "application/json");

            String token = API_TOKEN;
            if (token != null && !token.isEmpty()) {
                requestBuilder.header("Authorization", "Bearer " + token);
            }

            Request request = requestBuilder.build();
            return chain.proceed(request);
        }
    }

    // Aumentamos los tiempos de espera a 60 segundos
    private static final OkHttpClient httpClient = new OkHttpClient.Builder()
            .addInterceptor(new AuthInterceptor())
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    public static final Retrofit API_SERVICE = new Retrofit.Builder()
            .baseUrl(URL_API_SERVICE)
            .client(httpClient) // Usamos el cliente con los nuevos timeouts
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    public static ApiService createService() {
        return API_SERVICE.create(ApiService.class);
    }
}
