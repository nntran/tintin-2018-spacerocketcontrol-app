package fr.sqli.tintinspacerocketcontrolapp.service;

public enum Colors {
    BLUE("2"),
    YELLOW("1"),
    RED("3"),
    GREEN("0");

    public final String code;


    Colors(final String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return this.code;
    }
}
