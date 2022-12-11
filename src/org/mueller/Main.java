package org.mueller;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.stream.Stream;

/**
 * Main class.
 *
 * @author KNIME GmbH
 */
public class Main {

    public static void main(String[] args) throws IOException {
        // add your code here
        if (args.length < 8) {
            throw new RuntimeException("Please provide all required arguments");
        }
        String fileName = args[1];
        String inputType = args[3];
        String[] operations = args[5].split(",");
        int threadCount = Integer.parseInt(args[7]);


        //Get the stream of lines
        Stream<String> lines = Utils.readFileAsStream(fileName);
        //Add stats and
        //Compute pipeline
        lines = (switch (inputType) {
            case "string" -> Pipeline.buildStringPipeline(operations)
                    .computeForStream(lines, threadCount);
            //The following statements use a view of the stream to make further processing simpler.
            case "int" -> Pipeline.buildIntegerPipeline(operations)
                    .computeForStreamWithView(lines, Integer::parseInt, String::valueOf, threadCount);
            case "double" -> Pipeline.buildDoublePipeline(operations)
                    .computeForStreamWithView(lines, Double::parseDouble, String::valueOf, threadCount);
            default -> lines;
        }).peek(v -> Statistics.getInstance().updateStatisticsWithLine(v));

        //either print to standard out or to file
        if (args.length == 8) {
            lines.forEach(System.out::println);
        } else {
            String outputFileName = args[9];
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName));
            lines.forEach(x -> {
                try {
                    writer.write(x);
                    writer.newLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            writer.flush();
        }
        // DO NOT CHANGE THE FOLLOWING LINES OF CODE
        System.out.println(String.format("Processed %d lines (%d of which were unique)", //
                Statistics.getInstance().getNoOfLinesRead(), //
                Statistics.getInstance().getNoOfUniqueLines()));
    }

}