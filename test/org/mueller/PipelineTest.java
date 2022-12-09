package org.mueller;

import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class PipelineTest {

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

    @org.junit.jupiter.api.RepeatedTest(1000)
    void computeForElement() {
        String s1 = randomString(10);
        String s2 = randomString(10);

        Pipeline<String> p = new Pipeline<>();
        p.addPipelineStep(v -> s2);
        assert s2.equals(p.computeForElement(s1));
    }

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