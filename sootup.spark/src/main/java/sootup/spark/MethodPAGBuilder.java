package sootup.spark;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import sootup.core.model.SootMethod;

@Getter
@Builder
public class MethodPAGBuilder {

    @NonNull
    private SootMethod sootMethod;

}
