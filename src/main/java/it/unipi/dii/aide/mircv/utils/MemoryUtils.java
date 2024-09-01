package it.unipi.dii.aide.mircv.utils;

import static it.unipi.dii.aide.mircv.indexer.spimi.Spimi.MEMORY_LIMIT;

public class MemoryUtils {

    /**
     * Get the memory info of the JVM
     * @return the memory used
     */
    public static float getMemoryInfo() {
        float memoryUsed = (float) (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024;
        float memoryLimit = (float) (Runtime.getRuntime().totalMemory() - MEMORY_LIMIT) / 1024 / 1024;
        System.out.println("Memory used: " + memoryUsed + "/" + memoryLimit + " MB");

        return memoryUsed;
    }

}
