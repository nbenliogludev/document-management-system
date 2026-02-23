package com.nbenliogludev.documentgenerator;

import com.nbenliogludev.documentgenerator.client.DocumentApiClient;
import com.nbenliogludev.documentgenerator.config.GeneratorConfig;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Main application for the Document Generator CLI Tool.
 */
public class DocumentGeneratorApplication {

    public static void main(String[] args) {
        GeneratorConfig config = GeneratorConfig.load(args);
        System.out.println(
                String.format("[generator] start: count=%d, baseUrl=%s", config.getCount(), config.getBaseUrl()));

        if (config.getCount() <= 0) {
            System.err.println("[generator] Count must be greater than 0. Exiting.");
            return;
        }

        DocumentApiClient apiClient = new DocumentApiClient(config);

        AtomicInteger successCounter = new AtomicInteger();
        AtomicInteger errorCounter = new AtomicInteger();

        long startTime = System.currentTimeMillis();

        for (int i = 1; i <= config.getCount(); i++) {
            String title = config.getTitlePrefix() + " #" + i;
            String author = config.getAuthorPrefix() + "-" + i;

            System.out.println(String.format("[generator] creating document %d/%d", i, config.getCount()));

            boolean success = apiClient.createDocument(title, author);

            if (success) {
                System.out.println(
                        String.format("[generator] created document %d/%d title='%s'", i, config.getCount(), title));
                successCounter.incrementAndGet();
            } else {
                System.err.println(
                        String.format("[generator] failed document %d/%d title='%s'", i, config.getCount(), title));
                errorCounter.incrementAndGet();
            }

            // Sleep if configured
            if (config.getDelayMs() > 0 && i < config.getCount()) {
                try {
                    TimeUnit.MILLISECONDS.sleep(config.getDelayMs());
                } catch (InterruptedException e) {
                    System.err.println("\n[generator] interrupted!");
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        long endTime = System.currentTimeMillis();
        long elapsedMs = endTime - startTime;

        System.out.println(String.format("[generator] finished: requested=%d, success=%d, failed=%d, tookMs=%d",
                config.getCount(), successCounter.get(), errorCounter.get(), elapsedMs));
    }
}
