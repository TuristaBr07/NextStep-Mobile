package com.tamarin.nextstep;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface SupabaseApi {

    // --- AUTENTICAÇÃO ---
    @POST("auth/v1/token?grant_type=password")
    Call<LoginResponse> login(@Body LoginRequest request);

    // --- TRANSAÇÕES ---

    // Buscar transações (ordenadas por data, da mais recente para a mais antiga)
    @GET("rest/v1/transactions?select=*&order=date.desc")
    Call<List<Transaction>> getTransactions();

    // CRIAR NOVA TRANSAÇÃO (O código novo começa aqui)
    // O header "Prefer: return=representation" pede pro Supabase devolver
    // os dados que acabou de salvar (útil para confirmar que deu certo).
    @Headers("Prefer: return=representation")
    @POST("rest/v1/transactions")
    Call<List<Transaction>> createTransaction(@Body Transaction transaction);

    // --- CATEGORIAS ---

    // Buscar lista de categorias para preencher o menu
    @GET("rest/v1/categories?select=*")
    Call<List<Category>> getCategories();
}