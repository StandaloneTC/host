package tech.standalonetc.host.struct

import org.mechdancer.dependency.Component

/**
 * TreeComponent
 *
 * Constructing and can be added to scope recursively
 * All [children] can be found in scope.
 */
abstract class TreeComponent(
    val name: String,
    val parent: TreeComponent?
) : Component {

    private val _children = mutableListOf<TreeComponent>()

    protected fun <T : TreeComponent> T.asChild() =
        this@asChild.also(this@TreeComponent._children::plusAssign)

    private val ancestors: List<TreeComponent> = parent?.ancestors?.plus(parent) ?: listOf()
    val children = object : List<TreeComponent> by _children {}

    override fun toString() =
        "${ancestors.joinToString(separator = ".") { it.name }}.$name"

    final override fun equals(other: Any?): Boolean {
        if (other !is TreeComponent) return false
        return other.parent == parent && other.name == name
    }

    final override fun hashCode() =
        (parent.hashCode() shl 31) + name.hashCode()

    fun String.joinPrefix() = "${toString()}.$this"

}
