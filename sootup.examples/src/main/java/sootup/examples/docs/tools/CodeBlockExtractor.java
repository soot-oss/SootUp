package sootup.examples.docs.tools;

import java.io.*;
import java.nio.file.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CodeBlockExtractor {

    private static final Pattern INCLUDE_PATTERN =
            Pattern.compile("\\{\\{include:([^:]+):([^}]+)\\}\\}");

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java CodeBlockExtractor <input-dir> <output-dir>");
            System.err.println("Example: java CodeBlockExtractor docs-src docs");
            System.exit(1);
        }

        String inputDir = args[0];
        String outputDir = args[1];

        try {
            processMarkdownFiles(Paths.get(inputDir), Paths.get(outputDir));
            System.out.println("Documentation processing completed successfully.");
        } catch (IOException e) {
            System.err.println("Error processing files: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void processMarkdownFiles(Path inputDir, Path outputDir) throws IOException {
        // Create output directory if it doesn't exist
        Files.createDirectories(outputDir);

        // Process all .md files in the input directory recursively
        Files.walk(inputDir)
                .filter(path -> path.toString().endsWith(".md"))
                .forEach(path -> {
                    try {
                        processMarkdownFile(path, inputDir, outputDir);
                    } catch (IOException e) {
                        System.err.println("Error processing " + path + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                });
    }

    private static void processMarkdownFile(Path inputFile, Path inputDir, Path outputDir)
            throws IOException {

        // Read the markdown file
        String content = Files.readString(inputFile);

        // Process includes relative to the project root (parent of docs directory)
        Path basePath = inputDir.getParent();
        String processedContent = processIncludes(content, basePath);

        // Calculate relative path and create output file
        Path relativePath = inputDir.relativize(inputFile);
        Path outputFile = outputDir.resolve(relativePath);
        Files.createDirectories(outputFile.getParent());

        // Write processed content
        Files.writeString(outputFile, processedContent);

        System.out.println("Processed: " + relativePath);
    }

    private static String processIncludes(String content, Path basePath) {
        Matcher matcher = INCLUDE_PATTERN.matcher(content);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String filePath = matcher.group(1);
            String blockName = matcher.group(2);

            try {
                Path sourceFile = basePath.resolve(filePath);
                String codeBlock = extractCodeBlock(sourceFile, blockName);

                if (codeBlock != null) {
                    // Create a clean code block with Java syntax highlighting
                    String replacement = "```java\n" + codeBlock + "\n```";
                    matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
                } else {
                    System.err.println("Warning: Block '" + blockName + "' not found in " + filePath);
                    String replacement = "```java\n// Block '" + blockName + "' not found in " + filePath + "\n```";
                    matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
                }
            } catch (IOException e) {
                System.err.println("Error reading file " + filePath + ": " + e.getMessage());
                String replacement = "```java\n// Error reading file " + filePath + ": " + e.getMessage() + "\n```";
                matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
            }
        }

        matcher.appendTail(result);
        return result.toString();
    }

    private static String extractCodeBlock(Path filePath, String blockName) throws IOException {
        if (!Files.exists(filePath)) {
            throw new IOException("File not found: " + filePath);
        }

        String content = Files.readString(filePath);

        // Find start and end markers
        String startMarker = "// " + blockName + " {";
        String endMarker = "// } " + blockName;

        int startIndex = content.indexOf(startMarker);
        if (startIndex == -1) {
            return null; // Block not found
        }

        // Move to the next line after start marker
        startIndex = content.indexOf('\n', startIndex);
        if (startIndex == -1) {
            return null;
        }
        startIndex++; // Move past the newline

        int endIndex = content.indexOf(endMarker, startIndex);
        if (endIndex == -1) {
            return null; // End marker not found
        }

        // Extract the block content
        String block = content.substring(startIndex, endIndex);

        // Clean up the block (remove common indentation and trim)
        return cleanupCodeBlock(block);
    }

    private static String cleanupCodeBlock(String block) {
        if (block == null || block.trim().isEmpty()) {
            return "";
        }

        String[] lines = block.split("\n");

        // Remove empty lines at start and end
        int start = 0;
        int end = lines.length - 1;

        while (start <= end && lines[start].trim().isEmpty()) {
            start++;
        }

        while (end >= start && lines[end].trim().isEmpty()) {
            end--;
        }

        if (start > end) {
            return "";
        }

        // Find minimum indentation among non-empty lines
        int minIndent = Integer.MAX_VALUE;
        for (int i = start; i <= end; i++) {
            String line = lines[i];
            if (!line.trim().isEmpty()) {
                int indent = 0;
                for (char c : line.toCharArray()) {
                    if (c == ' ') {
                        indent++;
                    } else if (c == '\t') {
                        indent += 4; // Treat tab as 4 spaces
                    } else {
                        break;
                    }
                }
                minIndent = Math.min(minIndent, indent);
            }
        }

        // Handle case where all lines are empty (shouldn't happen after trimming)
        if (minIndent == Integer.MAX_VALUE) {
            minIndent = 0;
        }

        // Remove common indentation and build result
        StringBuilder result = new StringBuilder();
        for (int i = start; i <= end; i++) {
            String line = lines[i];
            if (line.trim().isEmpty()) {
                result.append("\n");
            } else {
                // Remove common indentation
                if (line.length() > minIndent) {
                    result.append(line.substring(minIndent));
                } else {
                    result.append(line);
                }
                result.append("\n");
            }
        }

        return result.toString().trim();
    }
}