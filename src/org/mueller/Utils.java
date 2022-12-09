package org.mueller;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.stream.Stream;

public abstract class Utils {
    public static Stream<String> readFileAsStream(String fileName) throws IOException {
        BufferedReader bf = new BufferedReader(new FileReader(fileName));
        return bf.lines();
    }
}
