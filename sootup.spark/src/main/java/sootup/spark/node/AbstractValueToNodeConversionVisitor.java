package sootup.spark.node;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.visitor.AbstractValueVisitor;

@Slf4j
public abstract class AbstractValueToNodeConversionVisitor extends AbstractValueVisitor {
    @Override
    public void defaultCaseValue(@NonNull Value v) {
        log.warn("Unimplemented node conversion for value: {} of type: {}", v, v.getClass());
    }
}