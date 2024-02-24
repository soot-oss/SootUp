package org.sootup.java.codepropertygraph.evaluation.output;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ResultAggregator {

  public static void generateSummaryForDirectory(String directoryPath) throws IOException {
    File dir = new File(directoryPath);
    File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));

    if (files == null) return;

    long totalDifferentEdges = 0, totalNumOfMethods = 0, totalSameEdges = 0;
    long totalMilliseconds = 0, totalSeconds = 0, totalMinutes = 0, totalHours = 0;
    double weightedSumPercentage = 0.0;
    long totalWeight = 0;

    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    for (File file : files) {
      JsonObject jsonObject = JsonParser.parseReader(new FileReader(file)).getAsJsonObject();

      if (!jsonObject.has("differentEdges")) continue;

      totalDifferentEdges += jsonObject.get("differentEdges").getAsLong();
      totalNumOfMethods += jsonObject.get("numOfMethods").getAsLong();
      totalSameEdges += jsonObject.get("sameEdges").getAsLong();
      long weight =
          jsonObject.get("differentEdges").getAsLong() + jsonObject.get("sameEdges").getAsLong();
      totalWeight += weight;
      String percentageStr = jsonObject.get("similarityPercentage").getAsString().replace(" %", "");
      double percentage = Double.parseDouble(percentageStr);
      weightedSumPercentage += percentage * weight;

      JsonObject elapsedTime = jsonObject.getAsJsonObject("elapsedTime");
      totalMilliseconds += elapsedTime.get("milliseconds").getAsLong();
      totalSeconds += elapsedTime.get("seconds").getAsLong();
      totalMinutes += elapsedTime.get("minutes").getAsLong();
      totalHours += elapsedTime.get("hours").getAsLong();
    }

    // Adjust elapsed time
    totalSeconds += totalMilliseconds / 1000;
    totalMilliseconds %= 1000;
    totalMinutes += totalSeconds / 60;
    totalSeconds %= 60;
    totalHours += totalMinutes / 60;
    totalMinutes %= 60;

    double averagePercentage = totalWeight > 0 ? weightedSumPercentage / totalWeight : 0;

    JsonObject summary = new JsonObject();
    summary.addProperty("totalDifferentEdges", totalDifferentEdges);
    summary.addProperty("totalNumOfMethods", totalNumOfMethods);
    summary.addProperty("totalSameEdges", totalSameEdges);
    summary.addProperty("averageSimilarityPercentage", String.format("%.4f %%", averagePercentage));

    JsonObject elapsedTimeSummary = new JsonObject();
    elapsedTimeSummary.addProperty("milliseconds", totalMilliseconds);
    elapsedTimeSummary.addProperty("seconds", totalSeconds);
    elapsedTimeSummary.addProperty("minutes", totalMinutes);
    elapsedTimeSummary.addProperty("hours", totalHours);
    summary.add("elapsedTime", elapsedTimeSummary);

    try (FileWriter file = new FileWriter(directoryPath + "/_results.json")) {
      gson.toJson(summary, file);
    }
  }
}
