package de.upb.soot.namespaces;

import de.upb.soot.namespaces.classprovider.ClassSource;
import de.upb.soot.signatures.ClassSignature;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Composes a namespace out of other namespaces hence removing the necessity to adapt every API to allow for multiple namespaces
 *
 * @author Linghui Luo
 * @author Ben Hermann
 *
 */
public class CompositeNamespace implements INamespace {
    private List<INamespace> namespaces;

    public CompositeNamespace(List<INamespace> namespaces) {
        this.namespaces = namespaces;
    }

    @Override
    public Optional<ClassSource> getClassSource(ClassSignature signature) {
        List<Optional<ClassSource>> result = namespaces.stream().map(n -> n.getClassSource(signature))
                                                                .filter(o -> o.isPresent()).collect(Collectors.toList());
        if(result.size() > 1) {
            // TODO: Warn here b/c of multiple results
            return Optional.empty();
        }
        if(result.size() == 1) return result.get(0);
        return Optional.empty();
    }
}
