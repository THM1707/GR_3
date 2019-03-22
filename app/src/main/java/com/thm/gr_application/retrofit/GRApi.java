package com.thm.gr_application.retrofit;

import com.thm.gr_application.payload.CredentialResponse;
import com.thm.gr_application.payload.InvoiceIndexResponse;
import com.thm.gr_application.payload.InvoiceResponse;
import com.thm.gr_application.payload.MessageResponse;
import com.thm.gr_application.payload.ParkingLotInRequest;
import com.thm.gr_application.payload.ParkingLotsResponse;
import com.thm.gr_application.payload.SignUpRequest;
import com.thm.gr_application.payload.SignUpResponse;

import io.reactivex.Completable;
import io.reactivex.Single;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GRApi {
    @POST("api/auth/signIn")
    Single<CredentialResponse> login(@Query("usernameOrEmail") String username,
                                     @Query("password") String password);

    @POST("api/auth/signUp")
    Single<SignUpResponse> signUp(@Body SignUpRequest request);

    @POST("api/auth/changePassword")
    Single<MessageResponse> changePassword(@Header("Authorization") String authToken,
                                           @Query("oldPassword") String oldPassword,
                                           @Query("newPassword") String newPassword);

    @GET("api/parking_lot/all")
    Single<ParkingLotsResponse> getParkingLots(@Header("Authorization") String authToken);

    @POST("api/parking_lot/in")
    Call<ParkingLotsResponse> getParkingLotsIn(@Header("Authorization") String authToken,
                                               @Body ParkingLotInRequest request);

    @GET("api/favorite")
    Call<ParkingLotsResponse> getFavorite(@Header("Authorization") String authToken);

    @PUT("api/favorite/remove/{id}")
    Completable removeFavorite(@Header("Authorization") String authToken, @Path("id") Long id);

    @PUT("api/favorite/add/{id}")
    Completable addFavorite(@Header("Authorization") String authToken, @Path("id") Long id);

    @GET("api/invoice/user_pending")
    Single<InvoiceResponse> getUserPending(@Header("Authorization") String authToken);

    @POST("api/invoice/cancel/{id}")
    Single<MessageResponse> cancelPending(@Header("Authorization") String authToken, @Path("id") Long id);

    @POST("api/invoice/request")
    Single<InvoiceResponse> requestBooking(@Header("Authorization") String authToken,
                                           @Query("parkingLotId") Long pId,
                                           @Query("plate") String plate);


    @GET("api/invoice/manager/index")
    Single<InvoiceIndexResponse> invoiceIndex(@Header("Authorization") String authToken);

    @POST("api/invoice/manager/create")
    Single<InvoiceResponse> createBooking(@Header("Authorization") String authToken, @Query("plate") String plate);

    @POST("api/invoice/manager/accept/{id}")
    Single<InvoiceResponse> acceptBooking(@Header("Authorization") String authToken, @Path("id") Long id);

    @POST("api/invoice/manager/withdraw/{id}")
    Single<InvoiceResponse> withdraw(@Header("Authorization") String authToken, @Path("id") Long id);
}
