package fr.sqli.tintinspacerocketcontrolapp.service.api;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface SpaceRocketApi {

    @GET("/health")
    Observable<Health> health();

    @POST("/simon/start")
    Observable<Start> start(@Body final Gamer gamer);
}
