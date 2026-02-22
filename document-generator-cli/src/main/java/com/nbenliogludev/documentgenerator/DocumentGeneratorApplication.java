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
        System.out.println("Starting Document Generator CLI...");

        GeneratorConfig config = GeneratorConfig.load(args);

        System.out.println("====== Configuration ======");
        System.out.println("Base URL:      " + config.getBaseUrl());
        System.out.println("Total Count:   " + config.getCount());
        System.out.println("Delay (ms):    " + config.getDelayMs());
        System.out.println("Author Prefix: " + config.getAuthorPrefix());
        System.out.println("Title Prefix:  " + config.getTitlePrefix());
        System.out.println("Number Prefix: " + config.getNumberPrefix());
        System.out.println("===========================\n");

        if (config.getCount() <= 0) {
            System.err.println("Count must be greater than 0. Exiting.");
            return;
        }

        DocumentApiClient apiClient = new DocumentApiClient(config);

        AtomicInteger successCounter = new AtomicInteger();
        AtomicInteger errorCounter = new AtomicInteger();

        long startTime = System.currentTimeMillis();

        for (int i = 1; i <= config.getCount(); i++) {
            String title = config.getTitlePrefix() + " #" + i;
            String author = config.getAuthorPrefix() + "-" + i;

            System.out.print(String.format("[%d/%d] Generating document '%s'... ", i, config.getCount(), title));

            boolean success = apiClient.createDocument(title, author);

            if (success) {
                System.out.println("SUCCESS");
                successCounter.incrementAndGet();
            } else {
                System.out.println("FAILED");
                errorCounter.incrementAndGet();
            }

            // Sleep if configured
            if (config.getDelayMs() > 0 && i < config.getCount()) {
                try {
                    TimeUnit.MILLISECONDS.sleep(config.getDelayMs());
                } catch (InterruptedException e) {
                    System.err.println("\nGenerator interrupted!");
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        long endTime = System.currentTimeMillis();
        long elapsedMs = endTime - startTime;

        System.out.println("\n====== Generation Summary ======");
        System.out.println("Total Attempts: " + config.getCount());
        System.out.println("Successful:     " + successCounter.get());
        System.out.println("Failed:         " + errorCounter.get());
        System.out.println(String.format("Elapsed Time:   %d.%03d s", elapsedMs / 1000, elapsedMs % 1000));
        System.out.println("================================\n");
    }
}
