package zzzank.probejs;

import com.google.gson.GsonBuilder;
import lombok.val;
import net.minecraft.util.GsonHelper;
import zzzank.probejs.lang.java.ClassRegistry;
import zzzank.probejs.lang.java.clazz.ClazzMemberCollector;
import zzzank.probejs.utils.NameUtils;

import java.util.Arrays;

import static zzzank.probejs.ProbeJS.LOGGER;

/**
 * @author ZZZank
 */
public class Tes {

    public static void main(String[] args) {
//        val reg = new ClassRegistry(new ClazzMemberCollector());
//
//        reg.fromClasses(Arrays.asList(
//            GsonBuilder.class,
//            GsonHelper.class
//        ));
//
//        reg.walkClass();
//        LOGGER.info("found {} class", reg.foundClasses.size());

        val orig = "01234567890";
        val replaced = NameUtils.cutOffStartEnds(orig, Arrays.asList(
            new int[] {0, 2},
            new int[] {4, 4}
        ));
        LOGGER.info(replaced);
    }
}
