package test;

import org.springframework.web.bind.annotation.GetMapping;

/**
 * desc:
 *
 * @author <a href="kangqinghua#wxchina.com">beswarm</a>
 * @version 1.0.0
 * @date 2018-03-02
 */
public class TestControllerImpl implements TestControllerInterface {

    @GetMapping("/test")
    @Override
    public String test() {
        return "test success";
    }
}
