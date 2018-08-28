package fr.sqli.tintinspacerocketcontrolapp.simon.ex;

public class PlayerAlreadyPlayedException extends Exception {
    public int gamerId;

    public PlayerAlreadyPlayedException(int gamerId) {
        super();
        this.gamerId = gamerId;
    }
}
