package zzzank.probejs.lang.java.clazz;

import zzzank.probejs.lang.java.clazz.members.ConstructorInfo;
import zzzank.probejs.lang.java.clazz.members.FieldInfo;
import zzzank.probejs.lang.java.clazz.members.MethodInfo;

import java.util.stream.Stream;

/**
 * @author ZZZank
 */
public interface MemberCollector {
    void accept(Class<?> clazz);

    Stream<? extends ConstructorInfo> constructors();

    Stream<? extends MethodInfo> methods();

    Stream<? extends FieldInfo> fields();
}
