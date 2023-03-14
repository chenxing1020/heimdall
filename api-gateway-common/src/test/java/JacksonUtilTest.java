import com.xchen.heimdall.common.util.JacksonUtil;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static com.xchen.heimdall.common.util.JacksonUtil.JS_NUMBER_MAX_VALUE;
import static com.xchen.heimdall.common.util.JacksonUtil.JS_NUMBER_MIN_VALUE;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author xchen
 * @date 2022/10/26
 */
class JacksonUtilTest {

    @Test
    void testLongSerializer() {

        Set<Long> set = new HashSet<>();
        set.add(JS_NUMBER_MIN_VALUE + 1);
        set.add(JS_NUMBER_MAX_VALUE - 1);
        set.add(JS_NUMBER_MIN_VALUE);
        set.add(JS_NUMBER_MAX_VALUE);
        set.add(Long.MAX_VALUE);
        set.add(Long.MIN_VALUE);
        assertEquals("[\"-9007199254740992\",\"9007199254740991\",\"9223372036854775807\",\"-9223372036854775808\",-9007199254740991,9007199254740990]", JacksonUtil.encode(set));
    }
}
