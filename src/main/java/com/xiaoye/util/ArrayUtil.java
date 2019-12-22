package com.xiaoye.util;

public class ArrayUtil {

    public static <T> boolean hasElem(T[] array)
    {
        return array != null && array.length > 0;
    }

    public static boolean contains(String[] array, String elem)
    {
        for (String s : array)
        {
            if (s.equals(elem))
                return true;
        }
        return false;
    }
}
