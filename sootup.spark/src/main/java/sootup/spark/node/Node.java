package sootup.spark.node;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import sootup.core.types.Type;

@FieldDefaults(makeFinal=true, level= AccessLevel.PRIVATE)
@Getter
@SuperBuilder
public class Node {
    @NonNull
    Type type;

    @Override
    public String toString() {
        return String.format("\"%s\"", getType());
    }

}
