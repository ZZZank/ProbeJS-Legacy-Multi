package zzzank.probejs.features.rhizo;

import java.util.function.Supplier;

/**
 * @author ZZZank
 */
public interface RhizoState {

    Supplier<Boolean> MOD = () -> true;
    boolean ENUM_TYPE_WRAPPER = true;
    boolean ENUM_TYPE_INFO = false;
    boolean REMAPPER = true;
    boolean GENERIC_ANNOTATION = true;
    boolean INFO_ANNOTATION = true;
    boolean RETURNS_SELF_ANNOTATION = false;
    boolean CLASS_WRAPPER = true;
}
