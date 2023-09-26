package org.example.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This is mandatory in for the RepeatableAnnotation's @Repeatable behavior to work
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface RepeatableAnnotationContainer {

  RepeatableAnnotation[] value();
}
