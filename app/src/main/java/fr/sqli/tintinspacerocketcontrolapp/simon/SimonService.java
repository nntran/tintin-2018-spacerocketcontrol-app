package fr.sqli.tintinspacerocketcontrolapp.simon;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.LinkedList;
import java.util.List;

import fr.sqli.tintinspacerocketcontrolapp.player.Player;
import fr.sqli.tintinspacerocketcontrolapp.simon.api.Gamer;
import fr.sqli.tintinspacerocketcontrolapp.simon.api.Score;
import fr.sqli.tintinspacerocketcontrolapp.simon.api.SpaceRocketApi;
import fr.sqli.tintinspacerocketcontrolapp.simon.api.Start;
import fr.sqli.tintinspacerocketcontrolapp.simon.api.TryBody;
import fr.sqli.tintinspacerocketcontrolapp.simon.ex.GameFinishedException;
import fr.sqli.tintinspacerocketcontrolapp.simon.pojos.PlayResult;
import fr.sqli.tintinspacerocketcontrolapp.simon.pojos.Colors;
import fr.sqli.tintinspacerocketcontrolapp.simon.pojos.TryResult;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.moshi.MoshiConverterFactory;

/**
 * Singleton permettant d'appeler les APIs Rest de la Fusée
 */
public final class SimonService {

    private static SimonService INSTANCE;
    private SpaceRocketApi spaceRocketApi;
    private Context context;

    private static final String SHARED_PREF_NAME = "tintinspacerocket";
    private static final String SERVER_URL_SHARED_PREF_KEY = "server_url";

    private String serverUrl = "http://Android.local:8888/";

    private SimonService(final Context context) {
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
    public final static SimonService getInstance(final Context context) {
        if (INSTANCE == null) {
            INSTANCE = new SimonService(context);
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
        gamer.gamerContact = player.isContact();

        return spaceRocketApi.start(gamer)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<PlayResult> play(final Player player) {
        // TODO Gérer exception jeu terminé
        return spaceRocketApi.play(player.getId()).flatMap(play -> {
            final PlayResult playResult = new PlayResult();
            playResult.correctSequence = convertColorsCodesArrayToColorsList(play.sequence);
            playResult.remainingAttempts = play.remainingAttempts;

            return Observable.just(playResult);
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<TryResult> trySequence(final Player player, final List<Colors> sequenceTried, final long time) {
        final TryBody tryBody = new TryBody();
        tryBody.time = time;
        tryBody.sequence = convertLEDColorsListToArray(sequenceTried);

        // TODO Gérer exception jeu terminé
        return spaceRocketApi.trySequence(player.getId(), tryBody).flatMap(tryResponse -> {
            TryResult result = new TryResult();
            result.remainingAttempts = tryResponse.remainingAttempts;
            result.result = tryResponse.result;
            return Observable.just(result);
        }).onErrorResumeNext(throwable -> {
            if (throwable instanceof HttpException) {
                final HttpException httpException = (HttpException) throwable;
                if (httpException.code() == 403) {
                    return Observable.error(new GameFinishedException());
                } else {
                    return Observable.error(throwable);
                }
            } else {
                return Observable.error(throwable);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Score> getScore(final Player player) {
        return spaceRocketApi.getScore(player.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @NonNull
    private String[] convertLEDColorsListToArray(List<Colors> sequence) {
        String[] ledColors = new String[sequence.size()];
        for (int i = 0; i < sequence.size(); i++) {
            ledColors[i] = sequence.get(i).code;
        }
        return ledColors;
    }

    @NonNull
    private List<Colors> convertColorsCodesArrayToColorsList(String[] sequence) throws Exception {
        final List<Colors> colors = new LinkedList<>();

        for (int i= 0; i < sequence.length; i++) {
            Colors colorByCode = Colors.getByCode(sequence[i]);
            if (colorByCode == null) {
                throw new Exception("Color envoyée non reconnue !");
            }
            colors.add(colorByCode);
        }
        return colors;
    }

}
