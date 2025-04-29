package com.example.factastic;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.transition.Transition;

import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {


    private ConstraintLayout rootLayout;
    private TextView factText;
    private Button newFactButton;
    private ImageButton btnShare;
    private final String ACCESS_KEY = "guBIlwSC0bF54DewpViUQ-Dinx_gnzZAovgYO6vdI-s"; // Replace with your Unsplash Access Key
    private final String UNSPLASH_URL = "https://api.unsplash.com/photos/random?query=abstract&orientation=portrait&client_id=" + ACCESS_KEY;
    private FunFactApi apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        factText = findViewById(R.id.factText);
        newFactButton = findViewById(R.id.newFactButton);
        btnShare = findViewById(R.id.btnShare);
        rootLayout=findViewById(R.id.rootLayout);


//       rootLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.img2));

        setupRetrofit();
        fetchRandomImage();

        newFactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchRandomFact();
                fetchRandomImage();
            }
        });
        btnShare.setOnClickListener(v -> shareFact());

    }

    private void setupRetrofit() {
        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://uselessfacts.jsph.pl/").addConverterFactory(GsonConverterFactory.create()).build();

        apiService = retrofit.create(FunFactApi.class);
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

    private void fetchRandomFact() {
        factText.setText("Fetching Random Fact....");

        apiService.getRandomFact("en").enqueue(new Callback<FunFact>() {
            @Override
            public void onResponse(Call<FunFact> call, Response<FunFact> response) {

                if (response.isSuccessful() && response.body() != null) {
                    factText.setText(response.body().getText());
                } else {
                    factText.setText("Hmmm..couldn't load fact right now ");
                }
            }

            @Override
            public void onFailure(Call<FunFact> call, Throwable t) {
                factText.setText("Something went wrong.please try again later");
                Toast.makeText(MainActivity.this, "Error:" + t.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void fetchRandomImage() {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            try {
                URL url = new URL(UNSPLASH_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream stream = connection.getInputStream();
                Scanner scanner = new Scanner(stream).useDelimiter("\\A");
                String response = scanner.hasNext() ? scanner.next() : "";

                JSONObject jsonObject = new JSONObject(response);
                String imageUrl = jsonObject.getJSONObject("urls").getString("regular");

                runOnUiThread(() -> {
                    Glide.with(MainActivity.this)
                            .asBitmap()
                            .load(imageUrl)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .override(1080, 1920) // Adjust based on screen resolution
                            .into(new CustomTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                    Drawable drawable = new BitmapDrawable(getResources(), resource);
                                    rootLayout.setBackground(drawable);
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {
                                    rootLayout.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.img2));
                                }

                                @Override
                                public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                    super.onLoadFailed(errorDrawable);
                                    rootLayout.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.img2));
                                    Toast.makeText(MainActivity.this, "Failed to load image", Toast.LENGTH_SHORT).show();
                                }
                            });

                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error fetching image", Toast.LENGTH_SHORT).show());
            }

        });

    }
}
