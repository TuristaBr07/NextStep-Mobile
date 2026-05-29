package com.tamarin.nextstep;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AgendaActivity extends AppCompatActivity {

    private MaterialButtonToggleGroup toggleAgendaTab;
    private SwipeRefreshLayout swipeAgenda;
    private RecyclerView rvAgendaItems;
    private LinearLayout layoutAgendaEmpty;

    private TransactionAdapter adapter;
    private final List<Transaction> displayedItems = new ArrayList<>();
    private List<Transaction> allPending = new ArrayList<>();

    private final java.util.List<retrofit2.Call<?>> pendingCalls = new java.util.ArrayList<>();

    private <T> retrofit2.Call<T> track(retrofit2.Call<T> call) {
        pendingCalls.add(call);
        return call;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (retrofit2.Call<?> c : pendingCalls) {
            c.cancel();
        }
        pendingCalls.clear();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agenda);

        toggleAgendaTab = findViewById(R.id.toggleAgendaTab);
        swipeAgenda = findViewById(R.id.swipeAgenda);
        rvAgendaItems = findViewById(R.id.rvAgendaItems);
        layoutAgendaEmpty = findViewById(R.id.layoutAgendaEmpty);

        android.widget.ImageButton btnBack = findViewById(R.id.btnAgendaBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        swipeAgenda.setColorSchemeColors(ContextCompat.getColor(this, R.color.ns_primary));
        swipeAgenda.setOnRefreshListener(this::loadPendingTransactions);

        rvAgendaItems.setLayoutManager(new LinearLayoutManager(this));
        rvAgendaItems.setHasFixedSize(false);
        adapter = new TransactionAdapter(displayedItems);
        rvAgendaItems.setAdapter(adapter);

        toggleAgendaTab.check(R.id.btnTabPayable);
        toggleAgendaTab.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) filterAndShow();
        });

        loadPendingTransactions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPendingTransactions();
    }

    private void loadPendingTransactions() {
        swipeAgenda.setRefreshing(true);

        track(RetrofitClient.getApi().getPendingTransactions()).enqueue(new Callback<List<Transaction>>() {
            @Override
            public void onResponse(Call<List<Transaction>> call, Response<List<Transaction>> response) {
                if (isFinishing() || isDestroyed()) return;
                swipeAgenda.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    allPending = response.body();
                    filterAndShow();
                } else if (response.code() == 401) {
                    Toast.makeText(AgendaActivity.this, getString(R.string.error_session_expired), Toast.LENGTH_SHORT).show();
                    SessionManager.clear();
                    Intent intent = new Intent(AgendaActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(AgendaActivity.this, getString(R.string.error_load_agenda), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Transaction>> call, Throwable t) {
                if (call.isCanceled() || isFinishing() || isDestroyed()) return;
                swipeAgenda.setRefreshing(false);
                Toast.makeText(AgendaActivity.this, getString(R.string.error_connection_agenda), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterAndShow() {
        int checkedId = toggleAgendaTab.getCheckedButtonId();
        String typeFilter = (checkedId == R.id.btnTabReceivable)
                ? getString(R.string.transaction_type_income)
                : getString(R.string.transaction_type_expense);

        displayedItems.clear();
        for (Transaction tx : allPending) {
            if (tx.getType() != null && tx.getType().equalsIgnoreCase(typeFilter)) {
                displayedItems.add(tx);
            }
        }

        adapter.notifyDataSetChanged();

        if (displayedItems.isEmpty()) {
            layoutAgendaEmpty.setVisibility(View.VISIBLE);
            rvAgendaItems.setVisibility(View.GONE);
        } else {
            layoutAgendaEmpty.setVisibility(View.GONE);
            rvAgendaItems.setVisibility(View.VISIBLE);
        }
    }
}
