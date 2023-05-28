package ru.bav.entry;

public class TestNative {

    public static native String[] similarityTest(String input);
    public static native void loadBlacklist(String[] blacklist);
}
