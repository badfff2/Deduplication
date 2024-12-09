import java.util.ArrayList;

public class RabinFingerprint {

    private final int minChunk;
    private final int maxChunk;
    private final int anchorMask;

    public RabinFingerprint(int minChunk, int avgChunk, int maxChunk) {
        this.minChunk = minChunk;
        this.maxChunk = maxChunk;
        this.anchorMask = avgChunk - 1;
    }

    public ArrayList<byte[]> chunkFile(byte[] fileContent) {
        ArrayList<byte[]> chunks = new ArrayList<>();
        int start = 0;
        int fingerprint = 0;

        for (int i = 0; i < fileContent.length; i++) {
            fingerprint = (fingerprint * 257 + fileContent[i]) & 0xFFFFFFFF;

            if (((fingerprint & anchorMask) == 0 && (i - start + 1) >= minChunk) || (i - start + 1) >= maxChunk) {
                chunks.add(slice(fileContent, start, i + 1));
                start = i + 1;
            }
        }

        if (start < fileContent.length) {
            chunks.add(slice(fileContent, start, fileContent.length));
        }

        return chunks;
    }

    private byte[] slice(byte[] array, int start, int end) {
        byte[] slice = new byte[end - start];
        System.arraycopy(array, start, slice, 0, slice.length);
        return slice;
    }
}