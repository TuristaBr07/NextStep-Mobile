package com.tamarin.nextstep;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

        addBotMessage(
                "Olá! Eu sou o assistente da NextStep.\n\n" +
                        "Nesta primeira versão, eu ainda estou em modo local, mas a tela já foi preparada " +
                        "para futura integração com o microserviço de IA."
        );

        btnSendMessage.setOnClickListener(v -> sendMessage());

        updateEmptyState();
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

        simulateBotReply(userText);
    }

    private void simulateBotReply(String userText) {
        btnSendMessage.setEnabled(false);

        handler.postDelayed(() -> {
            addBotMessage(buildMockReply(userText));
            btnSendMessage.setEnabled(true);
        }, 700);
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
                "Nesta fase, estou respondendo com lógica local. No próximo passo, posso ser conectado a um endpoint HTTP real da IA.";
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