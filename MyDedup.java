import java.io.*;
import java.security.MessageDigest;
import java.util.*;

public class MyDedup {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java MyDedup <operation> [parameters]");
            System.exit(1);
        }

        String operation = args[0].toLowerCase();

        try {
            switch (operation) {
                case "upload":
                    if (args.length != 5) {
                        System.err.println("Usage: java MyDedup upload <min_chunk> <avg_chunk> <max_chunk> <file_to_upload>");
                        return;
                    }
                    int minChunk = Integer.parseInt(args[1]);
                    int avgChunk = Integer.parseInt(args[2]);
                    int maxChunk = Integer.parseInt(args[3]);
                    String fileToUpload = args[4];

                    if (minChunk >= avgChunk || avgChunk >= maxChunk) {
                        System.err.println("Error: Ensure min_chunk < avg_chunk < max_chunk.");
                        return;
                    }
                    if (!isPowerOfTwo(minChunk) || !isPowerOfTwo(avgChunk) || !isPowerOfTwo(maxChunk)) {
                        System.err.println("Error: Chunk sizes must be powers of two.");
                        return;
                    }

                    ensureDataFolderExists();

                    upload(minChunk, avgChunk, maxChunk, fileToUpload);
                    break;

                case "download":
                    if (args.length != 3) {
                        System.err.println("Usage: java MyDedup download <file_to_download> <local_file_name>");
                        return;
                    }
                    String fileToDownload = args[1];
                    String localFileName = args[2];
                    download(fileToDownload, localFileName);
                    break;

                case "delete":
                    if (args.length != 2) {
                        System.err.println("Usage: java MyDedup delete <file_to_delete>");
                        return;
                    }
                    String fileToDelete = args[1];
                    delete(fileToDelete);
                    break;

                    default:
                    System.err.println("Unknown operation: " + operation);
                    System.err.println("Valid operations: upload, download, delete");
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void upload(int minChunk, int avgChunk, int maxChunk, String fileToUpload) throws Exception {
        System.out.println("Uploading file: " + fileToUpload);
        
        // Load index
        Index index = Index.load();
        Store store = new Store();

        // return error if we upload duplicate files
        if (index.containsFile(fileToUpload)){
            System.err.println("Error: Duplicate file detected.");
            return;
        }
        
        // Read file content
        File file = new File(fileToUpload);
        byte[] fileContent = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(fileContent);
        }
        
        // Create Rabin fingerprint chunker
        RabinFingerprint chunker = new RabinFingerprint(minChunk, avgChunk, maxChunk);
        ArrayList<byte[]> chunks = chunker.chunkFile(fileContent);
        
        // Process each chunk
        MessageDigest md = MessageDigest.getInstance("MD5");
        for (byte[] chunk : chunks) {
            // Calculate chunk hash
            md.reset();
            String hash = bytesToHex(md.digest(chunk));
            
            // Add chunk to index and store
            index.addChunk(hash, chunk, fileToUpload);
            store.addChunk(hash, chunk, index);
        }
        
        // Flush remaining chunks in the last container
        store.flushContainer();
        index.incrementContainers();
        index.incrementFiles();
        
        // Save updated index
        index.save();
        
        // Print statistics
        index.printStats();
    }

    private static void download(String fileToDownload, String localFileName) {
        System.out.println("Downloading file: " + fileToDownload);

    }

    private static void delete(String fileToDelete) {
        System.out.println("Deleting file: " + fileToDelete);
    }

    private static boolean isPowerOfTwo(int n) {
        return (n > 0) && (n & (n - 1)) == 0;
    }

    private static void ensureDataFolderExists() {
        File dataFolder = new File("data");
        if (!dataFolder.exists()) {
            if (dataFolder.mkdir()) {
                System.out.println("Created data/ folder for storing containers.");
            } else {
                System.err.println("Error: Unable to create data/ folder. Please check permissions.");
                System.exit(1);
            }
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}