package smtlab.cse.varnamtutor;

import java.util.ArrayList;
import java.util.HashMap;

public class SongRecord {
    private int songId;
    private String songName;
    private ArrayList<String> segmentNames;
    private HashMap<String,String> segmentMap;
    private int tonic;
    private String raga;

    public void setRaga(String raga) {
        this.raga = raga;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public ArrayList<String> getSegmentNames() {
        return segmentNames;
    }

    public void setSegmentNames(ArrayList<String> segmentNames) {
        this.segmentNames = segmentNames;
    }

    public HashMap<String, String> getSegmentMap() {
        return segmentMap;
    }

    public void setSegmentMap(HashMap<String, String> segmentMap) {
        this.segmentMap = segmentMap;
    }

    public int getTonic() {
        return tonic;
    }

    public void setTonic(int tonic) {
        this.tonic = tonic;
    }

    public int getSongId() {
        return songId;
    }

    public void setSongId(int songId) {
        this.songId = songId;
    }
}
