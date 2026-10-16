package lk.susa.plant_lk.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
public class PlantCareTip implements Serializable {
    private int imageRes;
    private String title;
    private String description;

    @Setter
    private String treatments;
    @Setter
    private String pests;
    @Setter
    private String sideEffects;

    public PlantCareTip(int imageRes, String title, String description) {
        this.imageRes = imageRes;
        this.title = title;
        this.description = description;
    }

}