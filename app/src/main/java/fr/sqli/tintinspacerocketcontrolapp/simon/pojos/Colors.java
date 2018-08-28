package fr.sqli.tintinspacerocketcontrolapp.simon.pojos;

public enum Colors {
    BLUE(Codes.BLUE),
    YELLOW(Codes.YELLOW),
    RED(Codes.RED),
    GREEN(Codes.GREEN);

    public final String code;


    Colors(final String code) {
        this.code = code;
    }

    public static Colors getByCode(final String code) {
        Colors color = null;
        switch(code) {
            case Codes.BLUE:
                color = BLUE;
                break;
            case Codes.YELLOW:
                color = YELLOW;
                break;
            case Codes.RED:
                color = RED;
                break;
            case Codes.GREEN:
                color = GREEN;
                break;
        }
        return color;
    }

    @Override
    public String toString() {
        return this.code;
    }

    private static class Codes {
        public static final String GREEN = "0";
        public static final String RED = "3";
        public static final String YELLOW = "1";
        public static final String BLUE = "2";
    }
}
