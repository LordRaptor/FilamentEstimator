package lu.unreal.filamentestimator;

import com.google.common.base.MoreObjects;

public class Layer {

    private final int layerNumber;
    private double filamentExtruded;

    private int colorNumber;

    public Layer(int layerNumber, int colorNumber) {
        this.layerNumber = layerNumber;
        this.colorNumber = colorNumber;
    }

    public int getColorNumber() {
        return colorNumber;
    }

    public void setColorNumber(int colorNumber) {
        this.colorNumber = colorNumber;
    }

    public int getLayerNumber() {
        return layerNumber;
    }

    public double getFilamentExtruded() {
        return filamentExtruded;
    }

    public void addFilamentExtruded(double extrusion) {
        filamentExtruded += extrusion;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("layerNumber", layerNumber)
                .add("filamentExtruded", filamentExtruded)
                .add("colorNumber", colorNumber)
                .toString();
    }
}
