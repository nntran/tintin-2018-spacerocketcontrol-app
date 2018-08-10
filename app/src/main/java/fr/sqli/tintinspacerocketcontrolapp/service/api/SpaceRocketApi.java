package fr.sqli.tintinspacerocketcontrolapp.service.api;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.GET;

public interface SpaceRocketApi {

    @GET("/health")
    Observable<Health> health();
}
