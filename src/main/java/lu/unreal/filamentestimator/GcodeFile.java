package lu.unreal.filamentestimator;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

public class GcodeFile {
    private double filamentDensity;
    private double filamentDiameter;
    private String filamentType;

    private List<Layer> layers = new ArrayList<>();

    public double getFilamentDensity() {
        return filamentDensity;
    }

    public void setFilamentDensity(double filamentDensity) {
        this.filamentDensity = filamentDensity;
    }

    public double getFilamentDiameter() {
        return filamentDiameter;
    }

    public void setFilamentDiameter(double filamentDiameter) {
        this.filamentDiameter = filamentDiameter;
    }

    public String getFilamentType() {
        return filamentType;
    }

    public void setFilamentType(String filamentType) {
        this.filamentType = filamentType;
    }

    public List<Layer> getLayers() {
        return layers;
    }

    public void setLayers(List<Layer> layers) {
        this.layers = layers;
    }

    public double convertToWeight(double extrusion) {
        double volume = (Math.PI * Math.pow(filamentDiameter / 2.0, 2) * extrusion);

        return (volume / 1000.0) * filamentDensity;
    }
}
