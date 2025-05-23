package sootup.spark.node;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@FieldDefaults(makeFinal=true, level= AccessLevel.PRIVATE)
@SuperBuilder
public class AllocationNode extends Node {

    //TODO: we might have to distinguish different allocation sites


    @Override
    public String toString() {
        return String.format("\"new %s\"", getType());
    }
}
