package org.mueller;

import java.util.HashSet;
import java.util.Set;

/**
 * Captures statistics about the lines being read from the input file.
 *
 * @author KNIME GmbH
 */
public class Statistics {

    private final Set<String> m_linesRead = new HashSet<>();

    private int m_lineCounter;
    private static Statistics instance;

    /**
     * Updates statistics with respect to the given line. This method is supposed to
     * be called when a new line has been read from the input file.
     *
     * @param line A new line that has been read from the input file.
     */
    public void updateStatisticsWithLine(final String line) {
        m_lineCounter++;
        m_linesRead.add(line);
    }

    /**
     * @return the total number of lines read.
     */
    public int getNoOfLinesRead() {
        return m_lineCounter;
    }

    /**
     * @return the number of unique lines read.
     */
    public int getNoOfUniqueLines() {
        return m_linesRead.size();
    }

    /**
     * @return the shared {@link Statistics} instance to use.
     */
    public static Statistics getInstance() {
        if (Statistics.instance == null) {
            Statistics.instance = new Statistics();
        }
        return Statistics.instance;

    }
}