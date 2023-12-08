package org.timofeevAS;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static java.nio.file.StandardOpenOption.*;

public class ReverseByteFile {
    private static void reverseBuffer(ByteBuffer buffer) {
        int limit = buffer.limit();
        int half = limit / 2;

        for (int i = 0; i < half; i++) {
            int other = limit - i - 1;
            byte b = buffer.get(i);
            buffer.put(i, buffer.get(other));
            buffer.put(other, b);
        }
    }
    public static void reverseBytes(Path src, Path dst){
        try (FileChannel srcFileChannel = FileChannel.open(src, READ);
             FileChannel dstFileChannel = FileChannel.open(dst,CREATE,WRITE,TRUNCATE_EXISTING);
        ) {


            long count = srcFileChannel.size();
            long position = count - 1;

            if (count == 0){
                ByteBuffer emptyBuff = ByteBuffer.allocate(0);
                dstFileChannel.write(emptyBuff);
                return;
            }

            while (count > 0) {
                long bufferSize = Math.min(count, 1024); // Adjust the buffer size as needed
                ByteBuffer buffer = ByteBuffer.allocate((int) bufferSize);

                int bytesRead = srcFileChannel.read(buffer, position - bufferSize + 1);
                buffer.flip();

                // Reverse the order of bytes in the buffer
                reverseBuffer(buffer);

                buffer.position(0); // Ensure the position is set to the beginning
                buffer.limit((int) bufferSize); // Adjust limit to the actual number of bytes read

                dstFileChannel.write(buffer);
                count -= bytesRead;
                position -= bytesRead;
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            // Parsing args, we need two files
            System.out.println("Usage: java ReverseByteFile inputFilePath outputFilePath");
            return;
        }
        String inputFilePath = args[0];
        String outputFilePath = args[1];

        Path inputPath = Path.of(inputFilePath);
        Path outputPath = Path.of(outputFilePath);
        if (!inputPath.toFile().exists()) {
            System.out.println("Error: Input file does not exist.");
            return;
        }

        reverseBytes(inputPath,outputPath);
    }
}
