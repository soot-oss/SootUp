package sootup.spark.node;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import sootup.core.types.Type;

@Getter
@SuperBuilder
public class Node {
    @NonNull
    private final Type type;

}
