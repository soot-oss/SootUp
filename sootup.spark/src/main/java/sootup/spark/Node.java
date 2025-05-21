package sootup.spark;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import sootup.core.types.Type;

@Getter
@Builder
public class Node {
    @NonNull
    private final String Name;
    @NonNull
    private final Type type;

}
