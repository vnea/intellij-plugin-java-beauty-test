package com.vnea.intellij.plugin.javabeautytest.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil
import org.apache.commons.lang3.StringUtils
import java.util.*


class JavaTestFoldingBuilder : FoldingBuilderEx() {

    companion object {
        private const val JAVA_TEST_FOLDING = "JAVA_TEST_FOLDING"
        private const val ANNOTATION_JUNIT_TEST = "org.junit.jupiter.api.Test"
        private const val ANNOTATION_JUNIT_NESTED = "org.junit.jupiter.api.Nested"
    }

    override fun getPlaceholderText(node: ASTNode): String? {
        val psiElement = node.psi

        if (psiElement is PsiMethod) {
            return toHumanReadable(psiElement.name)
        } else if (psiElement is PsiClass) {
            val name = psiElement.name
            if (name != null) {
                return toHumanReadable(name)
            }
        }

        return null
    }

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val group = FoldingGroup.newGroup(JAVA_TEST_FOLDING)

        val descriptors = ArrayList<FoldingDescriptor>()
        val psiElements = PsiTreeUtil.findChildrenOfAnyType(
            root,
            PsiMethod::class.java,
            PsiClass::class.java
        )

        for (psiElement in psiElements) {
            if (psiElement is PsiMethod && psiElement.hasAnnotation(ANNOTATION_JUNIT_TEST)) {
                descriptors.add(createFoldingDescriptor(psiElement, psiElement.name, group))
            } else if (psiElement is PsiClass && psiElement.hasAnnotation(ANNOTATION_JUNIT_NESTED)) {
                val className = psiElement.name
                if (className != null) {
                    descriptors.add(createFoldingDescriptor(psiElement, className, group))
                }
            }
        }

        return descriptors.toTypedArray()
    }

    override fun isCollapsedByDefault(node: ASTNode) = true

    private fun createFoldingDescriptor(
        psiElement: PsiElement,
        methodOrClassName: String,
        group: FoldingGroup?
    ) = FoldingDescriptor(
        psiElement.node,
        TextRange(
            psiElement.textOffset,
            psiElement.textOffset + methodOrClassName.length
        ),
        group
    )

    private fun toHumanReadable(text: String): String {
        val humanReadable = StringUtils.join(
            StringUtils
                .splitByCharacterTypeCamelCase(text)
                .filterNot { chunkName -> chunkName.matches(Regex("_+")) }
                .map(String::toLowerCase),
            StringUtils.SPACE
        )

        return StringUtils.capitalize(humanReadable)
    }
}
