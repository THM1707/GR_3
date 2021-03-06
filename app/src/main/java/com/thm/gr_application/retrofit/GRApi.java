package com.thm.gr_application.retrofit;

import com.thm.gr_application.payload.CredentialResponse;
import com.thm.gr_application.payload.InvoiceResponse;
import com.thm.gr_application.payload.InvoicesResponse;
import com.thm.gr_application.payload.MessageResponse;
import com.thm.gr_application.payload.ParkingLotResponse;
import com.thm.gr_application.payload.ParkingLotsResponse;
import com.thm.gr_application.payload.ProfileRequest;
import com.thm.gr_application.payload.ProfileResponse;
import com.thm.gr_application.payload.ReviewRequest;
import com.thm.gr_application.payload.ReviewResposne;
import com.thm.gr_application.payload.ReviewsResponse;
import com.thm.gr_application.payload.SearchRequest;
import com.thm.gr_application.payload.SearchResponse;
import com.thm.gr_application.payload.SignUpRequest;
import com.thm.gr_application.payload.SignUpResponse;
import io.reactivex.Completable;
import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
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

    @GET("api/parking_lot/{id}")
    Single<ParkingLotResponse> getParkingLotById(@Header("Authorization") String authToken,
            @Path("id") Long id);

    @GET("api/favorite")
    Single<ParkingLotsResponse> getFavorite(@Header("Authorization") String authToken);

    @PUT("api/favorite/remove/{id}")
    Completable removeFavorite(@Header("Authorization") String authToken, @Path("id") Long id);

    @PUT("api/favorite/add/{id}")
    Completable addFavorite(@Header("Authorization") String authToken, @Path("id") Long id);

    @GET("api/invoice/user_pending")
    Single<InvoiceResponse> getUserPending(@Header("Authorization") String authToken);

    @POST("api/invoice/cancel/{id}")
    Single<MessageResponse> cancelPending(@Header("Authorization") String authToken,
            @Path("id") Long id);

    @POST("api/invoice/change/{id}")
    Single<MessageResponse> changeReservation(@Header("Authorization") String authToken,
            @Path("id") Long id, @Query("plate") String plate, @Query("duration") int duration);

    @POST("api/invoice/request")
    Single<InvoiceResponse> requestBooking(@Header("Authorization") String authToken,
            @Query("parkingLotId") Long pId, @Query("plate") String plate,
            @Query("duration") int duration);

    @POST("api/invoice/manager/create")
    Single<InvoiceResponse> createBooking(@Header("Authorization") String authToken,
            @Query("plate") String plate);

    @POST("api/invoice/manager/accept/{id}")
    Single<InvoiceResponse> acceptBooking(@Header("Authorization") String authToken,
            @Path("id") Long id);

    @POST("api/invoice/manager/checkout/{id}")
    Single<InvoiceResponse> checkout(@Header("Authorization") String authToken,
            @Path("id") Long id);

    @GET("api/user/profile")
    Single<ProfileResponse> getProfile(@Header("Authorization") String authToken);

    @POST("api/user/changeProfile")
    Single<MessageResponse> changeProfile(@Header("Authorization") String authToken,
            @Body ProfileRequest request);

    @GET("api/parking_lot/{id}/reviews")
    Single<ReviewsResponse> getReviews(@Header("Authorization") String authToken,
            @Path("id") Long id);

    @POST("api/user/review")
    Single<ReviewResposne> submitReview(@Header("Authorization") String authToken,
            @Body ReviewRequest request);

    @POST("api/user/review/{id}")
    Single<ReviewResposne> editReview(@Header("Authorization") String authToken,
            @Path("id") Long id, @Query("star") int star, @Query("comment") String comment);

    @DELETE("api/user/review/{id}")
    Completable deleteReview(@Header("Authorization") String authToken, @Path("id") Long id);

    @GET("api/invoice/manager/active")
    Single<InvoicesResponse> getActiveInvoices(@Header("Authorization") String authToken);

    @GET("api/invoice/manager/done")
    Single<InvoicesResponse> getDoneInvoices(@Header("Authorization") String authToken);

    @POST("api/user/smartSearching")
    Single<SearchResponse> getSearchResult(@Header("Authorization") String token,
            @Body SearchRequest request);

    @POST("api/user/recharge")
    Single<MessageResponse> recharge(@Header("Authorization") String token,
            @Query("option") int option);

    @GET("api/user/budget")
    Single<MessageResponse> getBudget(@Header("Authorization") String token);

    @GET("api/user/history")
    Single<InvoicesResponse> getHistory(@Header("Authorization") String token);

    @POST("api/user/notificationRegistration")
    Completable notificationRegistration(@Header("Authorization") String authToken,
            @Query("token") String token);
}
