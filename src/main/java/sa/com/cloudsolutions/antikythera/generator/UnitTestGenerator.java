package sa.com.cloudsolutions.antikythera.generator;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.Expression;
import sa.com.cloudsolutions.antikythera.depsolver.Graph;
import sa.com.cloudsolutions.antikythera.evaluator.SpringEvaluator;
import sa.com.cloudsolutions.antikythera.parser.AbstractCompiler;
import sa.com.cloudsolutions.antikythera.parser.ImportUtils;
import sa.com.cloudsolutions.antikythera.parser.ImportWrapper;

import java.util.List;
import java.util.Map;

public class UnitTestGenerator extends TestGenerator {
    @Override
    public void createTests(MethodDeclaration md, ControllerResponse response) {
        MethodDeclaration testMethod = buildTestMethod(md);
        gen.getType(0).addMember(testMethod);

        createInstance(md, testMethod);
    }

    private void createInstance(MethodDeclaration md, MethodDeclaration testMethod) {
        md.findAncestor(ClassOrInterfaceDeclaration.class).ifPresent(c -> {
            boolean matched = false;
            for (ConstructorDeclaration cd : c.findAll(ConstructorDeclaration.class)) {
                if (cd.getParameters().isEmpty()) {
                    getBody(testMethod).addStatement(c.getNameAsString() + "cls = new " + c.getNameAsString() + "()");
                    matched = true;
                    break;
                }
            }
        });
    }

    @Override
    public void setCommonPath(String commonPath) {

    }

    @Override
    public void setPreconditions(List<Expression> expr) {

    }

    @Override
    public boolean isBranched() {
        return false;
    }

    @Override
    public void setBranched(boolean branched) {

    }

    @Override
    public void addBeforeClass() {
        mockFields();
    }

    @Override
    public void mockFields() {
        TypeDeclaration<?> t = gen.getType(0);
        gen.addImport("org.springframework.boot.test.mock.mockito.MockBean");
        for (Map.Entry<String, CompilationUnit> entry : Graph.getDependencies().entrySet()) {
            CompilationUnit cu = entry.getValue();
            for (TypeDeclaration<?> decl : cu.getTypes()) {
                decl.findAll(FieldDeclaration.class).forEach(fd -> {
                    fd.getAnnotationByName("Autowired").ifPresent(ann -> {
                        FieldDeclaration field = t.addField(fd.getElementType(), fd.getVariable(0).getNameAsString());
                        field.addAnnotation("MockBean");
                        ImportWrapper wrapper = AbstractCompiler.findImport(cu, field.getElementType().asString());
                        if (wrapper != null) {
                            gen.addImport(wrapper.getImport());
                        }
                    });
                });
            }
        }
    }
}
