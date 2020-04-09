package com.deepwelldevelopment.spacequest.util;

public class PositionUtils {
    public static long hashOfPosition(int x, int z) {
        return (z << 16) ^ x;
    }
}
