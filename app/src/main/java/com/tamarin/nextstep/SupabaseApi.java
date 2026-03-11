package com.tamarin.nextstep;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PATCH;   // <-- Importação adicionada
import retrofit2.http.DELETE;  // <-- Importação adicionada
import retrofit2.http.Query;

public interface SupabaseApi {

    // --- AUTENTICAÇÃO ---
    @POST("auth/v1/token?grant_type=password")
    Call<LoginResponse> login(@Body LoginRequest request);

    // --- TRANSAÇÕES ---

    // Buscar transações (ordenadas por data, da mais recente para a mais antiga)
    @GET("rest/v1/transactions?select=*&order=date.desc")
    Call<List<Transaction>> getTransactions();

    // CRIAR NOVA TRANSAÇÃO
    @Headers("Prefer: return=representation")
    @POST("rest/v1/transactions")
    Call<List<Transaction>> createTransaction(@Body Transaction transaction);

    // ATUALIZAR TRANSAÇÃO (O Supabase usa PATCH para atualizações)
    @Headers("Prefer: return=representation")
    @PATCH("rest/v1/transactions")
    Call<List<Transaction>> updateTransaction(@Query("id") String idQuery, @Body Transaction transaction);

    // APAGAR TRANSAÇÃO
    @DELETE("rest/v1/transactions")
    Call<Void> deleteTransaction(@Query("id") String idQuery);

    // --- CATEGORIAS ---

    // Buscar lista de categorias para preencher o menu
    @GET("rest/v1/categories?select=*")
    Call<List<Category>> getCategories();
}