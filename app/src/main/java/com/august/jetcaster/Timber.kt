package com.august.jetcaster

import timber.log.Timber

class JetCasterTree : Timber.DebugTree() {

    override fun log(
        priority: Int, tag: String?, message: String, t: Throwable?
    ) {
        val (className, methodName) = getMethodName()
        super.log(priority, GLOBAL_TAG, "$className: $methodName: $message", t)
    }

    private fun getMethodName(): Pair<String, String> {
        val trace = Throwable().stackTrace.first { it.className !in fqcnIgnore }
        val originalClassName = trace.className.substringAfterLast('.')
        val className = if (originalClassName.length < MAX_CLASS_NAME_LENGTH) originalClassName
        else originalClassName.substring(0, MAX_CLASS_NAME_LENGTH)
        return className to trace.methodName
    }

    companion object {
        private const val GLOBAL_TAG = "JC_TAG"
        private const val MAX_CLASS_NAME_LENGTH = 16
        private val fqcnIgnore = listOf(
            Timber::class.java.name,
            Timber.Forest::class.java.name,
            Timber.Tree::class.java.name,
            Timber.DebugTree::class.java.name,
            JetCasterTree::class.java.name
        )
    }
}