package ru.bav.entry;

import ru.bav.server.Server;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;

public class Main {
    public static void main(String[] args) {
        loadClasses("KotlinPlug");
        Server.INSTANCE.start();
    }

    private static void loadClasses(String jarFile){
        addLibrary(new File(getJarFolder() + "/libs/"+jarFile+".jar"));
    }

    public static String getJarFolder(){
        String path = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        path = path.replace("/Server.jar", "");
        try {
            return URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "NULL";
        }
    }

    //Only possible on 8 Java
    public static void addLibrary(File file){
        if(!file.exists()){
            throw new IllegalArgumentException("File not found "+file.getAbsolutePath());
        }
        Method method = null;
        try {
            method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        method.setAccessible(true);
        try {
            method.invoke(ClassLoader.getSystemClassLoader(), file.toURI().toURL());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
