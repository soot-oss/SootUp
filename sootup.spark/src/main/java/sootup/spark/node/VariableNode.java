package sootup.spark.node;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@FieldDefaults(makeFinal=true, level= AccessLevel.PRIVATE)
@Getter
@SuperBuilder
public class VariableNode extends Node {

    @NonNull
    String name;

    @Override
    public String toString() {
        return String.format("\"%s %s\"", getType(), getName());
    }
}
