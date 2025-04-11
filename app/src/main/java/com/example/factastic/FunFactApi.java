package com.example.factastic;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;


public interface FunFactApi {

    @GET("random.json")
    Call<FunFact>getRandomFact(@Query("language") String language);


}
