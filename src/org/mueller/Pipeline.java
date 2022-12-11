package org.mueller;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * This class provides a pipeline to process elements. The input and output of the pipeline can be controlled via the generic parameter T
 *
 * @param <T>
 * @author Jannes Mueller
 */
public class Pipeline<T> {


    //Some pre-made pipelines.
    public static Pipeline<String> CAPITALIZE = new Pipeline<String>().addPipelineStep(String::toUpperCase);
    public static Pipeline<String> REVERSE_STRING =
            new Pipeline<String>().addPipelineStep(x -> new StringBuilder().append(x).reverse().toString());

    public static Pipeline<Integer> REVERSE_NUMBER =
            new Pipeline<Integer>().addPipelineStep(x -> {
                String i = String.valueOf(x);
                if (x < 0) {
                    return Integer.parseInt("-" + REVERSE_STRING.computeForElement(i.substring(1)));
                } else {
                    return Integer.parseInt(REVERSE_STRING.computeForElement(i));
                }
            });

    /**
     * This method generates a string processing pipeline out of operations that shall be performed on the stream of elements
     *
     * @param operations a list of operations
     * @return a pipeline performing these operations or nothing if an operation is not implemented
     */
    public static Pipeline<String> buildStringPipeline(String[] operations) {
        Pipeline<String> pipe = new Pipeline<>();
        for (String operation : operations) {
            pipe.appendPipeline(
                    switch (operation) {
                        case "reverse" -> REVERSE_STRING;
                        case "capitalize" -> CAPITALIZE;
                        default -> new Pipeline<>();
                    });
        }
        return pipe;
    }

    /**
     * This method generates an integer processing pipeline out of operations that shall be performed on the stream of elements
     *
     * @param operations a list of operations
     * @return a pipeline performing these operations or nothing if an operation is not implemented
     */
    public static Pipeline<Integer> buildIntegerPipeline(String[] operations) {
        Pipeline<Integer> pipe = new Pipeline<>();
        for (String operation : operations) {
            pipe.appendPipeline(
                    switch (operation) {
                        case "reverse" -> REVERSE_NUMBER;
                        case "neg" -> new Pipeline<Integer>().addPipelineStep(v -> -v);
                        default -> new Pipeline<>();
                    }
            );
        }
        return pipe;
    }

    /**
     * This method generates a double processing pipeline out of operations that shall be performed on the stream of elements
     *
     * @param operations a list of operations
     * @return a pipeline performing these operations or nothing if an operation is not implemented
     */
    public static Pipeline<Double> buildDoublePipeline(String[] operations) {
        Pipeline<Double> pipe = new Pipeline<>();
        for (String operation : operations) {
            pipe.appendPipeline(
                    // Usually these are not enough statements for a switch statement to be applicable.
                    // Since the set of possible operations is likely to grow in the future, it shall still be a switch
                    // statement to make extensions easier to develop
                    switch (operation) {
                        case "neg" -> new Pipeline<Double>().addPipelineStep(v -> -v);
                        default -> new Pipeline<>();
                    }
            );
        }
        return pipe;
    }

    /**
     * stores the pipeline as an unevaluated function
     */
    private Function<T, T> m_mapper = Function.identity();

    /**
     * This method adds a step to the pipeline. This step will be executed after all previous steps.
     *
     * @param step a function that shall be computed on the elements
     * @return the pipeline with the new step
     */
    public Pipeline<T> addPipelineStep(Function<T, T> step) {
        m_mapper = m_mapper.andThen(step);
        return this;
    }

    /**
     * Append an entire pipeline to the current one. This will execute the steps defined in the second pipeline after
     * the previously defined steps.
     *
     * @param pipeline the pipeline to append
     * @return the pipeline with the new pipeline at the end
     */
    public Pipeline<T> appendPipeline(Pipeline<T> pipeline) {
        this.addPipelineStep(pipeline.m_mapper);
        return this;
    }

    /**
     * Computes the pipeline of a single element.
     *
     * @param element the element to compute the pipeline for
     * @return the result of the pipeline
     */
    public T computeForElement(T element) {
        return this.m_mapper.apply(element);
    }

    /**
     * Computes the pipeline for a stream of elements. This method uses a {@link ForkJoinPool} to archive parallelism.
     *
     * @param s           the stream to process
     * @param threadCount the amount of threads used to process this stream
     * @return a stream consisting of processed elements.
     */
    public Stream<T> computeForStream(Stream<T> s, int threadCount) {
        ForkJoinPool fjp = new ForkJoinPool(threadCount);
        //The stream is first mapped to completable futures, and then those are awaited. Since the stream is sequential,
        // there is no loss of order.
        return s.sequential()
                .map(l -> CompletableFuture.supplyAsync(() -> this.computeForElement(l), fjp))
                .map(CompletableFuture::join);
    }

    /**
     * Utility function to compute this pipeline for streams of type {@link String} (e.g. reading in lines).
     * This function will map the elements of the stream to the desired type (the type of this pipeline) by using the first supplied function.
     * It will then execute the pipeline in parallel and map the results back to a {@link String}.
     *
     * @param s           The stream of strings
     * @param from        a function that converts the string to a type that the current pipeline can process
     * @param to          a function that converts the element from the type the pipeline handles back to a string
     * @param threadCount the amount of threads used to process this stream
     * @return a stream consisting of processed elements.
     */
    public Stream<String> computeForStreamWithView(Stream<String> s, Function<String, T> from, Function<T, String> to,
                                                   int threadCount) {
        return this.computeForStream(s.map(from), threadCount).map(to);
    }
}
