import java.io.*;
import java.util.*;

public class Index implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Maps chunk hash to list of files that reference it
    private Map<String, Set<String>> chunkReferences;
    // Maps filename to list of chunk hashes that compose the file
    private Map<String, List<String>> fileRecipes;
    // Maps chunk hash to its size in bytes
    private Map<String, Integer> chunkSizes;
    
    private int totalFiles;
    private long totalPreDedupBytes;
    private long totalUniqueBytes;
    private int totalContainers;
    
    public Index() {
        chunkReferences = new HashMap<>();
        fileRecipes = new HashMap<>();
        chunkSizes = new HashMap<>();
        totalFiles = 0;
        totalPreDedupBytes = 0;
        totalUniqueBytes = 0;
        totalContainers = 0;
    }
    
    public void addChunk(String hash, byte[] chunk, String filename) {
        int chunkSize = chunk.length;
        
        if (!chunkReferences.containsKey(hash)) {
            chunkReferences.put(hash, new HashSet<>());
            chunkSizes.put(hash, chunkSize);
            totalUniqueBytes += chunkSize;
        }
        
        chunkReferences.get(hash).add(filename);    
        
        if (!fileRecipes.containsKey(filename)) {
            fileRecipes.put(filename, new ArrayList<>());
        }
        fileRecipes.get(filename).add(hash);
        totalPreDedupBytes += chunkSize;
    }

    public void incrementContainers() {
        totalContainers++;
    }
    
    public void incrementFiles() {
        totalFiles++;
    }
    
    public static Index load() {
        File indexFile = new File("mydedup.index");
        if (!indexFile.exists()) {
            return new Index();
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(indexFile))) {
            return (Index) ois.readObject();
        } catch (Exception e) {
            System.err.println("Error loading index: " + e.getMessage());
            return new Index();
        }
    }
    
    public void save() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("mydedup.index"))) {
            oos.writeObject(this);
        } catch (Exception e) {
            System.err.println("Error saving index: " + e.getMessage());
        }
    }
    
    public void printStats() {
        System.out.println("Total number of files that have been stored: " + totalFiles);
        System.out.println("Total number of pre-deduplicated chunks in storage: " + 
            fileRecipes.values().stream().mapToInt(List::size).sum());
        System.out.println("Total number of unique chunks in storage: " + chunkReferences.size());
        System.out.println("Total number of bytes of pre-deduplicated chunks in storage: " + totalPreDedupBytes);
        System.out.println("Total number of bytes of unique chunks in storage: " + totalUniqueBytes);
        System.out.println("Total number of containers in storage: " + totalContainers);
        System.out.printf("Deduplication ratio: %.2f%n", (double) totalPreDedupBytes / totalUniqueBytes);
    }

    public boolean containsFile(String fileName){
        return fileRecipes.containsKey(fileName);
    }

    public boolean containsChunk(String hash){
        return chunkReferences.containsKey(hash);
    }

    public List<String> getFileRecipe(String filename) {
        return fileRecipes.get(filename);
    }
    
    public int getChunkSize(String hash) {
        return chunkSizes.get(hash);
    }
}