package com.lh.im.common.config.es;

import com.lh.im.common.util.TimeUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.util.StringUtils;

import java.util.Date;


/**
 * @author sea
 * @date 2022/4/28
 */
@ReadingConverter
public enum StringToDateConverter implements Converter<String, Date> {
    INSTANCE;

    @Override
    public Date convert(String source) {
        if (StringUtils.isEmpty(source)) {
            return null;
        }
        return TimeUtils.parseAsDate(source, TimeUtils.DATE_TIME);
    }

}
