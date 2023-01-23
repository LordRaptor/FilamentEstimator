package lu.unreal.filamentestimator;

import com.google.common.util.concurrent.AtomicDouble;
import com.opencsv.CSVWriter;
import lu.unreal.filamentestimator.GCodeParser.Line;
import lu.unreal.filamentestimator.GCodeParser.Line.Type;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.OptionalDouble;
import java.util.SortedMap;
import java.util.TreeMap;

public class Estimator {
    public static void main(String[] args) throws IOException {
        Path gcodeFile = Paths.get("E:\\My Files\\Projects\\3D Printing\\SP8 Coup Perdu Clock\\Sliced\\Frame",
                "frame-dial-roman-3-colors_0.2mm_PLA_MK3S_7h34m.gcode");
        List<Line> lines = GCodeParser.parseFile(gcodeFile);
        GcodeFile parsedFile = Estimator.parseLayers(lines);

        printCsvOutput(parsedFile);

    }

    public static GcodeFile parseLayers(List<Line> lines) {
        GcodeFile gcodeFile = new GcodeFile();

        int colorNumber = 1;
        int layerNumber = 0;
        Layer currentLayer = new Layer(layerNumber, colorNumber);
        gcodeFile.getLayers().add(currentLayer);

        double retraction = 0;

        for (Line l : lines) {
            if (l.getType() == Type.COMMENT) {
                if (l.getComment().startsWith("LAYER:")) {
                    //Layer change
                    currentLayer = new Layer(++layerNumber, colorNumber);
                    gcodeFile.getLayers().add(currentLayer);
                } else if (l.getComment().startsWith("filament_density = ")) {
                    gcodeFile.setFilamentDensity(Double.parseDouble(l.getComment().substring("filament_density = ".length())));
                } else if (l.getComment().startsWith("filament_diameter = ")) {
                    gcodeFile.setFilamentDiameter(Double.parseDouble(l.getComment().substring("filament_diameter = ".length())));
                } else if (l.getComment().startsWith("filament_type = ")) {
                    gcodeFile.setFilamentType(l.getComment().substring("filament_type = ".length()));
                }

            } else if (l.getType() == Type.GCODE && l.getCommand().equals("M600")) {
                currentLayer.setColorNumber(++colorNumber);
            } else if (l.getType() == Type.GCODE && l.getCommand().equals("G1")) {
                OptionalDouble extrusion = parseG1(l);
                if (extrusion.isPresent()) {
                    double value = extrusion.getAsDouble();
                    retraction += value;
                    if (retraction > 0) {
                        currentLayer.addFilamentExtruded(retraction);
                        retraction = 0;
                    }
                }
            }
        }

        return gcodeFile;
    }

    private static OptionalDouble parseG1(Line l) {
        for (String p : l.getParameters()) {
            if (p.startsWith("E")) {
                String extrusion = p.substring(1);
                if (extrusion.startsWith(".")) {
                    extrusion = "0" + extrusion;
                } else if (extrusion.startsWith("-.")) {
                    extrusion = "-0" + extrusion.substring(1);
                }
                return OptionalDouble.of(Double.parseDouble(extrusion));
            }
        }
        return OptionalDouble.empty();
    }

    public static void printCsvOutput(GcodeFile gcodeFile) {
        StringWriter stringWriter = new StringWriter();
        try (CSVWriter writer = new CSVWriter(stringWriter)) {
            writer.writeNext(new String[] {"Filament Type", gcodeFile.getFilamentType()});
            writer.writeNext(new String[]{"Filament Diameter [mm]", Double.toString(gcodeFile.getFilamentDiameter())});
            writer.writeNext(new String[]{"Filament Density [g/cm3]", Double.toString(gcodeFile.getFilamentDensity())});
            writer.writeNext(new String[]{"Total Layer Count", Integer.toString(gcodeFile.getLayers().size())});

            //Summaries
            SortedMap<Integer, AtomicDouble> totalPerColor = new TreeMap<>();
            for (Layer l : gcodeFile.getLayers()) {
                AtomicDouble atomicDouble = totalPerColor.computeIfAbsent(l.getColorNumber(), i -> new AtomicDouble());
                atomicDouble.addAndGet(l.getFilamentExtruded());
            }
            for (Entry<Integer, AtomicDouble> entry : totalPerColor.entrySet()) {
                double usage = gcodeFile.convertToWeight(entry.getValue().get());
                writer.writeNext(new String[]{"Color " + entry.getKey(), Double.toString(usage)});
            }


            //Details
            String[] line = new String[] {
                    "Layer",
                    "Color",
                    "Filament Used for layer [g]",
                    "Total Filament Used [g]",
                    "Total Filament Used Per Color [g]",
            };
            writer.writeNext(line);

            double totalFilament = 0;
            double runningFilament = 0;
            int currentColor = 0;
            for (Layer l : gcodeFile.getLayers()) {
                if (l.getColorNumber() != currentColor) {
                    runningFilament = 0;
                    currentColor = l.getColorNumber();
                }
                runningFilament += l.getFilamentExtruded();
                totalFilament += l.getFilamentExtruded();

                line[0] = Integer.toString(l.getLayerNumber());
                line[1] = Integer.toString(l.getColorNumber());
                line[2] = Double.toString(gcodeFile.convertToWeight(l.getFilamentExtruded()));
                line[3] = Double.toString(gcodeFile.convertToWeight(totalFilament));
                line[4] = Double.toString(gcodeFile.convertToWeight(runningFilament));

                writer.writeNext(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(stringWriter);
    }


}
