package com.tamarin.nextstep;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatbotActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "nextstep_chatbot_prefs";
    private static final String KEY_CHAT_MESSAGES_PREFIX = "chat_messages_";

    private RecyclerView rvChatMessages;
    private TextInputEditText etChatMessage;
    private TextInputLayout tilChatMessage;
    private ImageButton btnSendMessage;
    private TextView tvChatEmptyState;
    private TextView tvTypingIndicator;
    private MaterialToolbar toolbar;

    private final List<ChatMessage> messages = new ArrayList<>();
    private ChatMessageAdapter adapter;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        toolbar = findViewById(R.id.toolbarChatbot);
        rvChatMessages = findViewById(R.id.rvChatMessages);
        etChatMessage = findViewById(R.id.etChatMessage);
        tilChatMessage = findViewById(R.id.tilChatMessage);
        btnSendMessage = findViewById(R.id.btnSendMessage);
        tvChatEmptyState = findViewById(R.id.tvChatEmptyState);
        tvTypingIndicator = findViewById(R.id.tvTypingIndicator);

        toolbar.setTitle("Assistente NextStep IA");
        toolbar.inflateMenu(R.menu.chatbot_toolbar_menu);
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setOnMenuItemClickListener(this::handleToolbarMenuClick);

        rvChatMessages.setLayoutManager(new LinearLayoutManager(this));
        // Presumo que o seu ChatMessageAdapter e ChatMessage continuam iguais no projeto
        adapter = new ChatMessageAdapter(messages);
        rvChatMessages.setAdapter(adapter);

        restoreChatHistory();

        if (messages.isEmpty()) {
            addBotMessage("Olá! Sou a Inteligência Artificial da NextStep.\n\nEstou conectado ao sistema e pronto para ajudar. Você pode me pedir para registrar despesas, receitas, ou tirar dúvidas sobre organização financeira!", false);
        }

        btnSendMessage.setOnClickListener(v -> sendMessage());

        updateEmptyState();
        showTypingIndicator(false);
        setSendingState(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        persistChatHistory();
    }

    private boolean handleToolbarMenuClick(MenuItem item) {
        if (item.getItemId() == R.id.action_clear_chat) {
            confirmClearChat();
            return true;
        }
        return false;
    }

    private void confirmClearChat() {
        new AlertDialog.Builder(this)
                .setTitle("Limpar conversa")
                .setMessage("Deseja apagar todo o histórico desta conversa?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Limpar", (dialog, which) -> clearChatHistory())
                .show();
    }

    private void clearChatHistory() {
        messages.clear();
        adapter.notifyDataSetChanged();
        clearPersistedChatHistory();

        addBotMessage("Histórico limpo! Como posso te ajudar agora?", true);
        showTypingIndicator(false);
        updateEmptyState();

        Toast.makeText(this, "Conversa limpa com sucesso.", Toast.LENGTH_SHORT).show();
    }

    private void sendMessage() {
        String userText = etChatMessage.getText() != null
                ? etChatMessage.getText().toString().trim()
                : "";

        if (TextUtils.isEmpty(userText)) {
            tilChatMessage.setError("Digite uma mensagem");
            return;
        }

        tilChatMessage.setError(null);
        addUserMessage(userText);
        etChatMessage.setText("");

        // Dispara a requisição real para o Spring Boot!
        requestBotReply(userText);
    }

    private void requestBotReply(String userText) {
        setSendingState(true);
        showTypingIndicator(true);

        ChatRequestDTO requestDTO = new ChatRequestDTO(userText);

        // Chama o nosso RetrofitClient oficial, que já manda o Token JWT no cabeçalho
        RetrofitClient.getApi().sendMessageToChatbot(requestDTO).enqueue(new Callback<ChatResponseDTO>() {
            @Override
            public void onResponse(Call<ChatResponseDTO> call, Response<ChatResponseDTO> response) {
                setSendingState(false);
                showTypingIndicator(false);

                if (response.isSuccessful() && response.body() != null) {
                    // Pega a resposta processada pela OpenAI lá no servidor
                    String reply = response.body().getReply();
                    addBotMessage(reply, true);
                } else {
                    addBotMessage("Houve um problema de comunicação com os servidores da NextStep (Erro " + response.code() + ").", true);
                }
            }

            @Override
            public void onFailure(Call<ChatResponseDTO> call, Throwable t) {
                setSendingState(false);
                showTypingIndicator(false);
                addBotMessage("Falha na rede ao contactar a IA: " + t.getMessage(), true);
            }
        });
    }

    private void setSendingState(boolean sending) {
        btnSendMessage.setEnabled(!sending);
        etChatMessage.setEnabled(!sending);
        tilChatMessage.setEnabled(!sending);
        btnSendMessage.setAlpha(sending ? 0.55f : 1f);

        int tintColor = ContextCompat.getColor(
                this,
                sending ? R.color.ns_surface : android.R.color.white
        );
        btnSendMessage.setColorFilter(tintColor);
    }

    private void showTypingIndicator(boolean visible) {
        tvTypingIndicator.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void addUserMessage(String text) {
        messages.add(new ChatMessage(text, true));
        adapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();
        updateEmptyState();
        persistChatHistory();
    }

    private void addBotMessage(String text, boolean persist) {
        messages.add(new ChatMessage(text, false));
        adapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();
        updateEmptyState();

        if (persist) {
            persistChatHistory();
        }
    }

    private void scrollToBottom() {
        if (!messages.isEmpty()) {
            rvChatMessages.scrollToPosition(messages.size() - 1);
        }
    }

    private void updateEmptyState() {
        tvChatEmptyState.setVisibility(messages.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void persistChatHistory() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String json = gson.toJson(messages);
        preferences.edit().putString(getChatStorageKey(), json).apply();
    }

    private void restoreChatHistory() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String json = preferences.getString(getChatStorageKey(), null);

        if (json == null || json.trim().isEmpty()) {
            return;
        }

        Type type = new TypeToken<List<ChatMessage>>() {}.getType();
        List<ChatMessage> restoredMessages = gson.fromJson(json, type);

        if (restoredMessages != null && !restoredMessages.isEmpty()) {
            messages.clear();
            messages.addAll(restoredMessages);
            adapter.notifyDataSetChanged();
            scrollToBottom();
        }
    }

    private void clearPersistedChatHistory() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        preferences.edit().remove(getChatStorageKey()).apply();
    }

    private String getChatStorageKey() {
        String userId = SessionManager.getUserId();

        if (userId == null || userId.trim().isEmpty()) {
            return KEY_CHAT_MESSAGES_PREFIX + "guest";
        }

        return KEY_CHAT_MESSAGES_PREFIX + userId;
    }
}