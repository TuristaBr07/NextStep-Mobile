package com.tamarin.nextstep;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PATCH;
import retrofit2.http.DELETE;
import retrofit2.http.Query;

public interface SupabaseApi {

    // --- AUTENTICAÇÃO ---
    @POST("auth/v1/token?grant_type=password")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("auth/v1/signup")
    Call<Void> register(@Body SignUpRequest request);

    @POST("auth/v1/recover")
    Call<Void> recoverPassword(@Body RecoverRequest request);

    // --- PERFIL ---
    @GET("rest/v1/profiles?select=*")
    Call<List<Profile>> getProfile(@Query("id") String idQuery);

    @Headers("Prefer: return=representation")
    @PATCH("rest/v1/profiles")
    Call<List<Profile>> updateProfile(@Query("id") String idQuery, @Body Profile profile);

    // --- TRANSAÇÕES ---
    @GET("rest/v1/transactions?select=*&order=date.desc")
    Call<List<Transaction>> getTransactions();

    @Headers("Prefer: return=representation")
    @POST("rest/v1/transactions")
    Call<List<Transaction>> createTransaction(@Body Transaction transaction);

    @Headers("Prefer: return=representation")
    @PATCH("rest/v1/transactions")
    Call<List<Transaction>> updateTransaction(@Query("id") String idQuery, @Body Transaction transaction);

    @DELETE("rest/v1/transactions")
    Call<Void> deleteTransaction(@Query("id") String idQuery);

    // --- CATEGORIAS ---
    @GET("rest/v1/categories?select=*")
    Call<List<Category>> getCategories();

    @Headers("Prefer: return=representation")
    @POST("rest/v1/categories")
    Call<List<Category>> createCategory(@Body Category category);

    @DELETE("rest/v1/categories")
    Call<Void> deleteCategory(@Query("id") String idQuery);
}