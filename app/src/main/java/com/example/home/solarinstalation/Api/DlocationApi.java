package com.example.home.solarinstalation.Api;



import com.example.home.solarinstalation.Model.LocationRes;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by home on 12/18/2017.
 */

public interface DlocationApi {

    @Headers("Content-Type: application/json")
    @POST("postGeoLocation/")
    Call<LocationRes> getUser(@Body String body);
}
