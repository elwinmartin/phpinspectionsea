package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.FileSystemUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class LongInheritanceChainInspector extends BasePhpInspection {
    private static final String messagePattern = "Class has %c% parent classes, consider using appropriate design patterns.";

    private static final Set<String> showStoppers = new HashSet<>();
    static {
        showStoppers.add("\\PHPUnit_Framework_TestCase");
        showStoppers.add("\\PHPUnit\\Framework\\TestCase");

        showStoppers.add("\\yii\\base\\Behavior");
        showStoppers.add("\\yii\\base\\Component");

        showStoppers.add("\\Shopware\\Bundle\\StoreFrontBundle\\Struct\\BaseProduct");
        showStoppers.add("\\Enlight_Controller_Action");
        showStoppers.add("\\Enlight_Plugin_Bootstrap");

        showStoppers.add("\\sfActions");
        showStoppers.add("\\sfAction");
        showStoppers.add("\\sfBaseTask");
        showStoppers.add("\\sfFormObject");
        showStoppers.add("\\sfFormFilter");

        showStoppers.add("\\BaseObject");
        showStoppers.add("\\ModelCriteria");
        showStoppers.add("\\Propel\\Runtime\\ActiveQuery\\ModelCriteria");
    }

    @NotNull
    public String getShortName() {
        return "LongInheritanceChainInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpClass(PhpClass clazz) {
                final PsiElement psiClassName = NamedElementUtil.getNameIdentifier(clazz);
                final String className        = clazz.getName();
                /* skip un-reportable, exception and test classes */
                if (null == psiClassName || FileSystemUtil.isTestClass(clazz) || className.endsWith("Exception")) {
                    return;
                }

                PhpClass classToCheck = clazz;
                PhpClass parent       = classToCheck.getSuperClass();
                /* false-positives: abstract class implementation */
                if (null != parent && !classToCheck.isAbstract() && parent.isAbstract()) {
                    return;
                }

                int parentsCount = 0;
                /* in source code class CAN extend itself, PS will report it but data structure is incorrect still */
                while (null != parent && clazz != parent) {
                    classToCheck = parent;
                    parent       = classToCheck.getSuperClass();
                    ++parentsCount;

                    /* show-stoppers: frameworks god classes */
                    if (null != parent && showStoppers.contains(parent.getFQN())) {
                        break;
                    }
                }

                if (parentsCount >= 3 && !clazz.isDeprecated()) {
                    final String message = messagePattern.replace("%c%", String.valueOf(parentsCount));
                    holder.registerProblem(psiClassName, message, ProblemHighlightType.WEAK_WARNING);
                }
            }
        };
    }
}