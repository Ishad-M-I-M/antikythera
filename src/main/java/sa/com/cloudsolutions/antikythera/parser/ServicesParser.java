package sa.com.cloudsolutions.antikythera.parser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sa.com.cloudsolutions.antikythera.configuration.Settings;
import sa.com.cloudsolutions.antikythera.depsolver.Graph;
import sa.com.cloudsolutions.antikythera.evaluator.AntikytheraRunTime;
import sa.com.cloudsolutions.antikythera.evaluator.ArgumentGenerator;

import sa.com.cloudsolutions.antikythera.evaluator.SpringEvaluator;
import sa.com.cloudsolutions.antikythera.exception.AntikytheraException;
import sa.com.cloudsolutions.antikythera.exception.GeneratorException;
import sa.com.cloudsolutions.antikythera.generator.UnitTestGenerator;

import java.util.Map;

public class ServicesParser extends  DepsolvingParser{
    private static final Logger logger = LoggerFactory.getLogger(ServicesParser.class);

    CompilationUnit cu;
    SpringEvaluator evaluator;

    public ServicesParser(String cls) {
        this.cu = AntikytheraRunTime.getCompilationUnit(cls);
        if (this.cu == null) {
            throw new AntikytheraException("Class not found: " + cls);
        }
        evaluator = new SpringEvaluator(cls);
        evaluator.addGenerator(new UnitTestGenerator());

    }

    private void autoWire() {
        for (Map.Entry<String, CompilationUnit> entry : Graph.getDependencies().entrySet()) {
            CompilationUnit cu = entry.getValue();
            for (TypeDeclaration<?> decl : cu.getTypes()) {
                decl.findAll(FieldDeclaration.class).forEach(fd -> {
                    fd.getAnnotationByName("Autowired").ifPresent(ann -> {
                        System.out.println("Autowired found: " + fd.getVariable(0).getNameAsString());
                    });
                });
            }
        }
    }


    public void evaluateMethod(MethodDeclaration md, ArgumentGenerator gen) {
        evaluator.setArgumentGenerator(gen);
        evaluator.reset();
        evaluator.resetColors();
        AntikytheraRunTime.reset();
        try {
            evaluator.visit(md);

        } catch (AntikytheraException | ReflectiveOperationException e) {
            if ("log".equals(Settings.getProperty("dependencies.on_error"))) {
                logger.warn("Could not complete processing {} due to {}", md.getName(), e.getMessage());
            } else {
                throw new GeneratorException(e);
            }
        } finally {
            logger.info(md.getNameAsString());
        }
    }

    private boolean checkEligible(MethodDeclaration md) {
        return !md.isPublic();
    }
}
