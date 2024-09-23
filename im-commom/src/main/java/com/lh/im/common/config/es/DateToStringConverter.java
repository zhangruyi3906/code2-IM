package com.lh.im.common.config.es;

import com.lh.im.common.util.TimeUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.util.StringUtils;

import java.util.Date;


/**
 * @author sea
 * @date 2022/4/28
 */
@WritingConverter
public enum DateToStringConverter implements Converter<Date, String> {
    INSTANCE;

    @Override
    public String convert(Date source) {
        if (StringUtils.isEmpty(source)) {
            return null;
        }
        return TimeUtils.format(source, TimeUtils.DATE_TIME);
    }
}
