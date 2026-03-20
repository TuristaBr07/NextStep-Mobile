package com.tamarin.nextstep;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.DELETE;
import retrofit2.http.Path;

public interface SupabaseApi {

    // ---------------------------------------------------
    // AUTENTICAÇÃO
    // ---------------------------------------------------
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("auth/register")
    Call<Void> register(@Body SignUpRequest request);

    // ---------------------------------------------------
    // TRANSAÇÕES & INTELIGÊNCIA FINANCEIRA
    // ---------------------------------------------------
    @GET("transacoes")
    Call<List<Transaction>> getTransactions();

    @GET("transacoes/resumo")
    Call<TransactionSummary> getTransactionSummary();

    @GET("transacoes/relatorio")
    Call<List<CategoryReport>> getCategoryReports();

    @POST("transacoes")
    Call<Transaction> createTransaction(@Body Transaction transaction);

    @PUT("transacoes/{id}")
    Call<Transaction> updateTransaction(@Path("id") Long id, @Body Transaction transaction);

    @DELETE("transacoes/{id}")
    Call<Void> deleteTransaction(@Path("id") Long id);

    // ---------------------------------------------------
    // CATEGORIAS
    // ---------------------------------------------------
    @GET("categorias")
    Call<List<Category>> getCategories();

    @POST("categorias")
    Call<Category> createCategory(@Body Category category);

    @DELETE("categorias/{id}")
    Call<Void> deleteCategory(@Path("id") Long id);

    // ---------------------------------------------------
    // PERFIL
    // ---------------------------------------------------
    @GET("perfis/{id}")
    Call<List<Profile>> getProfile(@Path("id") String userId);

    @PUT("perfis/{id}")
    Call<List<Profile>> updateProfile(@Path("id") String userId, @Body Profile profile);

    // ---------------------------------------------------
    // INTELIGÊNCIA ARTIFICIAL (CÉREBRO NEXTSTEP)
    // ---------------------------------------------------
    @POST("chatbot")
    Call<ChatResponseDTO> sendMessageToChatbot(@Body ChatRequestDTO request);

}