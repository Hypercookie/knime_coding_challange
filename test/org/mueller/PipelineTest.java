package org.mueller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class PipelineTest {
    /**
     * Ensures correct order of pipeline steps
     */
    @org.junit.jupiter.api.RepeatedTest(1000)
    void addPipelineStep() {
        String s1 = randomString(10);
        String s2 = randomString(10);
        Pipeline<String> p = new Pipeline<>();
        p.addPipelineStep(s -> "");
        assert "".equals(p.computeForElement(s1));
        p.addPipelineStep(x -> s2);
        assert s2.equals(p.computeForElement(s1));
    }

    /**
     * Ensures correct order of appended pipelines
     */
    @org.junit.jupiter.api.RepeatedTest(1000)
    void appendPipeline() {
        String s1 = randomString(10);
        String s2 = randomString(10);

        Pipeline<String> p1 = new Pipeline<>();
        p1.addPipelineStep(s -> "");
        assert "".equals(p1.computeForElement(s1));

        Pipeline<String> p2 = new Pipeline<>();
        p2.addPipelineStep(s -> s2);
        assert s2.equals(p2.computeForElement(s1));

        Pipeline<String> combined = p1.appendPipeline(p2);
        assert s2.equals(combined.computeForElement(s1));
    }

    /**
     * Ensures correct computation for single element
     */
    @org.junit.jupiter.api.RepeatedTest(1000)
    void computeForElement() {
        String s1 = randomString(10);
        String s2 = randomString(10);

        Pipeline<String> p = new Pipeline<>();
        p.addPipelineStep(v -> s2);
        assert s2.equals(p.computeForElement(s1));
    }

    /**
     * Ensures correct sequential computation for stream with 10 threads.
     */
    @org.junit.jupiter.api.Test
    void computeForStream() {
        Stream<String> s = Stream.generate(() -> randomString(10)).limit(1000);
        ArrayList<String> elements = new ArrayList<>();
        s = s.sequential().peek(elements::add);
        Pipeline<String> p = Pipeline.REVERSE_STRING;
        s = p.computeForStream(s, 10);
        ArrayList<String> result = s.collect(Collectors.toCollection(ArrayList::new));
        for (int i = 0; i < elements.size(); i++) {
            assert p.computeForElement(elements.get(i)).equals(result.get(i));
        }
    }

    /**
     * Ensures correct sequential computation for stream with 10 threads and view.
     */
    @org.junit.jupiter.api.Test
    void computeForStreamWithView() {
        Stream<String> s = Stream.generate(() -> new Random().nextInt(10000)).map(String::valueOf).limit(1000);
        ArrayList<String> elements = new ArrayList<>();
        s = s.sequential().peek(elements::add);
        Pipeline<Integer> p = Pipeline.REVERSE_NUMBER;
        s = p.computeForStreamWithView(s, Integer::parseInt, String::valueOf, 10);
        ArrayList<String> result = s.collect(Collectors.toCollection(ArrayList::new));
        for (int i = 0; i < elements.size(); i++) {
            assert String.valueOf(p.computeForElement(Integer.parseInt(elements.get(i)))).equals(result.get(i));
        }
    }

    /**
     * Ensures that a file is correctly and sequentially read, and the output is correctly piped.
     */
    @org.junit.jupiter.api.Test
    void testFromFileInput(){
        Stream<String> s = Stream.generate(() -> new Random().nextInt(10000)).map(String::valueOf).limit(1000);
        ArrayList<String> l = new ArrayList<>();
        try(BufferedWriter wr =  new BufferedWriter(new FileWriter(".tmp.txt"))){
            s.forEach(x->{
                try {
                    l.add(x);
                    wr.write(x);
                    wr.newLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            wr.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            Stream<String> s2 = Utils.readFileAsStream(".tmp.txt");
            ArrayList<String> l2 = new ArrayList<>();
            Pipeline<String> p = new Pipeline<>();
            p.addPipelineStep(x->{
                l2.add(x);
                return x;
            });
            p.appendPipeline(Pipeline.REVERSE_STRING);
            s2 = p.computeForStream(s2,10);
            List<String> s3 = s2.collect(Collectors.toList());
            for (int i = 0; i < s3.size(); i++) {
                assert l.get(i).equals(l2.get(i));
                assert s3.get(i).equals(Pipeline.REVERSE_STRING.computeForElement(l2.get(i)));
            }
            new File(".tmp.txt").deleteOnExit();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    /**
     * Returns a random string.
     * @param l length
     * @return random string of length l
     */
    static String randomString(int l) {
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvxyz0123456789";
        StringBuilder s = new StringBuilder(l);
        int i;
        for (i = 0; i < l; i++) {
            int ch = (int) (AlphaNumericString.length() * Math.random());
            s.append(AlphaNumericString.charAt(ch));
        }
        return s.toString();
    }
}