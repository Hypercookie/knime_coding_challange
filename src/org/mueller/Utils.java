package org.mueller;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.stream.Stream;

public abstract class Utils {
    /**
     * Reads in a file and returns a stream which contains the lines of the file in sequential order
     * @param fileName the location of the file
     * @return a stream containing all the lines
     * @throws IOException if file does not exist or can not be read
     */
    public static Stream<String> readFileAsStream(String fileName) throws IOException {
        BufferedReader bf = new BufferedReader(new FileReader(fileName));
        return bf.lines();
    }
}
