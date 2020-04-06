package com.deepwelldevelopment.spacequest.util;

import org.lwjgl.BufferUtils;

import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

public class IOUtils {

    /**
     * Resizes a buffer
     *
     * @param buffer      The buffer to resize
     * @param newCapacity The desired capacity of the buffer
     * @return A new buffer with the given capacity
     */
    private static ByteBuffer resizeBuffer(ByteBuffer buffer, int newCapacity) {
        ByteBuffer newBuffer = BufferUtils.createByteBuffer(newCapacity);
        buffer.flip();
        newBuffer.put(buffer);
        return buffer;
    }

    /**
     * Loads a resource file into a byte buffer
     *
     * @param resource   The resource to load
     * @param bufferSize The desired size of the buffer. If the size is not
     *                   enough to load the resource, the buffer will be resized
     * @return The buffer, filled with the byte data from the file
     * @throws IOException If the resource is unable to be read
     */
    public static ByteBuffer ioResourceToByteBuffer(String resource,
            int bufferSize) throws IOException {
        ByteBuffer buffer;
        URL url = Thread.currentThread().getContextClassLoader()
                .getResource(resource);
        if (url == null) {
            throw new IOException("Classpath resource not found: " + resource);
        }
        File file = new File(url.getFile());

        if (file.isFile()) {
            FileInputStream fis = new FileInputStream(file);
            FileChannel channel = fis.getChannel();
            buffer = channel.map(MapMode.READ_ONLY, 0, channel.size());
            channel.close();
            fis.close();
        } else {
            buffer = BufferUtils.createByteBuffer(bufferSize);
            try (InputStream source = url.openStream()) {
                if (source == null) {
                    throw new FileNotFoundException(resource);
                }
                byte[] buf = new byte[8192];
                while (true) {
                    int bytes = source.read(buf, 0, buf.length);
                    if (bytes == -1) break;
                    if (buffer.remaining() < bytes) {
                        buffer = resizeBuffer(buffer,
                                Math.max(buffer.capacity() * 2,
                                        buffer.capacity() - buffer.remaining() +
                                                bytes
                                )
                        );
                    }
                    buffer.put(buf, 0, bytes);
                }
                buffer.flip();
            }
        }

        return buffer;
    }
}
