package com.tony.callerloc.utils;

public class EncryptUtil {

    public static int encryptPrefix(int p) {
        return p + 35;
    }

    public static int encryptMid(int n) {
        // first swap, then rotate
        return rotate(swap(n));
    }

    // swap by 5000
    private static int swap(int n) {
        int m = n;
        if (m < 5000) {
            m += 5000;
        } else {
            m -= 5000;
        }
        return m;
    }

    // rotate by 103, 2000
    private static int rotate(int n) {
        int m = n;
        if (m < 2000) {
            m += 103;
            if (m >= 2000) {
                m -= 2000;
            }
        } else if (m < 4000) {
            m += 103;
            if (m >= 4000) {
                m -= 2000;
            }
        } else if (m < 6000) {
            m += 103;
            if (m >= 6000) {
                m -= 2000;
            }
        } else if (m < 8000) {
            m += 103;
            if (m >= 8000) {
                m -= 2000;
            }
        } else if (m < 10000) {
            m += 103;
            if (m >= 10000) {
                m -= 2000;
            }
        }

        return m;
    }
}
