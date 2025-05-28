package template.core.base.designsystem.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Stable
class ComponentStateHolder<T>(initialValue: T) : ComponentState<T> {
    override var value by mutableStateOf(initialValue)
        private set

    override fun update(newValue: T) {
        value = newValue
    }
}

@Composable
fun <T> rememberComponentState(initialValue: T): ComponentState<T> {
    return remember { ComponentStateHolder(initialValue) }
}
