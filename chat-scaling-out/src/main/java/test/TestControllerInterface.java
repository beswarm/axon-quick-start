package test;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * desc:
 *
 * @author <a href="kangqinghua#wxchina.com">beswarm</a>
 * @version 1.0.0
 * @date 2018-03-02
 */
@RestController
@RequestMapping("/endpoints")
public interface TestControllerInterface {
    String test();
}
