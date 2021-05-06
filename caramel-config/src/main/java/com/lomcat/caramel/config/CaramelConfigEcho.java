/*
 * Copyright 2018-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lomcat.caramel.config;

import com.lomcat.caramel.core.assist.StringAide;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <P>Caramel 加载的配置文件内容打印</P>
 *
 * @author Kweny
 * @since 0.0.1
 */
public class CaramelConfigEcho {

    private static final Logger logger = LoggerFactory.getLogger(CaramelConfigEcho.class);

    public static final String GRANULARITY_SUMMARY = "summary"; // 打印加载的配置文件
    public static final String GRANULARITY_TRACK = "track"; // 打印配置项加载轨迹，包含 summary
    public static final String GRANULARITY_CONTENT = "content"; // 打印完整的配置内容，包含 track 和 summary

    /**
     * 打印内容的粒度：null-不打印；"summary"-打印加载的配置文件；"track"-打印每个配置项的加载轨迹；"content"-打印完整的配置内容content。
     */
    private String granularity;
    /**
     * 敏感数据打印时是否脱敏
     */
    private boolean masking;// TODO-Kweny 需要配置项支持特殊标记的加密/解密处理

    private boolean summaryEnabled;
    private boolean trackEnabled;
    private boolean contentEnabled;

    public boolean isEchoEnabled() {
        return summaryEnabled || trackEnabled || contentEnabled;
    }

    public boolean isSummaryEnabled() {
        return summaryEnabled || trackEnabled;
    }

    public boolean isTrackEnabled() {
        return trackEnabled;
    }

    public boolean isContentEnabled() {
        return contentEnabled;
    }

    public String getGranularity() {
        return granularity;
    }

    public void setGranularity(String granularity) {
        this.granularity = granularity;
        resolveGranularity();
    }

    public boolean isMasking() {
        return masking;
    }

    public void setMasking(boolean masking) {
        this.masking = masking;
    }

    public void echo(String text) {
        logger.info(text);
    }

    private void resolveGranularity() {
        if (StringAide.isBlank(this.granularity)) {
            return;
        }
        String[] granularitySections = this.granularity.split(",");
        for (String section : granularitySections) {
            section = StringAide.trim(section);
            this.summaryEnabled |= StringAide.equalsIgnoreCase(section, GRANULARITY_SUMMARY);
            this.trackEnabled |= StringAide.equalsIgnoreCase(section, GRANULARITY_TRACK);
            this.contentEnabled |= StringAide.equalsIgnoreCase(section, GRANULARITY_CONTENT);
        }
    }
}