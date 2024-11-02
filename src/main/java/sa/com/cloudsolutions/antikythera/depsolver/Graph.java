package sa.com.cloudsolutions.antikythera.depsolver;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import sa.com.cloudsolutions.antikythera.exception.AntikytheraException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Graph {
    /**
     * Map of fully qualified class names and their generated compilation units.
     *
     * For most classes the generated compilation unit will only be a subset of the input
     * compilation unit.
     */
    private static final Map<String, CompilationUnit> dependencies = new HashMap<>();
    /**
     * Map of nodes with their hash code as the key.
     * This is essentially our graph.
     */
    private static final Map<Integer, GraphNode> nodes = new HashMap<>();

    private Graph() {

    }

    /**
     * Creates a new graph node from the AST node if required.
     * If the GraphNode is already present in the graph, the same object is returned.
     * @param n AST node
     * @return a GraphNode that may have already existed.
     * @throws AntikytheraException if resolution fails
     */
    public static GraphNode createGraphNode(Node n) throws AntikytheraException {
        GraphNode g = nodes.get(n.hashCode());
        if(g != null) {
            return g;
        }
        g = new GraphNode(n);
        nodes.put(n.hashCode(), g);

        ClassOrInterfaceDeclaration cdecl = g.getEnclosingType();
        if (cdecl != null) {
            Optional<String> fullyQualifiedName = cdecl.getFullyQualifiedName();
            if (fullyQualifiedName.isPresent()) {
                String fqn = fullyQualifiedName.get();
                CompilationUnit destination = new CompilationUnit();
                if (!dependencies.containsKey(fqn)) {
                    dependencies.put(fqn, destination);
                    ClassOrInterfaceDeclaration target = destination.addClass(cdecl.getNameAsString());
                    target.setModifiers(cdecl.getModifiers());
                    if (cdecl.getJavadocComment().isPresent()) {
                        target.setJavadocComment(cdecl.getJavadocComment().get());
                    }
                    g.setClassDeclaration(target);
                }
                g.setDestination(destination);
            }
        }

        return g;
    }

    public static Map<String, CompilationUnit> getDependencies() {
        return dependencies;
    }

    public static Map<Integer, GraphNode> getNodes() {
        return nodes;
    }
}
