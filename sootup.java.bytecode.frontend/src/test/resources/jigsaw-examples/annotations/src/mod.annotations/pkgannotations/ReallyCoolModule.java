package pkgannotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.MODULE, ElementType.TYPE }) // module only
public @interface ReallyCoolModule {
	String wowReason() default "no reason given";
}
