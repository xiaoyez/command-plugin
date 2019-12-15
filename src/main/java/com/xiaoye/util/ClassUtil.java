package com.xiaoye.util;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassUtil {

    public static List<Class> searchClass(String basePackage) throws ClassNotFoundException, IOException {
        List<String> classPaths = new ArrayList<String>();
        //包名

        //然后把classpath和basePack合并
        List<Class> classList = new ArrayList<Class>();
        searchClassInJar(basePackage,classPaths,classList);
        searchClassInClassPath(basePackage,classPaths,classList);

        return classList;
    }

    /**
     * 该方法会得到所有的类，将类的绝对路径写入到classPaths中
     * @param file
     */
    private static void doPath(File file,List<String> classPaths) {
        if (file.isDirectory()) {//文件夹
            //文件夹我们就递归
            File[] files = file.listFiles();
            for (File f1 : files) {
                doPath(f1,classPaths);
            }
        } else {//标准文件
            //标准文件我们就判断是否是class文件
            if (file.getName().endsWith(".class")) {
                //如果是class文件我们就放入我们的集合中。
                classPaths.add(file.getPath());
            }
        }
    }

    private static void searchClassInClassPath(String basePackage,List<String> classPaths,List<Class> classList)  {
        //先把包名转换为路径,首先得到项目的classpath
        basePackage = basePackage.replace(".","/");
        String classpath = ClassUtil.class.getResource("/").getPath();
        if (!classpath.endsWith("/classes/"))
        {
            String old = classpath.substring(0,classpath.length() - 1);
            old = old.substring(old.lastIndexOf("/"));
            classpath = classpath.replaceFirst(old,"/classes");
        }
        String searchPath = classpath + basePackage;
        doPath(new File(searchPath),classPaths);
        //这个时候我们已经得到了指定包下所有的类的绝对路径了。我们现在利用这些绝对路径和java的反射机制得到他们的类对象
        for (String s : classPaths) {
            //把 D:\work\code\20170401\search-class\target\classes\com\baibin\search\a\A.class 这样的绝对路径转换为全类名com.baibin.search.a.A
            s = s.replace(classpath.replace("/","\\").replaceFirst("\\\\",""),"").replace("\\",".").replace(".class","");
            Class cls = null;
            try {
                cls = Class.forName(s);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            classList.add(cls);
        }
    }

    private static void searchClassInJar(String basePackage,List<String> classPatths,List<Class> classList) throws IOException {
        Enumeration<URL> urlEnumeration = Thread.currentThread().getContextClassLoader().getResources(basePackage.replace(".", "/"));
        while (urlEnumeration.hasMoreElements())
        {
            URL url = urlEnumeration.nextElement();//得到的结果大概是：jar:file:/C:/Users/ibm/.m2/repository/junit/junit/4.12/junit-4.12.jar!/org/junit
            String protocol = url.getProtocol();//大概是jar
            if ("jar".equalsIgnoreCase(protocol))
            {
                //转换为JarURLConnection
                JarURLConnection connection = (JarURLConnection) url.openConnection();
                if (connection != null)
                {
                    JarFile jarFile = connection.getJarFile();
                    if (jarFile != null)
                    {
                        //得到该jar文件下面的类实体
                        Enumeration<JarEntry> jarEntryEnumeration = jarFile.entries();
                        while (jarEntryEnumeration.hasMoreElements())
                        {
                            JarEntry entry = jarEntryEnumeration.nextElement();
                            String jarEntryName = entry.getName();
                            //这里我们需要过滤不是class文件和不在basePack包名下的类
                            if (jarEntryName.contains(".class") )
                            {
//                                && jarEntryName.replaceAll("/", ".").startsWith(basePackage)
                                jarEntryName = jarEntryName.replaceAll("/",".");
                                if (!jarEntryName.startsWith(basePackage))
                                {
                                    continue;
                                }
                                String className = jarEntryName.substring(0, jarEntryName.lastIndexOf(".")).replace("/", ".");
                                Class cls = null;
                                try {
                                    cls = Class.forName(className);
                                } catch (ClassNotFoundException e) {

                                }catch (NoClassDefFoundError e){

                                }
                                classList.add(cls);
                            }
                        }
                    }
                }
            }
        }
    }

}
