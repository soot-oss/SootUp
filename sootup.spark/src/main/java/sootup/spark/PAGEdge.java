package sootup.spark;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jgrapht.graph.DefaultEdge;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PAGEdge extends DefaultEdge {

    public enum EdgeType {
        ALLOCATION,
        ASSIGNMENT,
        STORE,
        LOAD
    }

    private final EdgeType edgeType;

    public static PAGEdge allocation(){
        return new PAGEdge(EdgeType.ALLOCATION);
    }

    public static PAGEdge assignment(){
        return new PAGEdge(EdgeType.ASSIGNMENT);
    }

    public static PAGEdge store(){
        return new PAGEdge(EdgeType.STORE);
    }

    public static PAGEdge load(){
        return new PAGEdge(EdgeType.LOAD);
    }

}
