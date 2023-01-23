package lu.unreal.filamentestimator;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import lu.unreal.filamentestimator.GCodeParser.Line.Type;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class GCodeParser {

    private static final char commentPrefix = ';';

    private static final Splitter SPLITTER = Splitter.on(CharMatcher.whitespace())
            .trimResults()
            .omitEmptyStrings();

    public static void main(String[] args) throws IOException {
        Path gcodeFile = Paths.get("E:\\My Files\\Projects\\3D Printing\\SP8 Coup Perdu Clock\\Sliced\\Misc", "shaft-collar_0.2mm_PLA_MK3S_9m.gcode");
        List<Line> lineStream = parseFile(gcodeFile);

        lineStream.forEach(System.out::println);
    }

    public static List<Line> parseFile(Path file) throws IOException {
        return Files.lines(file)
                .filter(l -> !Strings.isNullOrEmpty(l))
                .map(GCodeParser::parseLine)
                .toList();
    }

    public static Line parseLine(String line) {
        line = line.trim();
        if (line.isEmpty()) {
            throw new IllegalArgumentException("Empty line");
        }

        //Find comment
        int commentPrefixIndex = line.indexOf(commentPrefix);
        Line lineObject = new Line();

        if (commentPrefixIndex >= 0) {
            if (commentPrefixIndex + 1 < line.length()) {
                lineObject.comment = line.substring(commentPrefixIndex + 1).trim();
            }
            line = line.substring(0, commentPrefixIndex);

        }

        List<String> parts = SPLITTER.splitToList(line);

        if (parts.size() > 0) {
            lineObject.command = parts.get(0);
            lineObject.type = Type.GCODE;

            for (int i = 1; i < parts.size(); i++) {
                lineObject.parameters.add(parts.get(i));
            }

        } else {
            lineObject.type = Type.COMMENT;
        }

        return lineObject;
    }

    public static class Line {
        public enum Type {
            GCODE,
            COMMENT,
        }

        private Type type;
        private String command;
        private List<String> parameters = new ArrayList<>();
        private String comment = "";

        public Type getType() {
            return type;
        }

        public String getCommand() {
            return command;
        }

        public ImmutableList<String> getParameters() {
            return ImmutableList.copyOf(parameters);
        }

        public String getComment() {
            return comment;
        }

        @Override
        public String toString() {
            return switch (type) {
                case GCODE -> {
                    StringBuilder sb = new StringBuilder(command)
                            .append(" ");

                    parameters.forEach(p -> sb.append(p).append(" "));

                    if (!Strings.isNullOrEmpty(comment)) {
                        sb.append("; ").append(comment);
                    }
                    yield sb.toString();
                }
                case COMMENT -> "; " + comment;
            };
        }
    }
}
