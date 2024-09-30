//package com.example.veterinaryclinicmobileapplication;
//
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.TextView;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//
//public class MainActivity extends AppCompatActivity {
//
//    private EditText userInput;
//    private TextView chatbotResponse;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        userInput = findViewById(R.id.userInput);
//        chatbotResponse = findViewById(R.id.chatbotResponse);
//        Button sendButton = findViewById(R.id.sendButton);
//
//        sendButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String message = userInput.getText().toString();
//                Log.d("MainActivity", "Send button clicked, message: " + message);
//                sendMessageToChatGPT(message);
//            }
//        });
//    }
//
//    private void sendMessageToChatGPT(String message) {
//        Log.d("MainActivity", "Preparing to send message to ChatGPT");
//        OpenAIApi apiService = ApiClient.getClient().create(OpenAIApi.class);
//        ChatRequest request = new ChatRequest("gpt-3.5-turbo", message, 50); // Use the correct model
//        Call<ChatResponse> call = apiService.getCompletion(request);
//
//        call.enqueue(new Callback<ChatResponse>() {
//            @Override
//            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
//                if (response.isSuccessful()) {
//                    String reply = response.body().getChoices().get(0).getText();
//                    Log.d("MainActivity", "Response received: " + reply);
//                    chatbotResponse.setText(reply);
//                } else {
//                    Log.e("MainActivity", "Response unsuccessful: " + response.message());
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ChatResponse> call, Throwable t) {
//                Log.e("MainActivity", "Error: " + t.getMessage());
//            }
//        });
//    }
//}
