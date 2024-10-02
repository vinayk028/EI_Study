package com.patterns.creational.builder;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocumentGenerationSystem {
    private static final Logger logger = LoggerFactory.getLogger(DocumentGenerationSystem.class);

    public static void main(String[] args) {
        try {
            DocumentDirector director = new DocumentDirector();

            // Creating a legal contract
            Document legalContract = director.constructLegalContract(
                new DocumentBuilder()
                    .setTitle("Service Agreement")
                    .setClient("Acme Corp")
                    .setDate(new Date())
            );
            System.out.println("Generated Legal Contract:\n" + legalContract);

            // Creating a technical specification
            Document technicalSpec = director.constructTechnicalSpec(
                new DocumentBuilder()
                    .setTitle("System Architecture Specification")
                    .setAuthor("John Doe")
                    .setDate(new Date())
            );
            System.out.println("\nGenerated Technical Specification:\n" + technicalSpec);

        } catch (Exception e) {
            logger.error("Error in document generation", e);
            System.out.println("Failed to generate documents: " + e.getMessage());
        }
    }
}

class Document {
    private final String title;
    private final String author;
    private final String client;
    private final Date date;
    private final List<String> sections;
    private final Map<String, String> metadata;
    private final List<String> footers;

    private Document(DocumentBuilder builder) {
        this.title = builder.title;
        this.author = builder.author;
        this.client = builder.client;
        this.date = builder.date;
        this.sections = new ArrayList<>(builder.sections);
        this.metadata = new HashMap<>(builder.metadata);
        this.footers = new ArrayList<>(builder.footers);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ").append(title).append(" ===\n");
        if (author != null) sb.append("Author: ").append(author).append("\n");
        if (client != null) sb.append("Client: ").append(client).append("\n");
        sb.append("Date: ").append(date).append("\n\n");

        for (String section : sections) {
            sb.append(section).append("\n\n");
        }

        if (!metadata.isEmpty()) {
            sb.append("Metadata:\n");
            metadata.forEach((key, value) -> sb.append("  ").append(key).append(": ").append(value).append("\n"));
        }

        for (String footer : footers) {
            sb.append("\n").append(footer);
        }

        return sb.toString();
    }

    static class DocumentBuilder {
        private String title;
        private String author;
        private String client;
        private Date date;
        private final List<String> sections = new ArrayList<>();
        private final Map<String, String> metadata = new HashMap<>();
        private final List<String> footers = new ArrayList<>();

        private static final Logger logger = LoggerFactory.getLogger(DocumentBuilder.class);

        public DocumentBuilder setTitle(String title) {
            this.title = Objects.requireNonNull(title, "Title cannot be null");
            return this;
        }

        public DocumentBuilder setAuthor(String author) {
            this.author = author;
            return this;
        }

        public DocumentBuilder setClient(String client) {
            this.client = client;
            return this;
        }

        public DocumentBuilder setDate(Date date) {
            this.date = new Date(date.getTime()); // Defensive copy
            return this;
        }

        public DocumentBuilder addSection(String section) {
            sections.add(Objects.requireNonNull(section, "Section cannot be null"));
            return this;
        }

        public DocumentBuilder addMetadata(String key, String value) {
            metadata.put(
                Objects.requireNonNull(key, "Metadata key cannot be null"),
                Objects.requireNonNull(value, "Metadata value cannot be null")
            );
            return this;
        }

        public DocumentBuilder addFooter(String footer) {
            footers.add(Objects.requireNonNull(footer, "Footer cannot be null"));
            return this;
        }

        public Document build() {
            validateDocument();
            logger.info("Building document: {}", title);
            return new Document(this);
        }

        private void validateDocument() {
            if (title == null || title.trim().isEmpty()) {
                throw new IllegalStateException("Document must have a title");
            }
            if (date == null) {
                logger.warn("Document date not set, using current date");
                date = new Date();
            }
        }
    }
}

class DocumentDirector {
    private static final Logger logger = LoggerFactory.getLogger(DocumentDirector.class);

    public Document constructLegalContract(DocumentBuilder builder) {
        logger.info("Constructing legal contract");
        return builder
            .addSection("1. Parties\nThis agreement is between the Client and the Service Provider.")
            .addSection("2. Services\nThe Service Provider agrees to provide the following services...")
            .addSection("3. Terms\nThe term of this agreement shall be...")
            .addMetadata("Document Type", "Legal Contract")
            .addMetadata("Version", "1.0")
            .addFooter("This document is legally binding")
            .addFooter("Page 1 of 1")
            .build();
    }

    public Document constructTechnicalSpec(DocumentBuilder builder) {
        logger.info("Constructing technical specification");
        return builder
            .addSection("1. Overview\nThis document outlines the technical specifications...")
            .addSection("2. Architecture\nThe system architecture consists of...")
            .addSection("3. Components\nKey components include...")
            .addMetadata("Document Type", "Technical Specification")
            .addMetadata("Status", "Draft")
            .addFooter("Confidential and Proprietary")
            .build();
    }
}
