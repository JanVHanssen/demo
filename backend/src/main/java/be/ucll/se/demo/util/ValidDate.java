package be.ucll.se.demo.util;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = DateFormatValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDate {
    String message() default "Date is invalid, it has to be of the following format dd/mm/yyyy";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
