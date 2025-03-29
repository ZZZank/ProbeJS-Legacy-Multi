package zzzank.probejs.utils.config.binding;

import org.jetbrains.annotations.NotNull;

/**
 * @author ZZZank
 */
public class DefaultBinding<T> extends BindingBase<T> {

    private T value;

    public DefaultBinding(@NotNull T defaultValue, @NotNull String name) {
        super(defaultValue, name);
        this.value = defaultValue;
    }

    @Override
    public @NotNull T get() {
        return value;
    }

    @Override
    protected void setImpl(T value) {
        this.value = value;
    }
}
