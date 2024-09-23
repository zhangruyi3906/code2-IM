package com.lh.im.common.config.es;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author dwl
 * @since 2023/12/14
 */
@Component("imEsConfig")
@Getter
@Setter
public class ImEsConfig {

    @Value("${im.es.index.private:im_private_msg_test}")
    private String privateMsgEsIndex;

    @Value("${im.es.index.private:im_group_msg_test}")
    private String groupMsgEsIndex;
}
