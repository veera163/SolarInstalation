package com.example.home.solarinstalation.Api;






import com.example.home.solarinstalation.Model.UploadImageResult;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface CollectApi {
    @Multipart
    @POST("upload/installationStatus/plantId/{plantId}/device/{deviceId}")
    Call<UploadImageResult> uploadImage(@Part MultipartBody.Part[] file, @Path("plantId") String plantId, @Path("deviceId") String deviceId);
   /* @GET("location")
    Call<Result> test();*/
}
