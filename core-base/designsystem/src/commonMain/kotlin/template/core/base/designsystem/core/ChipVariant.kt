package template.core.base.designsystem.core

sealed interface ChipVariant : ComponentVariant {
    override val name: String

    data object Assist : ChipVariant {
        override val name: String = "assist"
    }

    data object Filter : ChipVariant {
        override val name: String = "filter"
    }

    data object Input : ChipVariant {
        override val name: String = "input"
    }

    data object Suggestion : ChipVariant {
        override val name: String = "suggestion"
    }
}