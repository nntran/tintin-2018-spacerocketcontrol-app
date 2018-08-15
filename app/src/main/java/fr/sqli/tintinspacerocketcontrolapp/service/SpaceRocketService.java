package fr.sqli.tintinspacerocketcontrolapp.service;

import android.content.Context;

import fr.sqli.tintinspacerocketcontrolapp.player.Player;
import fr.sqli.tintinspacerocketcontrolapp.service.api.Gamer;
import fr.sqli.tintinspacerocketcontrolapp.service.api.SpaceRocketApi;
import fr.sqli.tintinspacerocketcontrolapp.service.api.Start;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;

/**
 * Singleton permettant d'appeler les APIs Rest de la Fusée
 */
public final class SpaceRocketService {

    private static SpaceRocketService INSTANCE;
    private SpaceRocketApi spaceRocketApi;
    private Context context;

    private static final String SHARED_PREF_NAME = "tintinspacerocket";
    private static final String SERVER_URL_SHARED_PREF_KEY = "server_url";

    private String serverUrl = "http://Android.local:8888/";

    private SpaceRocketService(final Context context) {
        this.context = context;

        final String savedServerUrl =
                this.context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
                        .getString(SERVER_URL_SHARED_PREF_KEY, null);

        if (savedServerUrl != null) {
            serverUrl = savedServerUrl;
        }

        initSpaceRocketApi();
    }

    /**
     * Renvoie l'instance du Singleton
     * @param context
     * @return
     */
    public final static SpaceRocketService getInstance(final Context context) {
        if (INSTANCE == null) {
            INSTANCE = new SpaceRocketService(context);
        }

        return INSTANCE;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(final String serverUrl) {
        this.serverUrl = serverUrl;

        context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(SERVER_URL_SHARED_PREF_KEY, serverUrl)
                .apply();

        initSpaceRocketApi();
    }

    private void initSpaceRocketApi() {
        spaceRocketApi = new Retrofit.Builder()
                .baseUrl(serverUrl)
                .addConverterFactory(MoshiConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build().create(SpaceRocketApi.class);
    }

    public Observable<Boolean> health() {
        return spaceRocketApi.health()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(health -> {
                    if ("alive".equals(health.status)) {
                        return Observable.just(Boolean.TRUE);
                    } else {
                        return Observable.just(Boolean.FALSE);
                    }
                });
    }

    public Observable<Start> start(final Player player) {
        final Gamer gamer = new Gamer();
        gamer.gamerFirstname = player.getFirstName();
        gamer.gamerLastname = player.getFirstName();
        gamer.gamerEmail = player.getEmail();
        gamer.gamerCompany = player.getCompany();
        gamer.gamerTwitter = player.getTwitter();
        gamer.gamerGenderMale = player.isGenderMale();

        return spaceRocketApi.start(gamer)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

}
