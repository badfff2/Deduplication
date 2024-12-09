import java.io.*;
import java.util.*;

public class Store {
    private static final int CONTAINER_SIZE = 1024 * 1024; // 1 MiB
    private final File dataDir;
    private ByteArrayOutputStream currentContainer;
    private Map<String, Integer> containerChunks;
    private int currentContainerId;
    
    public Store() {
        dataDir = new File("data");
        currentContainer = new ByteArrayOutputStream();
        containerChunks = new HashMap<>();
        currentContainerId = getNextContainerId();
    }
    
    private int getNextContainerId() {
        int maxId = -1;
        File[] files = dataDir.listFiles((dir, name) -> name.startsWith("container_"));
        if (files != null) {
            for (File file : files) {
                try {
                    int id = Integer.parseInt(file.getName().split("_")[1]);
                    maxId = Math.max(maxId, id);
                } catch (NumberFormatException e) {
                    // Skip invalid filenames
                }
            }
        }
        return maxId + 1;
    }
    
    public void addChunk(String hash, byte[] chunk) throws IOException {
        if (currentContainer.size() + chunk.length > CONTAINER_SIZE) {
            flushContainer();
        }
        
        containerChunks.put(hash, currentContainer.size());
        currentContainer.write(chunk);
    }
    
    public void flushContainer() throws IOException {
        if (currentContainer.size() > 0) {
            File containerFile = new File(dataDir, "container_" + currentContainerId);
            try (FileOutputStream fos = new FileOutputStream(containerFile)) {
                currentContainer.writeTo(fos);
            }
            
            // Write chunk offsets
            File offsetFile = new File(dataDir, "container_" + currentContainerId + ".offset");
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(offsetFile))) {
                oos.writeObject(containerChunks);
            }
            
            currentContainerId++;
            currentContainer = new ByteArrayOutputStream();
            containerChunks = new HashMap<>();
        }
    }
}