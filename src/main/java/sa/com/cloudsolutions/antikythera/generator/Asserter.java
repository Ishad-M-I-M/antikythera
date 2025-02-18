package sa.com.cloudsolutions.antikythera.generator;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sa.com.cloudsolutions.antikythera.evaluator.Evaluator;
import sa.com.cloudsolutions.antikythera.evaluator.Variable;

import java.lang.reflect.Method;
import java.util.Map;

public abstract class Asserter {
    private static final Logger logger = LoggerFactory.getLogger(Asserter.class);

    public abstract void assertNotNull(BlockStmt body, String variable);
    public abstract void setupImports(CompilationUnit gen);

    public void addFieldAsserts(MethodResponse resp, BlockStmt body) {
        if (resp.getBody().getValue() instanceof Evaluator ev) {
            int i = 0;
            for(Map.Entry<String, Variable> field : ev.getFields().entrySet()) {
                try {
                    if (field.getValue() != null && field.getValue().getValue() != null) {
                        Variable v = field.getValue();
                        String getter = "get" + field.getKey().substring(0, 1).toUpperCase() + field.getKey().substring(1);
                        body.addStatement(fieldAssertion(getter, v));
                        i++;
                    }
                } catch (Exception pex) {
                    logger.error("Error asserting {}", field.getKey());
                }
                if (i == 5) {
                    break;
                }
            }
        }
    }


    public ExpressionStmt fieldAssertion(String getter, Variable v) {
        MethodCallExpr assertEquals = new MethodCallExpr(new NameExpr("Assert"), "assertEquals");
        assertEquals.addArgument("resp." + getter + "()");

        if (v.getValue() instanceof String) {
            assertEquals.addArgument("\"" + v.getValue() + "\"");
        } else {
            assertEquals.addArgument(v.getValue().toString());
        }
        return new ExpressionStmt(assertEquals);
    }
}
