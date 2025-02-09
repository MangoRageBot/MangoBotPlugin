package org.mangorage.mangobot.website;

import org.mangorage.basicutils.LogHelper;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FolderPruner {
    // Configurable Variables
    private static final String FOLDER_PATH = "webpage/uploads"; // Path to your folder
    private static final long MAX_SIZE_GB = 10; // Maximum size in GB
    private static final long FILE_AGE_LIMIT_HOURS = 12; // Minimum file age to delete in hours
    private static final long PRUNE_INTERVAL_MINUTES = 30; // Prune interval in minutes

    public static void init() {
        // Schedule the task to run every PRUNE_INTERVAL_MINUTES
        LogHelper.info("Starting WebPage Uploads Pruner");
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(FolderPruner::pruneFolder, 0, PRUNE_INTERVAL_MINUTES, TimeUnit.MINUTES);
    }

    private static void pruneFolder() {
        File folder = new File(FOLDER_PATH);
        if (!folder.exists() || !folder.isDirectory()) {
            LogHelper.error("Invalid folder path.");
            return;
        }

        // Get folder size in bytes
        long maxSizeBytes = MAX_SIZE_GB * 1024 * 1024 * 1024; // Convert GB to bytes
        long currentSizeBytes = calculateFolderSize(folder);

        // If the folder size exceeds the limit, prune the folder
        if (currentSizeBytes > maxSizeBytes) {
            LogHelper.info("Folder exceeds size limit. Starting pruning process...");

            // Get files older than the specified age limit
            List<File> filesToDelete = Arrays.stream(folder.listFiles())
                    .filter(file -> file.lastModified() < System.currentTimeMillis() - FILE_AGE_LIMIT_HOURS * 60 * 60 * 1000)
                    .sorted(Comparator.comparingLong(File::lastModified)) // Sort files by last modified (oldest first)
                    .toList();

            // Delete files until the folder is within the size limit
            for (File file : filesToDelete) {
                if (calculateFolderSize(folder) > maxSizeBytes) {
                    LogHelper.info("Deleting file: " + file.getName());
                    file.delete();
                }
            }
        } else {
            LogHelper.info("Folder size is within limit. No pruning needed.");
        }
    }

    private static long calculateFolderSize(File folder) {
        // Calculate the total size of the files in the folder
        return Arrays.stream(folder.listFiles())
                .mapToLong(file -> file.isFile() ? file.length() : 0)
                .sum();
    }
}