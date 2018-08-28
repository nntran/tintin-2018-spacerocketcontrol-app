package fr.sqli.tintinspacerocketcontrolapp.pojos;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import fr.sqli.tintinspacerocketcontrolapp.simon.pojos.Colors;

public class CurrentTry {
    public List<Colors> tryingSequence = new LinkedList<>();
    public Date startDate;
    public boolean isPlayerTrying;
    public long time;
    public List<Colors> correctSequence = new LinkedList<>();
    public int remainingAttemps;
}
