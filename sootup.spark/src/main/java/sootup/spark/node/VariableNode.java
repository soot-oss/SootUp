package sootup.spark.node;

import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class VariableNode extends Node {

    @NonNull
    private String name;

}
