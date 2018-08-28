package fr.sqli.tintinspacerocketcontrolapp.simon.api;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface SpaceRocketApi {

    @GET("/health")
    Observable<Health> health();

    @POST("/simon/start")
    Observable<Start> start(@Body final Gamer gamer);

    @POST("/simon/{id}/play")
    Observable<Play> play(@Path("id") final int gamerId);

    @POST("/simon/{id}/try")
    Observable<TryResponse> trySequence(@Path("id") final int gamerId, @Body final TryBody tryBody);
}
