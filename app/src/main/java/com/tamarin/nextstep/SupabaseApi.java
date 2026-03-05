package com.tamarin.nextstep;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface SupabaseApi {

    // NOVO: Rota de Login (Pede Email/Senha, Devolve Token)
    @POST("auth/v1/token?grant_type=password")
    Call<LoginResponse> loginUser(@Body LoginRequest request);

    // ANTIGO: Busca transações
    @GET("rest/v1/transactions?select=*&order=date.desc")
    Call<List<Transaction>> getTransactions();
}