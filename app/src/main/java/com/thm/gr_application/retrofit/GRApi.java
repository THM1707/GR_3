package com.thm.gr_application.retrofit;

import com.thm.gr_application.payload.CredentialResponse;
import com.thm.gr_application.payload.ParkingLotInRequest;
import com.thm.gr_application.payload.ParkingLotsResponse;
import com.thm.gr_application.payload.SignUpRequest;
import com.thm.gr_application.payload.SignUpResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GRApi {
    @POST("api/auth/signin")
    Call<CredentialResponse> login(@Query("usernameOrEmail") String username,
                                   @Query("password") String password);

    @POST("api/auth/signup")
    Call<SignUpResponse> signUp(@Body SignUpRequest request);

    @GET("api/parking_lot/all")
    Call<ParkingLotsResponse> getParkingLots(@Header("Authorization") String authToken);

    @POST("api/parking_lot/in")
    Call<ParkingLotsResponse> getParkingLotsIn(@Header("Authorization") String authToken,
                                               @Body ParkingLotInRequest request);

    @GET("api/favorite")
    Call<ParkingLotsResponse> getFavorite(@Header("Authorization") String authToken);

    @PUT("api/favorite/remove/{id}")
    Call<Void> removeFavorite(@Header("Authorization") String authToken, @Path("id") Long id);

    @PUT("api/favorite/add/{id}")
    Call<Void> addFavorite(@Header("Authorization") String authToken, @Path("id") Long id);
}
