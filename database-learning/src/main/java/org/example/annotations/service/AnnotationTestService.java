package org.example.annotations.service;

import org.example.annotations.InheritedAnnotation;
import org.example.annotations.NonRepeatableAnnotation;
import org.example.annotations.RepeatableAnnotation;

/** NonRepeatableAnnotation - can be used only once */
@NonRepeatableAnnotation
//@NonRepeatableAnnotation // this is showing a compile error - "Duplicate annotation"

/** Repeatable annotations  */
// Repeatable annotation can be called inside the container
//@RepeatableAnnotationContainer(value = {
//    @RepeatableAnnotation,
//    @RepeatableAnnotation
//})

// Repeatable annotation can also be called multiple times without a container
@RepeatableAnnotation
@RepeatableAnnotation
@RepeatableAnnotation

//@MethodAnnotation // this is showing a compile error - "not applicable to type"
@InheritedAnnotation
public class AnnotationTestService {

}
