package com.xiaoye.util;


import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;

public class AnnotationUtil {


    public static boolean existAnnotation(AnnotatedElement element, Class<? extends Annotation> annotationClass) {
        if (element == annotationClass)
        {
            return true;
        }
        else
        {
            Annotation annotation = element.getAnnotation(annotationClass);
            if (annotation != null)
            {
                return true;
            }
            else
            {
                Annotation[] declaredAnnotations = element.getDeclaredAnnotations();
                for (Annotation declaredAnnotation : declaredAnnotations)
                {
                    if (declaredAnnotation instanceof Documented || declaredAnnotation instanceof Retention || declaredAnnotation instanceof Target)
                    {
                        continue;
                    }
                    boolean flag = existAnnotation(declaredAnnotation.annotationType(),annotationClass);
                    if (flag)
                        return true;
                }
                return false;
            }
        }
    }

    public static <T extends Annotation> T getAnnotation(AnnotatedElement element,Class<T> annotationClass)
    {
        if (element == annotationClass)
        {
            return null;
        }
        else
        {
            Annotation annotation = element.getAnnotation(annotationClass);
            if (annotation != null)
            {
                return (T)annotation;
            }
            else
            {
                Annotation[] declaredAnnotations = element.getDeclaredAnnotations();
                for (Annotation declaredAnnotation : declaredAnnotations)
                {
                    boolean flag = existAnnotation(declaredAnnotation.annotationType(),annotationClass);
                    if (flag)
                        return (T)declaredAnnotation;
                }
                return null;
            }
        }
    }
}
