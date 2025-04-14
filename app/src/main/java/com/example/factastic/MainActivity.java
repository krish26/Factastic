package com.example.factastic;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private TextView factText;
    private Button newFactButton;

    private ImageButton btnShare;
    private FunFactApi apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        factText=findViewById(R.id.factText);
        newFactButton=findViewById(R.id.newFactButton);
        btnShare = findViewById(R.id.btnShare);

        setupRetrofit();

        newFactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchRandomFact();
            }
        });
        btnShare.setOnClickListener(v -> shareFact());

    }

    private void setupRetrofit(){
        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://uselessfacts.jsph.pl/").addConverterFactory(GsonConverterFactory.create()).build();

        apiService=retrofit.create(FunFactApi.class);
    }

    private void shareFact() {
        String factToShare = factText.getText().toString();
        if (!factToShare.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, factToShare);
            startActivity(Intent.createChooser(intent, "Share this fun fact"));
        }
    }

    private void fetchRandomFact(){
        factText.setText("Fetching Random Fact....");

        apiService.getRandomFact("en").enqueue(new Callback<FunFact>() {
            @Override
            public void onResponse(Call<FunFact> call, Response<FunFact> response) {

                if(response.isSuccessful()&& response.body()!=null) {
                    factText.setText(response.body().getText());
                }
                else {
                    factText.setText("Hmmm..couldn't load fact right now ");
                }
            }

            @Override
            public void onFailure(Call<FunFact> call, Throwable t) {
                factText.setText("Something went wrong.please try again later");
                Toast.makeText(MainActivity.this,"Error:"+t.getMessage(),Toast.LENGTH_SHORT).show();

            }
        });
    }
}