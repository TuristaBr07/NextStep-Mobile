package com.tamarin.nextstep;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.tamarin.nextstep.chatbot.ChatbotApi;
import com.tamarin.nextstep.chatbot.ChatbotRequest;
import com.tamarin.nextstep.chatbot.ChatbotResponse;
import com.tamarin.nextstep.chatbot.ChatbotRetrofitClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatbotActivity extends AppCompatActivity {

    private RecyclerView rvChatMessages;
    private TextInputEditText etChatMessage;
    private ImageButton btnSendMessage;
    private TextView tvChatEmptyState;

    private final List<ChatMessage> messages = new ArrayList<>();
    private ChatMessageAdapter adapter;

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        MaterialToolbar toolbar = findViewById(R.id.toolbarChatbot);
        rvChatMessages = findViewById(R.id.rvChatMessages);
        etChatMessage = findViewById(R.id.etChatMessage);
        btnSendMessage = findViewById(R.id.btnSendMessage);
        tvChatEmptyState = findViewById(R.id.tvChatEmptyState);

        toolbar.setNavigationOnClickListener(v -> finish());

        rvChatMessages.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatMessageAdapter(messages);
        rvChatMessages.setAdapter(adapter);

        addBotMessage(buildWelcomeMessage());

        btnSendMessage.setOnClickListener(v -> sendMessage());

        updateEmptyState();
    }

    private String buildWelcomeMessage() {
        if (ChatbotRetrofitClient.isConfigured()) {
            return "Olá! Eu sou o assistente da NextStep.\n\n" +
                    "Nesta versão, já estou preparado para integração HTTP com o microserviço externo.";
        }

        return "Olá! Eu sou o assistente da NextStep.\n\n" +
                "Nesta versão, o endpoint externo ainda não foi configurado no app. " +
                "Por isso, vou responder em modo local até a integração real ser ativada.";
    }

    private void sendMessage() {
        String userText = etChatMessage.getText() != null
                ? etChatMessage.getText().toString().trim()
                : "";

        if (TextUtils.isEmpty(userText)) {
            etChatMessage.setError("Digite uma mensagem");
            return;
        }

        etChatMessage.setError(null);
        addUserMessage(userText);
        etChatMessage.setText("");
        requestBotReply(userText);
    }

    private void requestBotReply(String userText) {
        setSendingState(true);

        ChatbotApi chatbotApi = ChatbotRetrofitClient.getApi();

        if (chatbotApi == null) {
            simulateLocalFallback(userText);
            return;
        }

        String userId = SessionManager.getUserId();
        ChatbotRequest request = new ChatbotRequest(userText, userId, "android");

        chatbotApi.sendMessage(request).enqueue(new Callback<ChatbotResponse>() {
            @Override
            public void onResponse(Call<ChatbotResponse> call, Response<ChatbotResponse> response) {
                setSendingState(false);

                if (response.isSuccessful() && response.body() != null) {
                    String reply = response.body().extractBestReply();

                    if (reply != null && !reply.isEmpty()) {
                        addBotMessage(reply);
                        return;
                    }
                }

                addBotMessage(buildMockReply(userText));
                Toast.makeText(
                        ChatbotActivity.this,
                        "Resposta externa indisponível. Usando modo local.",
                        Toast.LENGTH_SHORT
                ).show();
            }

            @Override
            public void onFailure(Call<ChatbotResponse> call, Throwable t) {
                setSendingState(false);
                addBotMessage(buildMockReply(userText));
                Toast.makeText(
                        ChatbotActivity.this,
                        "Falha ao conectar com o assistente externo. Usando modo local.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void simulateLocalFallback(String userText) {
        handler.postDelayed(() -> {
            setSendingState(false);
            addBotMessage(buildMockReply(userText));
        }, 600);
    }

    private void setSendingState(boolean sending) {
        btnSendMessage.setEnabled(!sending);
        etChatMessage.setEnabled(!sending);
    }

    private String buildMockReply(String userText) {
        String lower = userText.toLowerCase(Locale.ROOT);

        if (lower.contains("mei") || lower.contains("limite")) {
            return "Dica NextStep: acompanhe o faturamento acumulado do ano para evitar ultrapassar o limite do MEI de R$ 81.000,00.";
        }

        if (lower.contains("despesa") || lower.contains("gasto")) {
            return "Uma boa prática é separar despesas pessoais das despesas do negócio e classificar cada lançamento corretamente.";
        }

        if (lower.contains("receita") || lower.contains("entrada")) {
            return "Você pode registrar cada entrada assim que ela acontecer. Isso melhora os relatórios e o controle do caixa.";
        }

        if (lower.contains("das") || lower.contains("imposto")) {
            return "Lembrete importante: acompanhar obrigações como o DAS reduz risco de atraso e ajuda na organização financeira.";
        }

        return "Entendi sua mensagem: \"" + userText + "\".\n\n" +
                "Neste momento, estou usando a resposta local do app. Assim que o endpoint Python estiver pronto, esta tela já poderá consumir a resposta externa.";
    }

    private void addUserMessage(String text) {
        messages.add(new ChatMessage(text, true));
        adapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();
        updateEmptyState();
    }

    private void addBotMessage(String text) {
        messages.add(new ChatMessage(text, false));
        adapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();
        updateEmptyState();
    }

    private void scrollToBottom() {
        if (!messages.isEmpty()) {
            rvChatMessages.scrollToPosition(messages.size() - 1);
        }
    }

    private void updateEmptyState() {
        tvChatEmptyState.setVisibility(messages.isEmpty() ? TextView.VISIBLE : TextView.GONE);
    }
}